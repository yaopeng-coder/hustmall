package cn.hust.hustmall.common;

import cn.hust.hustmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-10 09:29
 **/
public class RedisPool {

    private static JedisPool jedisPool ;  //jedis连接池，从中可以取出Jedis，是redis的客户端，可以用来操作redis
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total"));   //连接池最大Jedis实例个数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle"));      //连接池中最大空闲jedis个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle"));      //连接池中最小空闲Jedis个数
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow")); //从连接池中取出Jedis时是否需要测试
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return")); //将Jedis放回连接池是否需要测试

    private static String  redisIp = PropertiesUtil.getProperty("redis.ip");     //redis服务端的ip
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));   //redis服务端的端口


    private static void initPool(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setTestOnBorrow(testOnBorrow);
        poolConfig.setTestOnReturn(testOnReturn);
        jedisPool = new JedisPool(poolConfig,redisIp,redisPort,1000*2);
    }

    static {
        initPool();
    }

    public static Jedis getResource(){
        return jedisPool.getResource();
    }

    public static void returnResource(Jedis jedis){
        jedisPool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis){
        jedisPool.returnBrokenResource(jedis);
    }


    public static void main(String[] args){

        Jedis jedis = getResource();
        jedis.set("name","yaopeng");
        returnResource(jedis);
        jedis.set("hello","hello");
        String s = jedis.get("hello");
        System.out.println(s);
        jedisPool.close();


    }

}
