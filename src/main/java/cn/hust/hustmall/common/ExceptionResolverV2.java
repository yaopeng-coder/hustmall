package cn.hust.hustmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-12 16:33
 **/
@ControllerAdvice
@Component
@Slf4j
public class ExceptionResolverV2 {

    /**
     * 用注解的方式来实现全局异常处理
     * @param e
     * @param request
     * @param response
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ServerResponse resolveException(Exception e, HttpServletRequest request, HttpServletResponse response){
        log.error("发生异常的URL:{},exception",request.getRequestURI(),e);
        String errorMsg = "接口异常，请查看服务端日志信息";
        String responseData = e.toString();
        return ServerResponse.createByError(errorMsg,responseData);

    }
}
