package cn.hust.hustmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-10 15:37
 **/
@Slf4j
public class CookieUtil {

    private static final String COOKIE_NAME = "hustmall_login_token";
    private static final String COOKIE_DOMAIN = "www.hustmall.com";


    /**
     * 写cookie
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(COOKIE_NAME,token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/"); //代表设置在根目录
        cookie.setHttpOnly(true); //防止一些脚本攻击

        //不设置setMaxAge,那么cookie不会被写到硬盘里，只会存在于内存里，只在当前页面有效，单位是秒,-1代表永久
        cookie.setMaxAge(-1);
        log.info("写入cookie的名字：{}，写入cookie的值{}",cookie.getName(),cookie.getValue());

        response.addCookie(cookie);
    }


    /**
     * 读取cookie的值
     * @param request
     * @return
     */
    public static String readLoginCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    log.info("返回cookie的名字：{}，返回cookie的值{}",cookie.getName(),cookie.getValue());
                    return cookie.getValue();

                }
            }
        }
        return null;
    }

    public static void delLoginCookie(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if(StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                cookie.setDomain(COOKIE_DOMAIN);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
                log.info("删除时cookie的名字：{}，删除时cookie的值{}",cookie.getName(),cookie.getValue());
                return;
            }
        }
    }
}
