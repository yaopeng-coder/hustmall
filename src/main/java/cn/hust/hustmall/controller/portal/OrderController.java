package cn.hust.hustmall.controller.portal;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IOrderService;
import cn.hust.hustmall.vo.OrderProductVO;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付模块
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-05 09:45
 **/
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 支付，将二维码通过springmv上传文件的方式上传至ftp服务器，并返回域名加文件给前端，前端访问域名通过nginx转发找到该二维码
     *用户下单后，生成订单，商户对订单数据用私钥加密，发给支付宝，支付宝用商家的公钥进行验签，
     * 然后生成二维码的字符发给商家，用户扫码支付后，触发回调函数，商家会用支付宝公钥进行验证，并且校验数据，无误后会将订单状态改变
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("/pay.do")
    public ServerResponse<Map> pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String path = session.getServletContext().getRealPath("upload");
        return orderService.pay(user.getId(), orderNo, path);

    }

    /**
     * 支付宝回调函数
     *
     * @param request
     * @return
     */
    @RequestMapping("/alipay_callback.do")
    public Object alipayCallback(HttpServletRequest request) {

        //1.获取回调请求中的各种参数，注意key对应的value是数组类型的，所以我们需要对他进行转换
        Map<String, String[]> map = request.getParameterMap();
        Map<String, String> params = Maps.newHashMap();
        for (Iterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            String[] values = map.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(key, valueStr);
        }

        //2.验证回调的正确性，判断是不是支付宝发出的，并且还要避免重复通知
        //2.1在通知返回参数列表中，除去sign、sign_type两个参数外，凡是通知返回回来的参数皆是待验签的参数
        params.remove("sign_type");

        //2.2将剩下参数进行 url_decode, 然后进行字典排序，组成字符串，得到待签名字符串
        //2.3将签名参数（sign）使用 base64 解码为字节码串。

        //2.4用 RSA/RSA2 的验签方法，通过签名字符串、签名参数（经过 base64 解码）及支付宝公钥验证签名。
        //AlipaySignature.rsaCheckV2读源码，他第一步除去sign，然后将剩下参数进行 url_decode, 然后进行字典排序，组成字符串，得到待签名字符串，
        //最后将签名参数（sign）使用 base64 解码为字节码串
        try {
            boolean result = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "UTF-8", Configs.getSignType());
            if (!result) {
                return ServerResponse.createByErrorMessage("非法请求，请勿再次请求");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调异常,{}", e);
            return ServerResponse.createByErrorMessage("支付宝回调异常");
        }

        //todo:验证数据的正确性

        //3.验证数据，正确则返回"success",错误返回"fail"，支付宝规定的
        ServerResponse<String> serverResponse = orderService.aliPayBack(params);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        } else {
            return Const.AlipayCallback.RESPONSE_FAIL;
        }


    }

    /**
     * 查询订单状态，便于前端在用户支付完成后根据后端传回来的true进行跳转页面
     *
     * @param orderNo
     * @param session
     * @return
     */
    @RequestMapping("/query_order_pay_status.do")
    public ServerResponse<Boolean> queryOrderPayStatus(Long orderNo, HttpSession session) {
        //1.判断当前用户是否已登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return orderService.queryOrderPayStatus(orderNo, currentUser.getId());

    }

    /**
     * 创建订单
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("/create.do")
   public ServerResponse createOrder(HttpSession session, Integer shippingId){
       //1.判断当前用户是否已登录
       User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
       if (currentUser == null) {
           return ServerResponse.createByErrorMessage("用户未登录");
       }

        return orderService.createOrder(currentUser.getId(),shippingId);
   }

    /**
     * 取消订单
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("/cancel.do")
    public ServerResponse cancelOrder(HttpSession session, Long orderNo){
        //1.判断当前用户是否已登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return orderService.cancel(currentUser.getId(),orderNo);
    }

    /**
     * 得到购物车已经勾选的产品信息，提交订单时使用
     * @param session
     * @return
     */
    @RequestMapping("/get_order_cart_product.do")
    public ServerResponse<OrderProductVO> getOrderCartProduct(HttpSession session){
        //1.判断当前用户是否已登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return orderService.getOrderCartProduct(currentUser.getId());
    }

    /**
     * 查看订单详情
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("/detail.do")
    public ServerResponse detail(HttpSession session, Long orderNo){
        //1.判断当前用户是否已登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return orderService.detail(currentUser.getId(),orderNo);
    }

    @RequestMapping("/list.do")
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10")  Integer pageSize){
        //1.判断当前用户是否已登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return orderService.getOrderList(currentUser.getId(),pageNum,pageSize);
    }



}