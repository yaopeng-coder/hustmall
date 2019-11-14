package cn.hust.hustmall.task;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.service.IOrderService;
import cn.hust.hustmall.util.PropertiesUtil;
import cn.hust.hustmall.util.RedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    /**
     * 1.这里使用redis分布式锁+spring schedule实现定时关单，因为我们只希望有一个tomcat在执行这个关单任务即可，不需要都来执行，会浪费数据库
     * 的性能，而且你不停的执行sql语句，有可能会产生错乱，另外不能只在一个tomcat中加入定时任务的代码，会存在单点故障的问题，所以要做成分布式锁
     * 2.如果一个任务执行时间比较长，结果锁已经失效了，那么就会有另一个线程获取锁执行任务， 就会产生两个线程同时都可以执行该任务，所以这里的锁的时间要根据你的定时任务调整，
     *  课程里也有说，所以是一个太极的过程，不可能设置好千秋不变的。技术架构就是要随着业务变化不断变化
     * ~~设置的锁的时间一定要保证足够任务执行完。所以平时评估网站的qps等还是非常有必要的，也可以观察日志去看任务执行的时间然后来判断
     * 3.我们为了防止死锁，加入expire和时间戳，但也会导致一个问题，就是如果第一个获取锁的线程仍在工作，但是他的expire和时间戳已经过时，
     * 这时就会有别的线程可以获取到锁，该怎么解决
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

    public void closeOrder(){
        //1.修改锁的过期时间  这是防止死锁的第一个措施，不设置过期时间万一系统在这里挂了，那么会导致这个锁一直存在，很容易就导致死锁
        RedisPoolUtil.expire(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK,5);
        //2.执行关闭订单任务
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time","2"));
        orderService.closeOrder(hour);
        //3.删除锁
        RedisPoolUtil.del(Const.RedisLock.REDIS_CLOSE_ORDER_LOCK);
    }
}
