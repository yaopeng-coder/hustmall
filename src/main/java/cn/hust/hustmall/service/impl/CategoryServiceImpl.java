package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dao.CategoryMapper;
import cn.hust.hustmall.pojo.Category;
import cn.hust.hustmall.service.ICategoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * 产品类别
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-31 15:36
 **/
@Service("iCategoryService")
@Slf4j
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


    /**
     * 查询子节点
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){

        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            log.info("该类别下没有子节点,{}",categoryId);
            return ServerResponse.createByErrorMessage("未找到该品类");
        }

        return ServerResponse.createBySuccess(categoryList);

    }

    /**
     * 得到当前该节点和递归子节点的Id集合
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> getCategoryAndDeepCategoryById(Integer categoryId){

        //1.得到该节点下所有category信息
        //可以使用LinkLinkedHashSet 有序set集合
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategory(categorySet, categoryId);

        //2.得到所有子节点Id
        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId != null){
            for(Category category: categorySet){
                categoryIdList.add(category.getId());
            }
        }

        return ServerResponse.createBySuccess(categoryIdList);
    }


    /**
     *
     * 这里用了set集合用来排重，且category类必须重写equals和hahcode方法
     * 得到当前节点和递归子节点的category信息，
     * @param categorySet
     * @param categoryId
     * @return
     */
    public Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId){

        //1.查出当前节点的category信息，加入到set集合
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        //2.查出他所有的子节点category
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        
        //3.对每个子结点进行遍历查询
        for (Category childCategory: categoryList) {
            findChildrenCategory(categorySet,childCategory.getId());
        }
            return categorySet;
    }

}
