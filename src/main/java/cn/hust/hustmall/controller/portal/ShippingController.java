package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.Shipping;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IShippingService;
import cn.hust.hustmall.util.CookieUtil;
import cn.hust.hustmall.util.JsonUtil;
import cn.hust.hustmall.util.RedisPoolUtil;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 地址
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-04 10:49
 **/

@RestController
@RequestMapping("/shipping")
public class ShippingController {

    @Autowired
    private IShippingService shippingService;


    /**
     * 添加地址，考虑两个横向越权
     * @param
     * @param shipping
     * @return
     */
    @RequestMapping("/add.do")
    public ServerResponse<Map> addAddress(HttpServletRequest request, Shipping shipping){

        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
          if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

            return shippingService.addAddress(user.getId(),shipping);

    }

    /**
     * 删除地址，考虑一个横向越权
     * @param
     * @param shippingId
     * @return
     */
    @RequestMapping("/del.do")
    public ServerResponse<Map> delAddress(HttpServletRequest request , Integer shippingId){

        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
          if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return shippingService.delAddress(user.getId(),shippingId);

    }

    /**
     * 更新地址，考虑两个越权问题
     * @param
     * @param shipping
     * @return
     */
    @RequestMapping("/update.do")
    public ServerResponse<Map> updateAddress(HttpServletRequest request , Shipping shipping){

        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
                return ServerResponse.createByErrorMessage("用户未登录");
            }

        return shippingService.updateAddress(user.getId(),shipping);

    }

    /**
     * 查看收货地址，考虑一个横向越权问题
     * @param
     * @param shippingId
     * @return
     */
    @RequestMapping("/select.do")
    public ServerResponse<Shipping> selectAddress(HttpServletRequest request , Integer shippingId){

        String token = CookieUtil.readLoginCookie(request);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");
        }
        String userJsonString = RedisPoolUtil.get(token);
        User user = JsonUtil.string2Object(userJsonString, User.class);

         if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return shippingService.selectAddress(user.getId(),shippingId);

    }

    /**
     * 查看所有地址
     * @param
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse<PageInfo> listAddress(HttpServletRequest request , @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                                @RequestParam(value ="pageSize",defaultValue = "10") Integer pageSize){

          String token = CookieUtil.readLoginCookie(request);
          if(StringUtils.isBlank(token)){
              return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登陆status= 10");            }
              String userJsonString = RedisPoolUtil.get(token);
          User user = JsonUtil.string2Object(userJsonString, User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return shippingService.listAddress(user.getId(),pageNum,pageSize);

    }




}
