package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.common.TokenCache;
import cn.hust.hustmall.dao.UserMapper;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IUserService;
import cn.hust.hustmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-29 21:01
 **/
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        if( userMapper.checkUsername(username) == 0 ){
            return  ServerResponse.createByErrorMessage("用户名不存在");
        }

        //用加密后的密码验证
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误，请重新输入");
        }

        //为了避免用户密码被泄露，即使加密，也要置为empty
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);


    }

    @Override
    public ServerResponse<String> register(User user) {

        ServerResponse<String> response = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!response.isSuccess()){
            return response;
        }


        response = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!response.isSuccess()){
            return response;
        }

        //设置用户角色
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return  ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccess("注册成功");

    }


    //校验用户名和email
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                if(userMapper.checkUsername(str) > 0 ){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }

            if(Const.EMAIL.equals(type)){
                if(userMapper.checkEmail(str) >  0 ){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccess("检验成功");
    }


    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        ServerResponse response = this.checkValid(username,Const.USERNAME);
        if(response.isSuccess()){
            return ServerResponse.createByErrorMessage("该用户不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isBlank(question)){
            return ServerResponse.createByErrorMessage("找回密码问题为空");
        }

        return ServerResponse.createBySuccess(question);
    }


    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount > 0){
            //说明答案检验通过
            String forgetToken  = UUID.randomUUID().toString();
            TokenCache.setKey("token_"+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }


    public ServerResponse<String> forgerRestPassword(String username,String passwordNew, String forgetToken) {
        //1.检查用户名
        ServerResponse<String> checkValid = this.checkValid(username, Const.USERNAME);
        if (checkValid.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        //2.检查forgetToken
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("token不能为空");
        }

        //3.查看缓存token是否已经失效
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        //4.判断forgetToken是否与缓存token相等
        //4.1若相等，则更新数据库
        if (StringUtils.equals(token, forgetToken)) {
            String md5password = MD5Util.MD5EncodeUtf8(passwordNew);
            int count = userMapper.updatePasswordByUsername(username, md5password);
            //一定要判断是否大于0,可能会出现修改失败的情况
            if (count > 0) {
                return ServerResponse.createBySuccess("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
            return ServerResponse.createByErrorMessage("修改密码失败");
    }
}
