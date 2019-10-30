package cn.hust.hustmall.common;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-30 08:46
 **/
public class Const {

    public  static final String GCURRENT_USER = "GCURRENT_USER";
    public  static final String USERNAME = "username";
    public  static final String EMAIL = "email";


    public interface Role{
       int  ROLE_CUSTOMER =  0; //0代表普通用户
       int  ROLE_ADMIN = 1; //1代表管理者
    }
}
