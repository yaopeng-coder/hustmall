package cn.hust.hustmall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-06 18:21
 **/
@Data
public class OrderItemVO {


    private Long orderNo;

    private Integer productId;

    private String productName;

    private String productImage;

    private BigDecimal currentUnitPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private String createTime;
}
