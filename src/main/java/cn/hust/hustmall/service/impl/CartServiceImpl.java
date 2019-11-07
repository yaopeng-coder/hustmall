package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dao.CartMapper;
import cn.hust.hustmall.dao.ProductMapper;
import cn.hust.hustmall.dto.CartProductDTO;
import cn.hust.hustmall.pojo.Cart;
import cn.hust.hustmall.pojo.Product;
import cn.hust.hustmall.service.ICartService;
import cn.hust.hustmall.util.BigDecimalUtil;
import cn.hust.hustmall.util.PropertiesUtil;
import cn.hust.hustmall.vo.CartVO;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-02 16:59
 **/
@Service("iCartService")
public class CartServiceImpl implements ICartService{


    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    /**
     * 购物车添加商品
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ServerResponse<CartVO> addCart(Integer userId, Integer productId,Integer count){

        //1.判断参数
       if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        //2.判断cart是否为空
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if(cart == null){
            //该产品不在购物车里增加记录
            Cart cartNew = new Cart();
            cartNew .setUserId(userId);
            cartNew.setProductId(productId);
            cartNew.setQuantity(count);
            cartNew.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartNew);
        }else {
            count = count + cart.getQuantity();
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);

    }

    /**
     * 更新购物车某个产品数量
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ServerResponse<CartVO> updateCart(Integer userId, Integer productId,Integer count){
        //1.判断参数
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        //2.判断cart是否为空,不为空则更新购物车
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if(cart != null){
                cart.setQuantity(count);
                cartMapper.updateByPrimaryKeySelective(cart);
        }


        return this.list(userId);

    }

    /**
     * 删除购物车某个产品数量
     * @param userId
     * @param productIds
     * @return
     */
    public ServerResponse<CartVO> deleteCart(Integer userId, String productIds) {
        //1.将参数string转换成LIST
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        //2.删除产品，成功再返回视图更新对象
        int count = cartMapper.delectProductByUserIdProductIds(userId, productIdList);
        if(count > 0){
            return this.list(userId);
        }

        return ServerResponse.createByErrorMessage("删除失败");


    }


    /**
     * 购物车全选，全反选，单选，单反选
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    public ServerResponse<CartVO> selectOrUnSelect(Integer userId,Integer productId,Integer checked){
            cartMapper.updateCartChecked(userId,productId,checked);
            return this.list(userId);
    }


    /**
     * 购物车产品数量
     * @param userId
     * @return
     */
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
            if(userId == null){
                return ServerResponse.createBySuccess(0);
            }
            //注意，sql语句用sum来计数时，当userID不存在时，会返回null,所以要用IFNULL来将Null转为0
        int count = cartMapper.getCartCountByUserId(userId);
            return ServerResponse.createBySuccess(count);


    }

    /**
     * 得到某个用户的购物车列表
     * @param userId
     * @return
     */
    public CartVO  getCartVOLimit(Integer userId){

        //1.这里的cart表相当于cartItem,每个cart只对应一个产品，对每个cart需要封装一个CartProductDTO对象
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductDTO> cartProductDTOList = Lists.newArrayList();
        CartVO cartVO = new CartVO();
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(cartList)){

            for(Cart cart :cartList){

                //2.找到对应的product对象，利用cart和product给CartProductDTO赋值
                CartProductDTO cartProductDTO = new CartProductDTO();
                BeanUtils.copyProperties(cart,cartProductDTO);

                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                if(product != null){
                    BeanUtils.copyProperties(product,cartProductDTO);
                    //3.判断库存
                    if(product.getStock()>= cartProductDTO.getQuantity()){
                        //3.1库存充足，返回LIMIT_NUM_SUCCESS
                        cartProductDTO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        //3.2库存不充足，返回LIMIT_NUM_ERROR,且把数据库的cart更新
                        Integer realStock = product.getStock();
                        Cart cartNew = new Cart();
                        cartNew.setId(cart.getId());
                        cartNew.setQuantity(realStock);
                        cartMapper.updateByPrimaryKeySelective(cartNew);
                        cartProductDTO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    }
                    //4.计算价格
                    BigDecimal total = BigDecimalUtil.mul(cartProductDTO.getQuantity(),product.getPrice().doubleValue());
                    cartProductDTO.setProductTotalPrice(total);
                    cartProductDTO.setProductChecked(cart.getChecked());
                    //5.判断CartProductDTO是否选中，选中将刚刚计算的价格加到总价，并添加至List
                    if(cart.getChecked() == Const.Cart.CHECKED){
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),total.doubleValue());
                    }
                }
                cartProductDTOList.add(cartProductDTO);
            }
        }

        //5.判断购物车是否全选，取数据库里查询即可，反逻辑，只要找不到数据里的cart的checked均为0的情况即可
         cartVO.setAllChecked(this.getAllCheckedStatus(userId));

        //6.为cartvo设置总价，getOrderList,和imageHost
        cartVO.setCartTotalPrice(cartTotalPrice);
        cartVO.setProductDTOList(cartProductDTOList);
        cartVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVO;
    }


    /**
     * 得到某个用户的购物车列表视图对象
     * @param userId
     * @return
     */
    public ServerResponse<CartVO> list(Integer userId){
        CartVO cartVO = getCartVOLimit(userId);
        return ServerResponse.createBySuccess(cartVO);
    }

    /**
     * 判断购物车是否是全选
     * @param userId
     * @return
     */
    public boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return  false;
        }

        return (cartMapper.checkCartCheckedByUserId(userId) == 0);
    }


}
