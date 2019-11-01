package cn.hust.hustmall.controller.backend;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dto.ProductDetailDTO;
import cn.hust.hustmall.pojo.Product;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IProductService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 09:40
 **/

@RestController
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    /**
     *新增或者更新产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("/save.do")
    public ServerResponse<String> productSave(HttpSession session, Product product){
        //1.检查是否登陆
         User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
         if(currentUser == null){
                return ServerResponse.createByErrorMessage("未登录，请先登陆");
         }
        //2.检察是否为管理员

        if(currentUser.getRole() == Const.Role.ROLE_ADMIN){
            //3.进行新增或者更新产品操作
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }

    /**
     * 修改产品上下架
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("/set_sale_status.do")
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer productId, Integer status){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(currentUser.getRole() == Const.Role.ROLE_ADMIN){
            //3.进行新增或者更新产品操作
            return iProductService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }

    /**
     * 查看商品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("/detail.do")
    public ServerResponse<ProductDetailDTO> productDetail(HttpSession session, Integer productId){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(currentUser.getRole() == Const.Role.ROLE_ADMIN){
            //3.进行查看产品详情的操作
           return  iProductService.manageProductDetail(productId);

        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }

    /**
     *查看商品分页列表
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse<PageInfo> getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                            @RequestParam(value = "pageSize" ,defaultValue = "10") Integer pageSize){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(currentUser.getRole() == Const.Role.ROLE_ADMIN){
            //3.进行查看产品详情的操作
        return iProductService.getProductList(pageNum,pageSize);

        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }


    /**
     * 后台根据商品名或者ID实现模糊查询
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/search.do")
    public ServerResponse<PageInfo> searchProduct(HttpSession session, String productName,Integer productId,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                  @RequestParam(value = "pageSize" ,defaultValue = "10") Integer pageSize){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(currentUser.getRole() == Const.Role.ROLE_ADMIN){
            //3.进行模糊查询商品的操作
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);

        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }



}
