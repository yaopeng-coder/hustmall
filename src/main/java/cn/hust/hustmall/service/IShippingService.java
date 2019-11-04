package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.Shipping;
import com.github.pagehelper.PageInfo;

import java.util.Map;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-04 10:53
 **/
public interface IShippingService {

    ServerResponse<Map> addAddress(Integer userId, Shipping shipping);
    ServerResponse delAddress(Integer userID, Integer shippingId);
    ServerResponse updateAddress(Integer userId, Shipping shipping);
    ServerResponse<Shipping> selectAddress(Integer userId, Integer shippingId);
    ServerResponse<PageInfo> listAddress(Integer userId, Integer pageNum, Integer pageSize);
}
