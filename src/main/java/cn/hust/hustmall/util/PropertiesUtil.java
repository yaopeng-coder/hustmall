package cn.hust.hustmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 11:30
 **/
public class PropertiesUtil {

    private static Logger  logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties pros;

    static{
        String fileName = "mmall.properties";
        pros = new Properties();
        try {
            pros.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"utf-8"));
        } catch (IOException e) {
            logger.error("读取配置文件错误，{}",e);
        }
    }

    public static String getProperty(String key){
        //key.trim()是为了消除不必要的空格，以免造成错误
        String value = pros.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    public static String getProperty(String key,String defaultValue){
        //key.trim()是为了消除不必要的空格，以免造成错误
        String value = pros.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return defaultValue;
        }
        return value.trim();
    }






}
