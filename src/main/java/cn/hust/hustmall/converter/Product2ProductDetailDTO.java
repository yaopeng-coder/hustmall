package cn.hust.hustmall.converter;

import cn.hust.hustmall.dao.CategoryMapper;
import cn.hust.hustmall.dto.ProductDetailDTO;
import cn.hust.hustmall.pojo.Category;
import cn.hust.hustmall.pojo.Product;
import cn.hust.hustmall.util.DateTimeUtil;
import cn.hust.hustmall.util.PropertiesUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 11:26
 **/
@Component
public class Product2ProductDetailDTO {


    //若想在static方法里调用自定注入的属性步骤
    //1.类上加@Component
    //2.加@PostConstruct


    @Autowired
    private  CategoryMapper categoryMapper;

    private static CategoryMapper mapper;

    @PostConstruct
    public void init(){
        mapper = categoryMapper;
    }

     public static ProductDetailDTO convert(Product product){
         ProductDetailDTO productDetailDTO  = new ProductDetailDTO();
         BeanUtils.copyProperties(product,productDetailDTO);

         //1.需要赋值imageHost
         productDetailDTO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

         //2.需要赋值parentCategoryId
         Category category = mapper.selectByPrimaryKey(product.getCategoryId());
         //若categoryId为0，则代表根节点，其category为空，此时我们把根节点的根节点的设置为0
         if(category == null){
             productDetailDTO.setParentCategoryId(0);
         }else{
             productDetailDTO.setParentCategoryId(category.getParentId());
         }

         //3.需要转化数据库的时间，因为数据库的时间显示出来是Ms,需要转化成统一格式
         productDetailDTO.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
         productDetailDTO.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

            return productDetailDTO;
     }
}
