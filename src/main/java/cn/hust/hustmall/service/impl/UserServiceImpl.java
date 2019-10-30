package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dao.UserMapper;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        // todo Md5加密

        User user = userMapper.selectLogin(username, password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误，请重新输入");
        }

        //为了避免用户密码被泄露，即使加密，也要置为empty
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);


    }
}
