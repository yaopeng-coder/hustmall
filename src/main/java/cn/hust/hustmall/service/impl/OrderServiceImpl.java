package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.converter.Order2OrderVO;
import cn.hust.hustmall.converter.OrderItem2OrderItemVO;
import cn.hust.hustmall.dao.*;
import cn.hust.hustmall.pojo.*;
import cn.hust.hustmall.service.IOrderService;
import cn.hust.hustmall.util.BigDecimalUtil;
import cn.hust.hustmall.util.DateTimeUtil;
import cn.hust.hustmall.util.FTPUtil;
import cn.hust.hustmall.util.PropertiesUtil;
import cn.hust.hustmall.vo.OrderItemVO;
import cn.hust.hustmall.vo.OrderProductVO;
import cn.hust.hustmall.vo.OrderVO;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-05 09:48
 **/
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;


    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

    }

    /**
     * 订单支付
     * @param userId
     * @param orderNo
     * @param path
     * @return
     */
    public ServerResponse pay(Integer userId,Long orderNo,String path) {

        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在，无法支付");
        }

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = orderNo.toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "hustmall当面付，订单为："+outTradeNo;

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共花").append(order.getPayment().toString()).toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        for(OrderItem orderItem:orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(GoodsDetail.newInstance(orderItem.getProductId().toString(),orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),orderItem.getQuantity()));
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File fileDir = new File(path);
                if(!fileDir.exists()){
                    fileDir.setWritable(true);
                    fileDir.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path+"/qr-%s.png",
                        response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                log.info("filePath:" + qrPath);
                 ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                 File targetFile = new File(path,qrFileName);
                try {
                    FTPUtil.upload(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                   log.error("上传二维码失败,{}",e);
                }
                Map<String,String> map = new HashMap<>();
                map.put("orderNo",orderNo.toString());
                map.put("qrUrl",PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName());
                return ServerResponse.createBySuccess(map);
            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");


            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }


    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }


    /**
     * 支付回调参数验证
     * @param params
     * @return
     */
    public ServerResponse aliPayBack(Map<String,String> params ){

        //1.得到订单号，交易状态，支付宝交易号
        String orderNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");

        //2.判断订单是否存在
        Order order = orderMapper.selectByOrderNo(Long.parseLong(orderNo));
        if(order == null){
            return ServerResponse.createByErrorMessage("该订单不是hustmall商城的订单");
        }

        //3.判断订单状态是否已经支付
        if(order.getStatus() != Const.OrderStatusEnum.NOT_PAY.getCode()){
            return ServerResponse.createBySuccess("支付宝重复支付");
        }

        //4.判断交易状态是否是TRADE_SUCCESS，是则修改订单的订单状态和支付时间
        if(tradeStatus.equals(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS)){
            order.setStatus(Const.OrderStatusEnum.PAY.getCode());
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }

        //5.插入支付信息到数据库

        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(Long.parseLong(orderNo));
        payInfo.setUserId(order.getUserId());
        payInfo.setPlatformStatus(tradeStatus); //交易状态
        payInfo.setPlatformNumber(tradeNo); //支付宝交易号
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALI_PAY.getCode()); //交易类型

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    /**
     * 查询订单状态，便于前端在用户支付完成后根据后端传回来的true进行跳转页面
     * @param orderNo
     * @param userId
     * @return
     */
    public ServerResponse<Boolean> queryOrderPayStatus(Long orderNo,Integer userId){

        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户没有该订单，查询无效");
        }

        if(order.getStatus() >= Const.OrderStatusEnum.PAY.getCode()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createByError();

    }


    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    @Transactional
    public ServerResponse<OrderVO> createOrder(Integer userId,Integer shippingId){
        //1.查出本人购物车所有勾选了的物品
        List<Cart> cartList = cartMapper.selectByUserIdChecked(userId);
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("未选中任何商品");
        }

        //2.1生成订单号
        long orderNo = this.generateOrderNo();

        //3.生成List<OrderItem>，批量插入orderItem
        List<OrderItem> orderItemList = this.getCartOrderItemList(userId, cartList,orderNo);
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("订单详情不存在");
        }
        int count = orderItemMapper.orderItemBatch(orderItemList);
        if(count <=0){
            return ServerResponse.createByErrorMessage("订单详情表插入数据库失败");
        }
        //4.生成order,插入到数据库

            //3.1计算订单总价
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
                payment = BigDecimalUtil.add(orderItem.getTotalPrice().doubleValue(),payment.doubleValue());
        }
            //3.2生成订单插入数据库，
        Order order = this.assembleOrder(orderNo, userId, shippingId, payment);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单生成失败");
        }

        //4.减去每个产品库存
        this.reduceProductStock(orderItemList);

        //5.清空购物车
        this.cleanCart(cartList);

        //6.生成视图对象
        OrderVO orderVO = Order2OrderVO.assembelOrderVO(order, orderItemList);
        return ServerResponse.createBySuccess(orderVO);


    }


    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    @Transactional
    public ServerResponse<String> cancel(Integer userId, Long orderNo){

        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        }

        if(order.getStatus() != Const.OrderStatusEnum.NOT_PAY.getCode()){
            return ServerResponse.createByErrorMessage("此订单已经支付，不能取消");
        }


        Order orderNew = new Order();
        orderNew.setId(order.getId());
        orderNew.setStatus(Const.OrderStatusEnum.CANCEL.getCode());
        int count = orderMapper.updateByPrimaryKeySelective(orderNew);

        if(count <=0){
            return ServerResponse.createByErrorMessage("取消订单失败");

        }

        //增加库存
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        this.increaseProductStock(orderItemList);

        
        return ServerResponse.createBySuccess();



    }


    /**
     * 得到购物车已经勾选的产品信息
     * @param userId
     * @return
     */
    public ServerResponse<OrderProductVO> getOrderCartProduct(Integer userId){

        OrderProductVO orderProductVO = new OrderProductVO();
        //1.查出购物车选中的物品
        List<Cart> cartList = cartMapper.selectByUserIdChecked(userId);
        //2.得到订单详情
        List<OrderItem> orderItemList = this.getCartOrderItemList(userId,cartList,null);
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createBySuccess();
        }
        //3.拼装orderProductVO
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        orderProductVO.setProductTotalPrice(payment);
        List<OrderItemVO> orderItemVOList = OrderItem2OrderItemVO.conver(orderItemList);
        orderProductVO.setOrderItemVOList(orderItemVOList);
        orderProductVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVO);



    }

    /**
     * 查看订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVO> detail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("此用户该订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        if (CollectionUtils.isEmpty(orderItemList)) {

            return ServerResponse.createByErrorMessage("该订单的订单详情不存在");
        }

        OrderVO orderVO = Order2OrderVO.assembelOrderVO(order, orderItemList);
        return ServerResponse.createBySuccess(orderVO);

    }

    /**
     * 查看某人的订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectListByUserId(userId);
        List<OrderVO> orderVOList = Order2OrderVO.assembelOrderVOList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);

    }

    /**
     * 根据购物车生成订单详情
     * @param userId
     * @param cartList
     * @return
     */
    private List<OrderItem> getCartOrderItemList(Integer userId, List<Cart> cartList, Long orderNo){

        List<OrderItem> orderItemList = Lists.newArrayList();
        for(Cart cart: cartList){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            orderItem.setOrderNo(orderNo);
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity().doubleValue(),product.getPrice().doubleValue()));

            orderItemList.add(orderItem);
        }

        return orderItemList;
    }

    /**
     * 生成订单表
     * @param orderNo
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Long orderNo, Integer userId, Integer shippingId, BigDecimal payment){
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ON_LINE.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NOT_PAY.getCode());
        int count = orderMapper.insert(order);
        if(count > 0){
            return order;
        }
        return null;
    }

    /**
     * 生成订单号
     * @return
     */
    private Long generateOrderNo(){

        return System.currentTimeMillis()+ new Random().nextInt(100);


    }

    /**
     * 减去每个产品库存
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){

            //注意，这里面的sql语句用了select .. for update,在用主键进行查询时，会产生行锁，否则会产生表锁
            //在dao层返回值设置为Integer类型，因为如果产品被删除等等，用Int类型接受不了Null类型的值，会报错
            //区别与以前可以用int，因为以前查询都是select count(1),而现在是select stock
            //一定要用主键where条件，防止锁表。同时必须是支持MySQL的InnoDB。
            Integer stock = productMapper.selectStockByProductId(orderItem.getProductId());

            if(stock == null){
                continue;
            }
            Product product = new Product();
            product.setId(orderItem.getProductId());
            product.setStock(stock - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * 增加每个产品的库存
     * @param orderItemList
     */
    private void increaseProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){

            //注意，这里面的sql语句用了select .. for update,在用主键进行查询时，会产生行锁，否则会产生表锁
            //在dao层返回值设置为Integer类型，因为如果产品被删除等等，用Int类型接受不了Null类型的值，会报错
            //区别与以前可以用int，因为以前查询都是select count(1),而现在是select stock
            //一定要用主键where条件，防止锁表。同时必须是支持MySQL的InnoDB。
            Integer stock = productMapper.selectStockByProductId(orderItem.getProductId());

            if(stock == null){
                continue;
            }
            Product product = new Product();
            product.setId(orderItem.getProductId());
            product.setStock(stock+orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }


    /**
     * 清空购物车
     * @param cartList
     */
    public void cleanCart(List<Cart> cartList){
        for(Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }

    }




    //bakend ,管理员服务

    /**
     * 管理员查看订单列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageOrderList(Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        if (CollectionUtils.isEmpty(orderList)) {
            return ServerResponse.createBySuccess();
        }
        List<OrderVO> orderVOList = Order2OrderVO.assembelOrderVOList(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);


    }

    /**
     * 管理员查看单个订单
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageSearchOrder(Long orderNo,Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        if(CollectionUtils.isEmpty(orderItemList)){
            return  ServerResponse.createByErrorMessage("该订单详情不存在");
        }
        OrderVO orderVO = Order2OrderVO.assembelOrderVO(order, orderItemList);
        PageInfo pageInfo = new PageInfo(Arrays.asList(order));
        pageInfo.setList(Lists.newArrayList(orderVO));
        return ServerResponse.createBySuccess(pageInfo);

    }


    /**
     * 管理员查看某个订单详情
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVO> manageOrderDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单号不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        if(CollectionUtils.isEmpty(orderItemList)){
            return  ServerResponse.createByErrorMessage("订单详情不存在");
        }
        OrderVO orderVO = Order2OrderVO.assembelOrderVO(order,orderItemList);
        return ServerResponse.createBySuccess(orderVO);

    }

    /**
     * 管理员把订单发货
     * @param orderNo
     * @return
     */
    public ServerResponse<String> manageSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单号不存在");
        }
        if(order.getStatus() == Const.OrderStatusEnum.PAY.getCode()){
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            int count = orderMapper.updateByPrimaryKeySelective(order);
            if(count > 0){
                return ServerResponse.createBySuccess("发货成功");
            }
        }

        return ServerResponse.createByErrorMessage("发货失败");

    }

    /**
     * spirng schdule 定时任务关闭订单
     * @param hour
     */

    public void closeOrder(int hour){
        //1.查询在规定时间以前的所有订单
        Date date = DateUtils.addHours(new Date(),-hour);
        List<Order> orderList = orderMapper.selectByOrderStatusCreateTime(Const.OrderStatusEnum.NOT_PAY.getCode(), DateTimeUtil.dateToStr(date));

        //2.遍历某个订单，并且对订单的每个订单详情增加库存
        for(Order order : orderList){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            if(CollectionUtils.isNotEmpty(orderItemList)){

                //注意这里涉及到的锁问题
                this.increaseProductStock(orderItemList);
            }

            //3.关闭订单，将订单状态置为取消
            orderMapper.closeOrderById(order.getId());
            log.info("关闭订单orderNo{}",order.getOrderNo());
        }



    }


}
