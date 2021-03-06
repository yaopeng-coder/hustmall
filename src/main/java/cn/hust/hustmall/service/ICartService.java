package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.vo.CartVO;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-02 16:59
 **/
public interface ICartService {

    ServerResponse<CartVO> addCart(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVO> updateCart(Integer userId, Integer productId,Integer count);
    ServerResponse<CartVO> deleteCart(Integer userId, String productIds);


    ServerResponse<CartVO> list(Integer userId);
    ServerResponse<CartVO> selectOrUnSelect(Integer userId,Integer productId,Integer checked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
