package cn.hust.hustmall.task;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.RedissonManager;
import cn.hust.hustmall.service.IOrderService;
import cn.hust.hustmall.util.PropertiesUtil;
import cn.hust.hustmall.util.RedisPoolUtil;
import cn.hust.hustmall.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-13 18:39
 **/
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedissonManager redissonManager;



    //用来解决删除锁误删情况
    private ThreadLocal<String> threadLocal = new ThreadLocal();

    /**
     * 1.这里使用redis分布式锁+spring schedule实现定时关单，因为我们只希望有一个tomcat在执行这个关单任务即可，不需要都来执行，会浪费数据库
     * 的性能，而且你不停的执行sql语句，有可能会产生错乱，另外不能只在一个tomcat中加入定时任务的代码，会存在单点故障的问题，所以要做成分布式锁
     *
     * 2.分布式锁最终通过sernx命令判断，只会有一个进程获得锁，但是存在一个问题，如果这个节点挂了，那么死锁，所以设置expire，
     * 但是由于setnx 和expire是两个命令，所以如果在setnx和expire之间挂了，还是会死锁，所以我们加入时间戳，
     *
     *3. 但时间戳和expire也会导致一个问题，就是如果第一个获取锁的线程仍在工作，但是他的expire和时间戳已经过时，
     * 这时就会有别的线程可以获取到锁，解决这个问题i，可以加个守护线程给快要过期的锁续航，当过去了29秒，线程A还没执行完，
     * 该守护线程就会执行expire命令和getset,重置他的时间，为这把锁续航20秒，守护线程从第29秒开始执行，每20秒执行一次，
     * 即使这个节点挂了，由于线程A和守护线程在同一个进程，守护线程也会停下，这把锁到了超时的时候，没人给他续命，也就自动释放了
     *
     * 3.还有一个问题是getset由于是tomcat自己生成时间，所以必须要求分布式下每个tomcat时间必须同步，
     * 然后是这个锁的过期时间可能会被其他进程修改，并且所不具备拥有者标识，所以任何人都可以修改，在jedis1.9版本下可以用threadLocal+LUA脚本解决，
     * private ThreadLocal<String> threadLocal = new ThreadLocal();String s = UUID.randomUUID().toString();threadLocal.set(s);
     *
     * 4.在redis2.6之后，set指令支持过期时间和nx判断，所以不需要时间戳，时间戳可以改成requstId,然后在判断锁标识那里，可以通过Lua脚本判断  因为判断和释放锁是两个操作
     * String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
     *  redisClient.eval(luaScript , Collections.singletonList(key), Collections.singletonList(threadId));
     */
