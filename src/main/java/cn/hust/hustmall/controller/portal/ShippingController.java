package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.Shipping;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IShippingService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
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
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping("/add.do")
    public ServerResponse<Map> addAddress(HttpSession session , Shipping shipping){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

            return shippingService.addAddress(user.getId(),shipping);

    }

    /**
     * 删除地址，考虑一个横向越权
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("/del.do")
    public ServerResponse<Map> delAddress(HttpSession session , Integer shippingId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return shippingService.delAddress(user.getId(),shippingId);

    }

    /**
     * 更新地址，考虑两个越权问题
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping("/update.do")
    public ServerResponse<Map> updateAddress(HttpSession session , Shipping shipping){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return shippingService.updateAddress(user.getId(),shipping);

    }

    /**
     * 查看收货地址，考虑一个横向越权问题
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("/select.do")
    public ServerResponse<Shipping> selectAddress(HttpSession session , Integer shippingId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return shippingService.selectAddress(user.getId(),shippingId);

    }

    /**
     * 查看所有地址
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse<PageInfo> listAddress(HttpSession session , @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                                @RequestParam(value ="pageSize",defaultValue = "10") Integer pageSize){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return shippingService.listAddress(user.getId(),pageNum,pageSize);

    }




}
