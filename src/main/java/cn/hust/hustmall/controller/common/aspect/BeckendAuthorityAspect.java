package cn.hust.hustmall.controller.common.aspect;

import cn.hust.hustmall.exception.BackendAuthorityException;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.util.CookieUtil;
import cn.hust.hustmall.util.JsonUtil;
import cn.hust.hustmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-13 10:30
 **/
@Aspect
@Slf4j
@Component
public class BeckendAuthorityAspect {


    @Pointcut("execution(public * cn.hust.hustmall.controller.backend.*.*(..))")
    public void verify(){
    }

    @Before("verify()")
    public void doVerify(){

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        //解析请求参数，类似拦截器

        User user = null;

        String loginToken = CookieUtil.readLoginCookie(request);
        if(loginToken != null){
            String loginJsonUser = RedisShardedPoolUtil.get(loginToken);
            user =JsonUtil.string2Object(loginJsonUser, User.class);
        }

        if(user == null){
            throw  new BackendAuthorityException();
        }

    }


}
