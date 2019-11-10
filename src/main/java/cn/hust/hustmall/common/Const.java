package cn.hust.hustmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-30 08:46
 **/
public class Const {

    public  static final String CURRENT_USER = "CURRENT_USER";
    public  static final String USERNAME = "username";
    public  static final String EMAIL = "email";

    public  static final Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");

    public interface RedisCacheExtime{
        int REDIS_SESSION_EXTIME = 60 * 30; //30分钟
    }

    public interface Cart{
        int CHECKED = 1;//选中
        int UN_CHECKED = 0;//未选中
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }


    public interface Role{
       int  ROLE_CUSTOMER =  0; //0代表普通用户
       int  ROLE_ADMIN = 1; //1代表管理者
    }

    public enum productStatus{
       ON_SALE(1,"上架"),
       OFF_SALE(0,"下架");

        int  code;
        String  msg;

        productStatus(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

       public  int getCode(){
           return this.code;
        }

       public  String getMsg(){
            return this.msg;
        }


    }

    public enum OrderStatusEnum{

        CANCEL(0,"已取消"),
        NOT_PAY(10,"未支付"),
        PAY(20,"已支付"),
        SHIPPED(30,"已发货"),
        FINISHED(40,"已完成"),
        CLOSED(50,"已关闭");
        int  code;
        String  msg;

        public static OrderStatusEnum byCodeOf(Integer code){

            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.code == code){
                    return orderStatusEnum;
                }
            }
            return null;
        }

        OrderStatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public  int getCode(){
            return this.code;
        }

        public  String getMsg(){
            return this.msg;
        }
    }

    public interface AlipayCallback{
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAIL = "fail";
    }



    public enum PayPlatformEnum{

        ALI_PAY(1,"支付宝");
        int  code;
        String  msg;

        PayPlatformEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public  int getCode(){
            return this.code;
        }
        public  String getMsg(){
            return this.msg;
        }
    }

    public enum PaymentTypeEnum{

        ON_LINE(1,"线上支付"),
        ;
        int  code;
        String  msg;

        PaymentTypeEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public  int getCode(){
            return this.code;
        }
        public  String getMsg(){
            return this.msg;
        }


        public static PaymentTypeEnum byCodeOf(Integer code){

            for(PaymentTypeEnum paymentTypeEnum : values()){
                if(paymentTypeEnum.code == code){
                    return paymentTypeEnum;
                }
            }
            return null;
        }
    }
}
