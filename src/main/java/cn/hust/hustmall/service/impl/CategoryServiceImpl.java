package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dao.CategoryMapper;
import cn.hust.hustmall.pojo.Category;
import cn.hust.hustmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 产品类别
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-31 15:36
 **/
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加品类
     * @param parentId
     * @param categoryName
     * @return
     */
    public ServerResponse<String> addCategory(Integer parentId, String categoryName){

        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加分类错误，parentId或者categoryName不能为空");
        }

        Category category = new Category();
        category.setStatus(true);
        category.setParentId(parentId);
        category.setName(categoryName);

        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0 ){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");

    }


    /**
     * 更新品类名字
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse<String> updateCategotyName(Integer categoryId, String categoryName){
       /* 这种方法会查询两次数据库
       Category category = categoryMapper.selectByPrimaryKey(categotyId);
        if(category == null){
            return ServerResponse.createByErrorMessage("更新品类名字失败");
        }
        category.setName(categoryName);*/

        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新分类错误，categoryId或者categoryName不能为空");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0 ){
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createBySuccessMessage("更新品类名字失败");
    }

}
