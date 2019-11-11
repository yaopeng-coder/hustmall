package cn.hust.hustmall.controller.common;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.util.CookieUtil;
import cn.hust.hustmall.util.JsonUtil;
import cn.hust.hustmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-10 19:30
 **/
public class SessionExpireFilter implements Filter{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        //1.读取cookie,若不为空，取出token
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        if(StringUtils.isNotBlank(loginToken)){
            //2.根据token去redis中取值，若返回的userJson反序列化后不为空，则更新session时间
            String userJson = RedisShardedPoolUtil.get(loginToken);
            User user = JsonUtil.string2Object(userJson, User.class);
            if(user != null){
                RedisShardedPoolUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }

        }
        chain.doFilter(request,response);

    }

    @Override
    public void destroy() {

    }
}
