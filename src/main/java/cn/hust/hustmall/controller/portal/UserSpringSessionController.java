package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IUserService;
import cn.hust.hustmall.util.CookieUtil;
import cn.hust.hustmall.util.JsonUtil;
import cn.hust.hustmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-29 17:28
 **/

@Controller
@RequestMapping("/user/springsession")
public class UserSpringSessionController {

        @Autowired
        private IUserService iUserService;

        /**
         * 实现用户登录功能
         * @param username
         * @param password
         * @param session
         * @return
         */
        @RequestMapping(value = "/login.do")
        @ResponseBody
        public ServerResponse<User> login(String username, String password, HttpSession session,
                                           HttpServletResponse httpServletResponse){

            ServerResponse<User> response = iUserService.login(username, password);
            if(response.isSuccess()){

                //必须要将user序列化，否则存入不到redis中去
     //         session.setAttribute(Const.CURRENT_USER,response.getData());
                //写进cookie
                CookieUtil.writeLoginToken(httpServletResponse,session.getId());
                //用户信息保存到redis
                RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }
            return  response;
        }

        /**
         * 用户登出
         * @param
         * @return
         */
        @RequestMapping(value = "/logout.do")
        @ResponseBody
        public ServerResponse<String> logout(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){

            //注意登出时，他只是删除了redis中springsession中关于用户信息的一部分，没有删除浏览器中的cookie
    //        session.removeAttribute(Const.CURRENT_USER);

            //删除cookie
            CookieUtil.delLoginCookie(httpServletRequest,httpServletResponse);
            //删除redis中的数据
            String token = CookieUtil.readLoginCookie(httpServletRequest);
            RedisShardedPoolUtil.del(token);
            return ServerResponse.createBySuccessMessage("退出成功");
        }


        /**
         *得到用户信息
         * @param
         * @return
         */
        @RequestMapping(value = "/get_user_info.do")
        @ResponseBody
        public ServerResponse<User> getUserInfo(HttpServletRequest request){
         //  User user = (User)session.getAttribute(Const.CURRENT_USER);
            String token = CookieUtil.readLoginCookie(request);
            if(StringUtils.isBlank(token)){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
            }
            String userJsonString = RedisShardedPoolUtil.get(token);
            User user = JsonUtil.string2Object(userJsonString, User.class);
            if(user != null){
                return ServerResponse.createBySuccess(user);
            }
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }






}
