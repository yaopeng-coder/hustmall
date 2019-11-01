package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dao.ProductMapper;
import cn.hust.hustmall.pojo.Product;
import cn.hust.hustmall.service.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *产品服务
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 09:45
 **/

@Service("iProductService")
public class ProducrServiceImpl implements IProductService {


    @Autowired
    private ProductMapper productMapper;

    /**
     * 新增或者更新产品
     * @param product
     * @return
     */
    public ServerResponse<String> saveOrUpdateProduct(Product product){
        //1.判断product是否为空，不为空设置主图
        if(product == null){
            return ServerResponse.createByErrorMessage("产品不能为空");
        }
        if(StringUtils.isNotBlank(product.getSubImages())){
            String[] subImageArray = product.getSubImages().split(",");
            product.setMainImage(subImageArray[0]);
        }

        //2.判断为更新还是新增操作

        if(product.getId() != null){
            //为更新操作
            int count = productMapper.updateByPrimaryKey(product);
            if(count > 0 ){
                return ServerResponse.createBySuccess("更新产品成功");
            }else {
                return ServerResponse.createByErrorMessage("更新产品失败");
            }
        }else{
            int count = productMapper.insert(product);
            if(count > 0){
                return ServerResponse.createBySuccess("新增产品成功");
            }else{
                return ServerResponse.createByErrorMessage("新增产品失败");
            }
        }

    }

    /**
     * 修改产品上下架
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status){

        //1.判断参数是否为空，不为空则更新
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int count = productMapper.updateByPrimaryKeySelective(product);
        if(count > 0){
            return ServerResponse.createBySuccess("修改产品状态成功");
        }else{
            return ServerResponse.createByErrorMessage("修改产品状态失败");
        }


    }

}
