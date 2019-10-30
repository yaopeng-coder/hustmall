package cn.hust.hustmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-30 18:47
 **/
@Slf4j
public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public  static String TOKEN_PREFIX = "token_";


    //LRU算法，最大缓存容量为10000，当超过时，利用LRU最少使用原则删除一些缓存
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000)
            .maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
                @Override
                //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                public String load(String s) throws Exception {
                    return "null";
                }
            });


    public static void setKey(String key, String value){
        localCache.put(key,value);
    }

    public static String getKey(String key){

        try {
            String value = localCache.get(key);
            if(value.equals("null")){
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            e.printStackTrace();
            log.error("localCache has  error,{}",e.getMessage());
            logger.error("localCache has  error,{}",e.getMessage());
        }

        return null;


    }



}
