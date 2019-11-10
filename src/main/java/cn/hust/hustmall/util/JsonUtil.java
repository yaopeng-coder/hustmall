package cn.hust.hustmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 序列化和反序列化
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-10 11:08
 **/
@Slf4j
public class JsonUtil {

    //使用单例模式
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象的全部字段引入
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        //取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);

        //忽略空Bean转换json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);

        //所有日期转换成下面格式
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //反序列化配置 忽略在Json字符串中存在，在java对象中不存在对应属性的情况
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    //对象转json
    public static  <T> String obj2String(T obj){

        if(obj == null){
            return null;
        }

        try {
            return obj instanceof String ? (String)obj : objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse Object to String error",e);
        }
        return null;
    }

    //对象转json,默认换行等等，格式很整齐
    public static  <T> String obj2StringPretty(T obj){

        if(obj == null){
            return null;
        }

        try {
            return obj instanceof String ? (String)obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse Object to String error",e);
        }
        return null;
    }

    //json字符串转对象
    public static <T> T string2Object(String str , Class<T> clazz){
        if(StringUtils.isBlank(str) && clazz == null){
            return null;
        }

        try {
            return clazz.equals(String.class) ? (T)str : objectMapper.readValue(str,clazz);
        } catch (IOException e) {
            log.warn("Parse String to Object error",e);
        }
        return null;
    }

    //json字符串转集合类复杂对象,通过typeRefence
    public static <T> T string2Object(String str, TypeReference<T> typeReference){
        if(StringUtils.isBlank(str) && typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str,typeReference));
        } catch (IOException e) {
            log.warn("Parse String to Object error",e);
        }
        return null;
    }

    //json字符串转集合类复杂对象,通过javaType
    public static <T> T string2Object(String str, Class<?> collectionClass,Class<?>... elementClasses){

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (IOException e) {
            log.warn("Parse String to Object error",e);
        }
        return null;
    }





}
