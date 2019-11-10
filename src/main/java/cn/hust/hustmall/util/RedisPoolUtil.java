package cn.hust.hustmall.util;

import cn.hust.hustmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-10 10:29
 **/
@Slf4j
public class RedisPoolUtil {


    /**
     * string类型set函数封装
     * @param key
     * @param value
     * @return
     */
    public static String set(String key, String value){
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getResource();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("set,jedis:{} ,result:{},error",jedis,result,e);
            RedisPool.returnBrokenResource(jedis);
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String setEx(String key, String value, Integer seconds){
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getResource();
            result = jedis.setex(key,seconds,value);
        } catch (Exception e) {
            log.error("setEx,jedis:{} ,result:{},error",jedis,result,e);
            RedisPool.returnBrokenResource(jedis);
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get,jedis:{} ,result:{},error",jedis,result,e);
            RedisPool.returnBrokenResource(jedis);
        }
        RedisPool.returnResource(jedis);
        return result;
    }


    public static Long expire(String key,Integer seconds){
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getResource();
            result = jedis.expire(key,seconds);
        } catch (Exception e) {
            log.error("expire,jedis:{} ,result:{},error",jedis,result,e);
            RedisPool.returnBrokenResource(jedis);
        }
        RedisPool.returnResource(jedis);
        return result;
    }


    public static Long del(String key){
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del,jedis:{} ,result:{},error",jedis,result,e);
            RedisPool.returnBrokenResource(jedis);
        }
        RedisPool.returnResource(jedis);
        return result;
    }




}
