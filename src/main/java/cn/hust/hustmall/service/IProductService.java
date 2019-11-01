package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.Product;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 09:45
 **/
public interface IProductService {

    ServerResponse<String> saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);
}
