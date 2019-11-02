package cn.hust.hustmall.converter;

import cn.hust.hustmall.dao.CategoryMapper;
import cn.hust.hustmall.dto.ProductListDTO;
import cn.hust.hustmall.pojo.Product;
import cn.hust.hustmall.util.PropertiesUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 11:26
 **/
@Component
public class Product2ProductListDTO {


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

     public static ProductListDTO convert(Product product){
         ProductListDTO productListDTO = new ProductListDTO();
         BeanUtils.copyProperties(product,productListDTO);

         //1.需要赋值imageHost
         productListDTO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
            return productListDTO;
     }

    public static List<ProductListDTO> convert(List<Product> productList){

         List<ProductListDTO> productListDTOList = Lists.newArrayList();
        for (Product product:productList) {
                productListDTOList.add(convert(product));
            
        }

        return productListDTOList;
    }

}
