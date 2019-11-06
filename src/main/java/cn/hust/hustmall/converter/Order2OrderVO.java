package cn.hust.hustmall.converter;


import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.pojo.Order;
import cn.hust.hustmall.util.DateTimeUtil;
import cn.hust.hustmall.util.PropertiesUtil;
import cn.hust.hustmall.vo.OrderVO;
import cn.hust.hustmall.vo.ShippingVO;
import org.springframework.beans.BeanUtils;


/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-06 18:36
 **/
public class Order2OrderVO {

    public static OrderVO conver(Order order){

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        //添加支付类型描述
        orderVO.setPaymentTypeDesc(Const.PaymentTypeEnum.byCodeOf(orderVO.getPaymentType()).getMsg());
        //添加订单状态描述
        orderVO.setStatusDesc(Const.OrderStatusEnum.byCodeOf(orderVO.getStatus()).getMsg());
        //将时间从data->string
        orderVO.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVO.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVO.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVO.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVO.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));

        orderVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        orderVO.setShippingId(order.getShippingId());

        ShippingVO shippingVO = Shipping2ShippingVO.conver(order.getShippingId());
        if(shippingVO != null){
            orderVO.setReceiverName(shippingVO.getReceiverName());
            orderVO.setShippingVO(shippingVO);
        }
        return orderVO;


    }
}
