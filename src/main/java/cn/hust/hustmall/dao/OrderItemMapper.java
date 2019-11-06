package cn.hust.hustmall.dao;

import cn.hust.hustmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectByUserIdOrderNo(@Param(value = "userId") Integer userId, @Param(value = "orderNo") Long orderNo);

    int orderItemBatch(@Param(value = "orderItemList") List<OrderItem> orderItemList);
}