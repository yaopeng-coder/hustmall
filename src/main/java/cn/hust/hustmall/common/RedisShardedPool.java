package cn.hust.hustmall.common;

import cn.hust.hustmall.util.PropertiesUtil;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-10 09:29
 **/
public class RedisShardedPool {

    //分片的Jedis连接池
    private static ShardedJedisPool shardedJedisPool ;  //jedis连接池，从中可以取出Jedis，是redis的客户端，可以用来操作redis
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));   //连接池最大Jedis实例个数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));      //连接池中最大空闲jedis个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));      //连接池中最小空闲Jedis个数
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow")); //从连接池中取出Jedis时是否需要测试
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return")); //将Jedis放回连接池是否需要测试

    private static String  redis1Ip = PropertiesUtil.getProperty("redis1.ip");     //redis服务端的ip
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));   //redis服务端的端口

    private static String  redis2Ip = PropertiesUtil.getProperty("redis2.ip");     //redis服务端的ip
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));   //redis服务端的端口


    private static void initPool(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);

        poolConfig.setTestOnBorrow(testOnBorrow);
        poolConfig.setTestOnReturn(testOnReturn);

        //连接耗尽时是否阻塞，FALSE会抛出异常，true阻塞直到超时，默认为true
        poolConfig.setBlockWhenExhausted(true);


        JedisShardInfo shardInfo1 = new JedisShardInfo(redis1Ip,redis1Port,2*1000);
        JedisShardInfo shardInfo2 = new JedisShardInfo(redis2Ip,redis2Port,2*1000);

        List<JedisShardInfo> shardInfoList = new ArrayList<JedisShardInfo>(2);
        shardInfoList.add(shardInfo1);
        shardInfoList.add(shardInfo2);

        //Redis服务器分区划分：将每台服务器服务器采用哈希算法划分为160个虚拟实例
        //sharded采用的哈希算法：MD5和MurmurHash类型；这里采用64位的MurmurHash算法，即一致性分布式hash算法
        //提供基于Key的划分方法；提供了ShardKeyTag实现
        //springsession 和redission不支持分布式redis
        shardedJedisPool = new ShardedJedisPool(poolConfig,shardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    public static ShardedJedis getResource(){
        return shardedJedisPool.getResource();
    }

    public static void returnResource(ShardedJedis jedis){
        shardedJedisPool.returnResource(jedis);
    }

    public static void returnBrokenResource(ShardedJedis jedis){
        shardedJedisPool.returnBrokenResource(jedis);
    }


    //测试
    public static void main(String[] args){

        ShardedJedis jedis = getResource();


        for(int i = 0;i<10;i++){
            jedis.set("key"+i,"key"+i);
        }


        returnResource(jedis);

        shardedJedisPool.close();


    }

}
