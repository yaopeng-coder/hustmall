package cn.hust.hustmall.common;

import lombok.Getter;

/**
 * 类似之前springboot项目中的resultEnum类
 */
@Getter
public enum ResponseCode {
     SUCCESS(0,"SUCCESS"),
     ERROR(1,"ERROR"),
     NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");


    private final int code;
    private final  String desc;


    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
