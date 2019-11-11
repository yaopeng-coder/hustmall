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
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("/user")
public class UserController {

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

          //      session.setAttribute(Const.CURRENT_USER,response.getData());

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
           // session.removeAttribute(Const.CURRENT_USER);

            //删除cookie
            CookieUtil.delLoginCookie(httpServletRequest,httpServletResponse);
            //删除redis中的数据
            String token = CookieUtil.readLoginCookie(httpServletRequest);
            RedisShardedPoolUtil.del(token);
            return ServerResponse.createBySuccessMessage("退出成功");
        }

        /**
         * 用户注册
         * @param user
         * @return
         */

        @RequestMapping(value = "/register.do",method = RequestMethod.POST)
        @ResponseBody
        public ServerResponse<String> register(User user){
            ServerResponse<String> response = iUserService.register(user);
            return response;
        }

        /**
         * 用户注册时实时校验，例如填完username到下一个输入框时，就校验你的username对不对
         * @param str
         * @param type
         * @return
         */
        @RequestMapping(value = "/check_valid.do",method = RequestMethod.POST)
        @ResponseBody
        public ServerResponse<String> checkValid(String str, String type){
            return iUserService.checkValid(str,type);
        }

        /**
         *得到用户信息
         * @param
         * @return
         */
        @RequestMapping(value = "/get_user_info.do",method = RequestMethod.POST)
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

        /**
         *忘记密码之提示问题
         * @param username
         * @return
         */
        @RequestMapping(value = "/forget_get_question.do",method = RequestMethod.POST)
        @ResponseBody
        public ServerResponse<String> forgetGetQuestion(String username){
            return iUserService.selectQuestion(username);
        }

    /**
     * 忘记密码之检查用户答案，正确后在本地缓存生成token
     * @param username
     * @param question
     * @param answer
     * @return
     */
        @RequestMapping(value = "/forget_check_answer.do")
        @ResponseBody
        public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){
           return  iUserService.checkAnswer(username,question,answer);
        }


    /**
     * 忘记密码之重置密码，带token可以防止横向越权
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
        @RequestMapping(value = "/forget_reset_password.do")
        @ResponseBody
        public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken ){
            return  iUserService.forgerRestPassword(username,passwordNew,forgetToken);
        }

    /**
     * @param passwordOld
     * @param passwordNew
     * @param
     * @return
     */
        @RequestMapping(value = "/reset_password.do")
        @ResponseBody
        public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, HttpServletRequest request ){

               //1.检查用户是否登录
         //       User user = (User)session.getAttribute(Const.CURRENT_USER);
            String token = CookieUtil.readLoginCookie(request);
            if(StringUtils.isBlank(token)){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
            }
            String userJsonString = RedisShardedPoolUtil.get(token);
            User user = JsonUtil.string2Object(userJsonString, User.class);
            if(user == null){
                return ServerResponse.createByErrorMessage("用户未登录");
            }

            //2.重置密码
            return iUserService.resetPassword(passwordOld,passwordNew,user);
        }

        /**
         *更新用户个人信息功能
         * @param user
         * @param session
         * @return
         */

        @RequestMapping(value = "/update_information.do",method = RequestMethod.POST)
        @ResponseBody
        public ServerResponse<User> updateInfomation(User user, HttpServletRequest request,HttpSession session ){
                //1.判断当前用户是否已登录
          //   User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
            String token = CookieUtil.readLoginCookie(request);
            if(StringUtils.isBlank(token)){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
            }
            String userJsonString = RedisShardedPoolUtil.get(token);
            User currentUser = JsonUtil.string2Object(userJsonString, User.class);

             if(currentUser == null){
                 return ServerResponse.createByErrorMessage("用户未登录");
             }

            //2.为了防止横行越权，前端传过来的user只有email，phone，question和answer
                //id必须从session里取，否则会容易导致横向越权,用户名也不能修改
            currentUser.setId(user.getId());
            currentUser.setUsername(user.getUsername());
            ServerResponse<User> response = iUserService.updateInfomation(currentUser);
            if(response.isSuccess()){
                //3.更新信息，若成功则更新session里的user
                response.getData().setUsername(user.getUsername());
              //  session.setAttribute(Const.CURRENT_USER,response.getData());
                RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);

            }

            return response;

        }

        /**
         * 得到个人用户信息
         *在修改信息时，先调用这个接口，判断用户是否登录，未登录强制登陆，能得到个人信息界面，才方便修改信息
         * @return
         */
        @RequestMapping(value = "/get_information.do")
        @ResponseBody
        public ServerResponse<User> getInformation( HttpServletRequest request ){

            //1.判断当前用户是否登陆
       //     User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
            String token = CookieUtil.readLoginCookie(request);
            if(StringUtils.isBlank(token)){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
            }
            String userJsonString = RedisShardedPoolUtil.get(token);
            User user = JsonUtil.string2Object(userJsonString, User.class);

            if(user == null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
            }
            //2.登陆则传回用户信息

            return iUserService.getInfomation(user.getId());
        }



}
