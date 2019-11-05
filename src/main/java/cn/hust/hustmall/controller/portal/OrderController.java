package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 支付模块
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-05 09:45
 **/
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 支付，将二维码通过springmv上传文件的方式上传至ftp服务器，并返回域名加文件给前端，前端访问域名通过nginx转发找到该二维码
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("/pay.do")
    public ServerResponse<Map> pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String path = session.getServletContext().getRealPath("upload");
        return orderService.pay(user.getId(),orderNo,path);




    }
}
