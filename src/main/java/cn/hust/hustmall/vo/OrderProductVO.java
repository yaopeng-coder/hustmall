package cn.hust.hustmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-07 09:20
 **/
@Data
public class OrderProductVO {

    private List<OrderItemVO> orderItemVOList;

    private String  imageHost;

    private BigDecimal productTotalPrice;

}
