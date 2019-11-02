package cn.hust.hustmall.vo;

import cn.hust.hustmall.dto.CartProductDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-02 16:55
 **/

@Data
public class CartVO {

    private List<CartProductDTO> productDTOList;

    private boolean allChecked;

    private BigDecimal cartTotalPrice;

    private String imageHost;

}
