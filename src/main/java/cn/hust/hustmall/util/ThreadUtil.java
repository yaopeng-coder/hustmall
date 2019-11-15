package cn.hust.hustmall.util;

import cn.hust.hustmall.common.Const;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-14 16:37
 **/
@Slf4j
public class ThreadUtil {


    //单例模式实现守护线程
    private static volatile Thread thread = null;


    public static Thread getDaemonThread(String key){
//        if(thread == null){
//            synchronized (ThreadUtil.class){
//                if(thread == null) {
//                    //必须声明成静态内部类，否则无法new
//                    thread = new Thread(new ServivalProcessor(key));
//                    thread.setName("redisLockDaemonThread");
//                    thread.setDaemon(true);
//                }
//            }
//        }

        //不能设计成单例模式，同一个线程调用两次start，会产生java.lang.IllegalThreadStateException，查看Thread的start方法即可知
        //线程状态不为0，即抛出该异常
        thread = new Thread(new ServivalProcessor(key));
        thread.setName("redisLockDaemonThread");
        thread.setDaemon(true);
        log.info("线程创建成功");
        return thread;

    }

//    public static void stopThread(){
//        ServivalProcessor.stop();
//    }

    //必须声明成静态内部类，否则无法new
    static class ServivalProcessor implements Runnable{

        private String key;

        //线程关闭的标记
        private  Boolean signal = Boolean.TRUE;


       /* static void stop() {
            signal = Boolean.FALSE;
        }*/

        public ServivalProcessor(String key) {
            this.key = key;
        }

        @Override
        public void run() {
            log.info(String.valueOf(signal));

            //将waitTime设置为Math.max(1, lockTime * 2 / 3)，即守护线程许需要等待waitTime后才可以去重新设置锁的超时时间，避免了资源的浪费
            int waitTime = Const.RedisCacheExtime.REDIS_LOCK_EXTIME * 1000 * 2 / 3;

            while (signal){
                log.info("守护线程即将启动");
                try {

                    log.info("守护线程睡眠{}",waitTime);
                    Thread.sleep(waitTime);
                    log.info("守护线程开始续时，{}",Thread.currentThread().getName());
                    RedisPoolUtil.expire(key,Const.RedisCacheExtime.REDIS_LOCK_EXTIME);
                } catch (InterruptedException e) {
                    log.info("守护线程要停止啦，不用续时啦,{}",Thread.currentThread().getName());
                    signal = Boolean.FALSE;
                    return;
                }
            }

        }
    }

}
