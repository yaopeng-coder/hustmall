package cn.hust.hustmall.handle;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.exception.BackendAuthorityException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-13 11:12
 **/
@ControllerAdvice
@RestController
@Component
public class BackendAuthorityExceptionHandler {

    @ExceptionHandler(BackendAuthorityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ServerResponse authorityExceptionHandler(){
            return ServerResponse.createByErrorMessage("未登录或者不是管理员");
    }
}
