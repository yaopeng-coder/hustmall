package cn.hust.hustmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 11:51
 **/
public class DateTimeUtil {

    public static final String STANDARD_FORMAT  = "yyyy-MM-dd HH:mm:ss";


    /**
     * 为什么用joda.time不用simpleDataFormat,后者JDK默认的有多线程问题
     * @param date
     * @param formatString
     * @return
     */

    public static String dateToStr(Date date,String formatString){

        //1.首先判断非空
        if(date == null){
            return StringUtils.EMPTY;
        }
        //2.首先把date转换成datetime
        DateTime dateTime = new DateTime(date);

        //3.根据转换格式将datetime转换成string
        return dateTime.toString(formatString);

    }

    public static Date strToDate(String datetimeStr, String formatString){

        //1.首先构造dateTimeFormatter,格式化string
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatString);

        //2.将string转换成datetime,parseDateTime()函数有一个非空判断，若datetimeStr为空抛异常
        DateTime dateTime = dateTimeFormatter.parseDateTime(datetimeStr);

        //3.将datetime转换成date
        return dateTime.toDate();

    }

    public static String dateToStr(Date date){

        //1.首先判断非空
        if(date == null){
            return StringUtils.EMPTY;
        }
        //2.首先把date转换成datetime
        DateTime dateTime = new DateTime(date);

        //3.根据转换格式将datetime转换成string
        return dateTime.toString(STANDARD_FORMAT );

    }

    public static Date strToDate(String datetimeStr){

        //1.首先构造dateTimeFormatter,格式化string
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT );

        //2.将string转换成datetime
        DateTime dateTime = dateTimeFormatter.parseDateTime(datetimeStr);

        //3.将datetime转换成date
        return dateTime.toDate();

    }
}
