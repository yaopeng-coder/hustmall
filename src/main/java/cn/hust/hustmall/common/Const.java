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
}
