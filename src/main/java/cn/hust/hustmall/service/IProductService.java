package cn.hust.hustmall.service;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dto.ProductDetailDTO;
import cn.hust.hustmall.pojo.Product;
import com.github.pagehelper.PageInfo;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 09:45
 **/
public interface IProductService {

    ServerResponse<String> saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);
    ServerResponse<ProductDetailDTO> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum, Integer pageSize);

    ServerResponse<ProductDetailDTO>  productDetail(Integer productId);
    ServerResponse<PageInfo> productList(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy);
}
