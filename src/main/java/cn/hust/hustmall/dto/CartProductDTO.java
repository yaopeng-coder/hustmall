package cn.hust.hustmall.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-02 16:51
 **/

@Data
public class CartProductDTO {

    private Integer id;

    private Integer userId;

    private Integer productId;

    private Integer quantity;

    @JsonProperty("productName")
    private String name;

    @JsonProperty("productSubtitle")
    private String subtitle;

    @JsonProperty("productMainImage")
    private String mainImage;

    @JsonProperty("productPrice")
    private BigDecimal price;

    @JsonProperty("productStatus")
    private Integer status;

    private BigDecimal productTotalPrice;

    @JsonProperty("productStock")
    private Integer stock;

    private Integer productChecked;

    private String limitQuantity;


}
