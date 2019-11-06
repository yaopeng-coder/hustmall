package cn.hust.hustmall.converter;



import cn.hust.hustmall.dao.ShippingMapper;
import cn.hust.hustmall.pojo.Shipping;
import cn.hust.hustmall.vo.ShippingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-06 18:43
 **/
@Component
public class Shipping2ShippingVO {

    @Autowired
    private ShippingMapper shippingMapper;

    private static ShippingMapper mapper;

    @PostConstruct
    private void init(){
        mapper = shippingMapper;
    }

    public static ShippingVO conver(Integer shippingId){
        ShippingVO shippingVO = new ShippingVO() ;
        Shipping shipping = mapper.selectByPrimaryKey(shippingId);
        BeanUtils.copyProperties(shipping,shippingVO);
        return shippingVO;
    }
}
