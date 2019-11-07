package cn.hust.hustmall.dao;

import cn.hust.hustmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectByUserIdOrderNo(@Param(value = "userId") Integer userId, @Param(value = "orderNo") Long orderNo);

    Order selectByOrderNo(Long orderNo);

    List<Order> selectListByUserId(Integer userId);

    List<Order> selectAllOrder();
}