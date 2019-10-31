package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.Category;

import java.util.List;
import java.util.Set;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-31 15:36
 **/
public interface ICategoryService {

    ServerResponse<String> addCategory(Integer parentId, String categoryName);
    ServerResponse<String> updateCategotyName(Integer categoryId, String categoryName);
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer parentId);
    ServerResponse<List<Integer>> getCategoryAndDeepCategoryById(Integer categoryId);
    Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId);
}
