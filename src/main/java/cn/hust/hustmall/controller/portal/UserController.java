package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
        @RequestMapping(value = "login.do",method = RequestMethod.POST)
        @ResponseBody
        public ServerResponse<User> login(String username, String password, HttpSession session){

            ServerResponse<User> response = iUserService.login(username, password);
            if(response.isSuccess()){
                session.setAttribute(Const.GCURRENT_USER,response.getData());
            }
            return  response;

        }





}
