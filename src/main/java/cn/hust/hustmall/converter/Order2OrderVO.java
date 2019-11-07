package cn.hust.hustmall.converter;


import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.dao.OrderItemMapper;
import cn.hust.hustmall.pojo.Order;
import cn.hust.hustmall.pojo.OrderItem;
import cn.hust.hustmall.util.DateTimeUtil;
import cn.hust.hustmall.util.PropertiesUtil;
import cn.hust.hustmall.vo.OrderItemVO;
import cn.hust.hustmall.vo.OrderVO;
import cn.hust.hustmall.vo.ShippingVO;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;


/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-06 18:36
 **/
@Component
public class Order2OrderVO {

    @Autowired
    private OrderItemMapper orderItemMapper;

    private static OrderItemMapper mapper;

    @PostConstruct
    public void init(){
        mapper = orderItemMapper;
    }

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


    public static OrderVO assembelOrderVO(Order order , List<OrderItem> orderItemList){
        OrderVO orderVO = conver(order);
        List<OrderItemVO> orderItemVOList = OrderItem2OrderItemVO.conver(orderItemList);
        orderVO.setOrderItemVOList(orderItemVOList);
        return orderVO;
    }

    public static List<OrderVO> assembelOrderVOList(List<Order> orderList,Integer userId){

       List<OrderVO> orderVOList = Lists.newArrayList();
       for(Order order : orderList){
           List<OrderItem> orderItemList = Lists.newArrayList();
           if(userId == null){
             orderItemList = mapper.selectByOrderNo(order.getOrderNo());
           }else{
              orderItemList = mapper.selectByUserIdOrderNo(userId, order.getOrderNo());
           }
           OrderVO orderVO = Order2OrderVO.assembelOrderVO(order, orderItemList);
           orderVOList.add(orderVO);

       }
       return orderVOList;
    }

}
