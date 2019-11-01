package cn.hust.hustmall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 11:16
 **/
@Data
public class ProductDetailDTO {

    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private String subImages;

    private String detail;

    private BigDecimal price;

    private Integer stock;

    private Integer status;

    private String createTime;

    private String updateTime;

    private Integer parentCategoryId;

    private String imageHost;

}
