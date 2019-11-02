package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dto.ProductDetailDTO;
import cn.hust.hustmall.service.IProductService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-02 10:11
 **/

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService iProductService;


    /**
     * 用户查看商品详情
     * @param productId
     * @return
     */
    @RequestMapping("/detail.do")
    public ServerResponse<ProductDetailDTO> detail(Integer productId){

            return  iProductService.productDetail(productId);

    }


    /**
     * 用户根据分类ID或者关键字进行产品搜索已经进行动态排序
     * @param categotyId
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse<PageInfo> list(@RequestParam(value = "categoryId",required = false) Integer categotyId,
                                         @RequestParam(value = "keyword",required = false) String keyword,
                                         @RequestParam(value = "pageNum",defaultValue = "1",required = false) Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10",required = false) Integer pageSize,
                                         @RequestParam(value = "orderBy",defaultValue = "",required = false) String orderBy){

        return iProductService.productList(categotyId,keyword,pageNum,pageSize,orderBy);

    }


}
