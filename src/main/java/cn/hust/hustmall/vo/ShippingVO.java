package cn.hust.hustmall.vo;

import lombok.Data;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-06 18:26
 **/
@Data
public class ShippingVO {

    private String receiverName;

    private String receiverPhone;

    private String receiverMobile;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverAddress;

    private String receiverZip;
}
