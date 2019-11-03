package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.ICartService;
import cn.hust.hustmall.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 购物车
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-02 16:47
 **/
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     * 购物车添加商品
     * @param session
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/add.do")
    public ServerResponse<CartVO> addCart(HttpSession session , Integer productId, Integer count){
            User user = (User)session.getAttribute(Const.CURRENT_USER);
            if(user == null){
                return ServerResponse.createBySuccess();
            }

            return iCartService.addCart(user.getId(),productId,count);
    }


    /**
     * 更新购物车某个产品的数量
     * @param session
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/update.do")
    public ServerResponse<CartVO> updateCart(HttpSession session, Integer productId, Integer count){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createBySuccess();
        }

        return iCartService.updateCart(user.getId(),productId,count);

    }

    /**
     * 删除购物车某个产品
     * @param session
     * @param productIds
     * @return
     */
    @RequestMapping("/delete_product.do")
    public ServerResponse<CartVO> deleteProduct(HttpSession session, String productIds){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createBySuccess();
        }

        return iCartService.deleteCart(user.getId(),productIds);
    }

}