//    @Scheduled(cron = "0 */1 * * * ?") //每一分钟启动一次
//    public void closeOrderTaskV1(){
//        log.info("关闭订单定时任务启动");
//        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time"));
//        orderService.closeOrder(hour);
//        log.info("关闭订单定时任务结束");
//
//    }

   @Scheduled(cron = "0 */1 * * * ?") //每一分钟启动一次
    public void closeOrderTaskV2(){
        log.info("关闭订单定时任务启动");
        //1.首先通过setNx方法判断可不可以加锁，锁的value值是当前时间加一个过期时间
        long outTime = Long.parseLong(PropertiesUtil.getProperty("close.order.out.time"));
        Long lockResult = RedisPoolUtil.setnx(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK, String.valueOf(System.currentTimeMillis() + outTime));
        //2.若可以，则expire修改锁的过期时间，然后执行关闭订单任务,然后删除锁
        if(lockResult.intValue() == 1){
                this.closeOrder();
        }else {
            //3.若不可以
            //3.1先用get获取当前锁的值value1,若为空，则仍然可以setex然后重复第一步，否则返回获取锁失败
            String lockValue1 = RedisPoolUtil.get(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK);
            //3.2若不为空且当前时间>锁的value值
            if(lockValue1 != null && System.currentTimeMillis() > Long.parseLong(lockValue1)){
                //3.2.1重新getset，获取值value2
                //这里是防止死锁的第二个地方，双重判断，但是也会导致一个问题，就是会导致这个锁的过期时间可能会被其他进程修改，但是即使被修改，
                // 也是只能在这5s内的，也还是能接受
                String lockValue2 = RedisPoolUtil.getSet(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK,String.valueOf(System.currentTimeMillis()+outTime));
                //3.2.2若value2==null 或者stringutils.equals两个value
                if(lockValue2 == null || (lockValue2 != null && StringUtils.equals(lockValue1,lockValue2))){
                    //3.2.3执行关闭订单任务
                    this.closeOrder();
                }else{
                    log.info("获取分布式锁失败");
                }
            }else{
                log.info("获取分布式锁失败");
            }
        }
        log.info("关闭订单定时任务结束");
    }


    /**
     * 主从复制，每台机器都有所有数据，主机可读写，从机只可读，适合数据量不太大，请求量中等的情况
       分片存储，每台机器都有部分数据，这样意味着可以存储大量的数据能够承担大量的请求，
       但是有一个缺点就是不能支持批量操作，因为不同的数据分散在不同的实例
     */

    /**
     * redission源码解析，Rlock是接口，有四个子类实现，分别是RedissonLock,RedissonReadLock,RedissonWriteLock,RedissonFairLock,
     * redissoLock在trylock阶段生成threadId，unlock调用，来防止别人误解锁问题，redossLock的trylock如果调用不带释放时间那个函数的话，
     * 它内部就多了异步scheduleExpirationRenewal调度，会异步调用redis的pexpire命令，重置过期时间 ，
     * 只有unlock调用cancelExpirationRenewal()才会停止但是会导致一个问题，就是如果该节点挂了任务没办法自行停止
     *获取锁和释放锁都是lua脚本加上Redis的eval命令实现，所以做好任务超时也设置个守护线程
     */

    /**
     * 为什么用守护线程？ 首先守护线程和用户线程最大的不同就是在于虚拟机的离开，如果用户线程全部退出，java虚拟机也就退出，
     * 所以一般守护线程就是用来为其他对象和线程服务的，这样就能保证当我们这个节点挂了，这个守护线程也就能挂了
     * thread.setDaemon(true)必须在thread.start()之前设置，
     * 否则会跑出一个IllegalThreadStateException异常。你不能把正在运行的常规线程设置为守护线程
     */

    //  @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3(){
        //1.获取到redisson
        Redisson redisson = redissonManager.getRedisson();
        //2.获取到分布式锁
        Boolean booleanLock = Boolean.FALSE;
        RLock lock = redisson.getLock(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK);
        //3.尝试加锁，成功执行业务
        try {
            //这个执行时间有一个坑，如果时间过长，执行时间过短，那么极端的时间内还是会有很多其他服务调用，虽然不是同一时间，但是也会影响性能
            //对于秒杀可以，但对于定时任务不好，所以将其设置为0；
            //lock.trylock语句底层使用了lua语句，能保证原子性
            if(booleanLock = lock.tryLock(2,50, TimeUnit.SECONDS)){
                //3.1执行关闭订单任务
                log.info("获取redisson分布式锁成功，{}，{}",Const.RedisLock.REDIS_CLOSE_ORDER_LOCK,Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time","2"));
               orderService.closeOrder(hour);
            }else {
                log.info("Redisson没有获取到分布式锁:{},ThreadName:{}",Const.RedisLock.REDIS_CLOSE_ORDER_LOCK,Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("获取redisson分布式锁出现异常，{}，{}",Const.RedisLock.REDIS_CLOSE_ORDER_LOCK,Thread.currentThread().getName());
        } finally {

            if(!booleanLock){
                //没有获取到锁直接返回
                return;
            }

            //获取到锁要先释放锁
            lock.unlock();
            log.info("释放redisson分布式锁成功，{}，{}",Const.RedisLock.REDIS_CLOSE_ORDER_LOCK,Thread.currentThread().getName());
        }





    }


    public void closeOrder(){
        //1.修改锁的过期时间  这是防止死锁的第一个措施，不设置过期时间万一系统在这里挂了，那么会导致这个锁一直存在，很容易就导致死锁
        RedisPoolUtil.expire(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK,Const.RedisCacheExtime.REDIS_LOCK_EXTIME);
        //2.加入守护线程
        Thread daemonThread = ThreadUtil.getDaemonThread(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK);
        daemonThread.start();
        //3.执行关闭订单任务
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time","2"));
    //    orderService.closeOrder(hour);
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //关闭守护线程
        ThreadUtil.stopThread();
        //一定也要执行这个interrupt，能将它从sleep中唤醒
        daemonThread.interrupt();
        //4.删除锁
        RedisPoolUtil.del(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK);
    }
}
