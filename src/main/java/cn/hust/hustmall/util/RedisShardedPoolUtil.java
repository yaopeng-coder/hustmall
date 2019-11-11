package cn.hust.hustmall.util;

import cn.hust.hustmall.common.RedisShardedPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-10 10:29
 **/
@Slf4j
public class RedisShardedPoolUtil {


    /**
     * string类型set函数封装
     * @param key
     * @param value
     * @return
     */
    public static String set(String key, String value){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("set,jedis:{} ,result:{},error",jedis,result,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String setEx(String key, String value, Integer seconds){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.setex(key,seconds,value);
        } catch (Exception e) {
            log.error("setEx,jedis:{} ,result:{},error",jedis,result,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get,jedis:{} ,result:{},error",jedis,result,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }


    public static Long expire(String key,Integer seconds){
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.expire(key,seconds);
        } catch (Exception e) {
            log.error("expire,jedis:{} ,result:{},error",jedis,result,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }


    public static Long del(String key){
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del,jedis:{} ,result:{},error",jedis,result,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }




}
