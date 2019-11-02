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

    @RequestMapping("/add.do")
    public ServerResponse<CartVO> addCart(HttpSession session , Integer productId, Integer count){
            User user = (User)session.getAttribute(Const.CURRENT_USER);
            if(user == null){
                return ServerResponse.createBySuccess();
            }

            Integer userId = user.getId();
            return iCartService.addCart(userId,productId,count);
    }
}
