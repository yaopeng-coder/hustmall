package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;

import java.util.Map;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-05 09:48
 **/
public interface IOrderService {
    ServerResponse<Map> pay(Integer userId, Long orderNo, String path);
}
