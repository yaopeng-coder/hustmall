package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-29 21:00
 **/
public interface IUserService {

    ServerResponse<User> login(String username, String password);
}
