package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-31 15:36
 **/
public interface ICategoryService {

    ServerResponse<String> addCategory(Integer parentId, String categoryName);
    ServerResponse<String> updateCategotyName(Integer categoryId, String categoryName);
}
