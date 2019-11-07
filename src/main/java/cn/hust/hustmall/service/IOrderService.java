package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.vo.OrderProductVO;
import cn.hust.hustmall.vo.OrderVO;

import java.util.Map;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-05 09:48
 **/
public interface IOrderService {
    ServerResponse<Map> pay(Integer userId, Long orderNo, String path);
    ServerResponse<String> aliPayBack(Map<String,String> params );
    ServerResponse<Boolean> queryOrderPayStatus(Long orderNo,Integer userId);

    ServerResponse<OrderVO> createOrder(Integer userId, Integer shippingId);

    ServerResponse<String> cancel(Integer userId, Long orderNo);

    ServerResponse<OrderProductVO> getOrderCartProduct(Integer userId);
}
