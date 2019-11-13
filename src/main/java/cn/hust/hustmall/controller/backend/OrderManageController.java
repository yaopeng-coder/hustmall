package cn.hust.hustmall.controller.backend;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.service.IOrderService;
import cn.hust.hustmall.service.IUserService;
import cn.hust.hustmall.vo.OrderVO;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-07 11:02
 **/
@RestController
@RequestMapping("/manage/order")
public class OrderManageController {


    @Autowired
    private IUserService iUserService;

    @Autowired
    private IOrderService orderService;


    /**
     * 管理员查看订单列表
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10")  Integer pageSize){
        //1.检查是否登陆
//        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
//        if(currentUser == null){
//            return ServerResponse.createByErrorMessage("未登录，请先登陆");
//        }
//        //2.检察是否为管理员
//
//        if(iUserService.checkAdminRole(currentUser).isSuccess()){
//            //3.进行业务操作
//            return orderService.manageOrderList(pageNum,pageSize);
//
//        }else{
//            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
//        }

        //拦截器已经验证权限，直接进行业务处理

        return orderService.manageOrderList(pageNum,pageSize);
    }

    /**
     * 管理员查看某个订单
     * @param session
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/search.do")
    public ServerResponse<PageInfo> searchOrder(HttpSession session, Long orderNo,@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10")  Integer pageSize){
        //1.检查是否登陆
//        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
//        if(currentUser == null){
//            return ServerResponse.createByErrorMessage("未登录，请先登陆");
//        }
//        //2.检察是否为管理员
//
//        if(iUserService.checkAdminRole(currentUser).isSuccess()){
//            //3.进行业务操作
//                return orderService.manageSearchOrder(orderNo,pageNum,pageSize);
//
//        }else{
//            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
//        }

        //拦截器已经验证权限，直接进行业务处理
        return orderService.manageSearchOrder(orderNo,pageNum,pageSize);
    }

    /**
     * 管理员查看订单详情
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("/detail.do")
    public ServerResponse<OrderVO> manageDetailOrder(HttpSession session, Long orderNo){
        //1.检查是否登陆
//        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
//        if(currentUser == null){
//            return ServerResponse.createByErrorMessage("未登录，请先登陆");
//        }
//        //2.检察是否为管理员
//
//        if(iUserService.checkAdminRole(currentUser).isSuccess()){
//            //3.进行业务操作
//            return orderService.manageOrderDetail(orderNo);
//
//        }else{
//            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
//        }

        //拦截器已经验证权限，直接进行业务处理
        return orderService.manageOrderDetail(orderNo);
    }

    /**
     * 管理员发货
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("/send_goods.do")
    public ServerResponse<String> manageSendGoods(HttpSession session, Long orderNo){
        //1.检查是否登陆
//        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
//        if(currentUser == null){
//            return ServerResponse.createByErrorMessage("未登录，请先登陆");
//        }
//        //2.检察是否为管理员
//
//        if(iUserService.checkAdminRole(currentUser).isSuccess()){
//            //3.进行业务操作
//            return orderService.manageSendGoods(orderNo);
//
//        }else{
//            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
//        }

        //拦截器已经验证权限，直接进行业务处理
        return orderService.manageSendGoods(orderNo);
    }
}
