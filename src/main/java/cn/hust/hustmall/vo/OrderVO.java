package cn.hust.hustmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-06 18:22
 **/

@Data
public class OrderVO {

    private Long orderNo;

    private BigDecimal payment;

    private Integer paymentType;

    private String paymentTypeDesc;

    private Integer postage;

    private Integer status;

    private String statusDesc;

    private String paymentTime;

    private String sendTime;

    private String endTime;

    private String closeTime;

    private String createTime;

    private Integer shippingId;

    private String imageHost;

    private String receiverName;

    private ShippingVO shippingVO;

    private List<OrderItemVO> orderItemVOList;

}
