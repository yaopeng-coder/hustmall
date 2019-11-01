package cn.hust.hustmall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 11:16
 **/
@Data
public class ProductListDTO {

    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;


    private Integer status;

    private BigDecimal price;


    private String imageHost;

}
