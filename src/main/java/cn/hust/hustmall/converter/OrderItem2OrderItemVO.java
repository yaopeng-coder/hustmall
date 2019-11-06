package cn.hust.hustmall.converter;

import cn.hust.hustmall.pojo.OrderItem;
import cn.hust.hustmall.util.DateTimeUtil;
import cn.hust.hustmall.vo.OrderItemVO;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-06 18:28
 **/
public class OrderItem2OrderItemVO {


    public static OrderItemVO conver(OrderItem orderItem){

        OrderItemVO orderItemVO = new OrderItemVO();
        BeanUtils.copyProperties(orderItem,orderItemVO);
        orderItemVO.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVO;
    }

    public static List<OrderItemVO> conver(List<OrderItem> orderItemList){

        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        for(OrderItem orderItem: orderItemList){
            OrderItemVO orderItemVO = conver(orderItem);
            orderItemVOList.add(orderItemVO);
        }

        return orderItemVOList;

    }
}
