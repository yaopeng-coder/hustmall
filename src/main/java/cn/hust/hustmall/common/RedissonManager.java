package cn.hust.hustmall.common;

import cn.hust.hustmall.util.PropertiesUtil;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-15 10:21
 **/


@Component
public class RedissonManager {

    private  Config config;

    private  Redisson redisson;

    private static String  redis1Ip = PropertiesUtil.getProperty("redis1.ip");     //redis服务端的ip
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));   //redis服务端的端口

    private static String  redis2Ip = PropertiesUtil.getProperty("redis2.ip");     //redis服务端的ip
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));   //redis服务端的端口



    //区别于RedisShardPool配置，这里用了@compoment容器注入的方式，所以所有的方法都不用static修饰，因为外部引用时都先@autowird
    //这个注解在外部调用@autowird引用的bean之前调用，和@compoment配合使用
    @PostConstruct
    private  void init(){
        config = new Config();
        config.useSingleServer().setAddress(new StringBuilder().append(redis1Ip).append(":").append(String.valueOf(redis1Port)).toString());
        redisson = (Redisson) Redisson.create(config);
    }

    public  Redisson getRedisson() {
        return redisson;
    }
}
