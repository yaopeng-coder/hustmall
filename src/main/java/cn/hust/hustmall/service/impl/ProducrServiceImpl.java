package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.converter.Product2ProductDetailDTO;
import cn.hust.hustmall.converter.Product2ProductListDTO;
import cn.hust.hustmall.dao.ProductMapper;
import cn.hust.hustmall.dto.ProductDetailDTO;
import cn.hust.hustmall.dto.ProductListDTO;
import cn.hust.hustmall.pojo.Product;
import cn.hust.hustmall.service.IProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 *产品服务
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 09:45
 **/

@Service("iProductService")
public class ProducrServiceImpl implements IProductService {


    @Autowired
    private  ProductMapper productMapper;

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


    /**
     * 查看单个产品详情
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailDTO> manageProductDetail(Integer productId){

        //1.判断productId是否为空
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        //2.判断查出来的产品是否为空
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }

        //3.属性复制给productDetailDTO
        ProductDetailDTO productDetailDTO = Product2ProductDetailDTO.convert(product);
        return ServerResponse.createBySuccess(productDetailDTO);
    }


    /**
     * 后台商品列表动态分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize){
        //1.pagehelper.start设置一些参数，告诉大家它要准备分页了，后面的查询都要使用它的动态代理方法包装。
        //mybaties的分页的插件的原理是利用AOP原理和拦截器
       // pageHelper分页主要是通过 aop来实现，在执行sql之前会在sql语句中添加limit offset这两个参数。这样就完成了动态的分页
        PageHelper.startPage(pageNum, pageSize);

        //2.查出所有产品列表，并进行数据封装
        //List productList = productMapper.selectList();返回的是一个代理对象，
        // 不是简单的集合，所以需要从productList中获取分页数据
        List<Product> productList = productMapper.selectList();

       /* List<ProductListDTO> productListDTOList = Lists.newArrayList();
        for(Product product : productList){
            ProductListDTO productListDTO = Product2ProductListDTO.convert(product);
            productListDTOList.add(productListDTO);
        }*/
        /**
         用lambda表达式实现
         */
        List<ProductListDTO> productListDTOList = productList.stream()
                .map(e -> Product2ProductListDTO.convert(e))
                .collect(Collectors.toList());

        //3.pageIngo 收尾，首先用sql查询出来的进行分页，然后设置pageinfo的list

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListDTOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     *模糊查询某个商品
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        String name = new StringBuilder().append("%").append(productName).append("%").toString();
        List<Product> productList = productMapper.searchByIdAndProductName(name, productId);
        List<ProductListDTO> productListDTOList = productList.stream()
                .map(e -> Product2ProductListDTO.convert(e))
                .collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListDTOList);
        return ServerResponse.createBySuccess(pageInfo);

    }
}
