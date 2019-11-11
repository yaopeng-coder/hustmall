package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.ICartService;
import cn.hust.hustmall.util.CookieUtil;
import cn.hust.hustmall.util.JsonUtil;
import cn.hust.hustmall.util.RedisShardedPoolUtil;
import cn.hust.hustmall.vo.CartVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
     * @param
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/add.do")
    public ServerResponse<CartVO> addCart(HttpServletRequest request, Integer productId, Integer count){
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
            if(user == null){
                return ServerResponse.createBySuccess();
            }

            return iCartService.addCart(user.getId(),productId,count);
    }


    /**
     * 更新购物车某个产品的数量
     * @param
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/update.do")
    public ServerResponse<CartVO> updateCart(HttpServletRequest request, Integer productId, Integer count){
      //  User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess();
        }

        return iCartService.updateCart(user.getId(),productId,count);

    }

    /**
     * 删除购物车某个产品
     * @param
     * @param productIds
     * @return
     */
    @RequestMapping("/delete_product.do")
    public ServerResponse<CartVO> deleteProduct(HttpServletRequest request, String productIds){
       // User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess();
        }

        return iCartService.deleteCart(user.getId(),productIds);
    }


    /**
     * 购物车列表
     * @param
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse<CartVO> list(HttpServletRequest request){
       // User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess();
        }
    return iCartService.list(user.getId());
    }

    /**
     * 购物车全选
     * @param
     * @return
     */
    @RequestMapping("/select_all.do")
    public ServerResponse<CartVO> selectAll(HttpServletRequest request){
      //  User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess();
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }


    /**
     * 购物车全反选
     * @param
     * @return
     */
    @RequestMapping("/un_select_all.do")
    public ServerResponse<CartVO> unSelectAll(HttpServletRequest request){
      //  User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess();
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    /**
     * 购物车单选
     * @param
     * @param productId
     * @return
     */
    @RequestMapping("/select.do")
    public ServerResponse<CartVO> select(HttpServletRequest request,Integer productId){
     //   User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess();
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }

    /**
     * 购物车单不选
     * @param
     * @param productId
     * @return
     */
    @RequestMapping("/un_select.do")
    public ServerResponse<CartVO> unSelect(HttpServletRequest request,Integer productId){
       // User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess();
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    /**
     * 购物车产品总数量
     * @param
     * @return
     */
    @RequestMapping("/get_cart_product_count.do")
    public ServerResponse<Integer> getCartProductCount(HttpServletRequest request){
     //   User user = (User)session.getAttribute(Const.CURRENT_USER);
        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisShardedPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
