package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dao.OrderItemMapper;
import cn.hust.hustmall.dao.OrderMapper;
import cn.hust.hustmall.dao.PayInfoMapper;
import cn.hust.hustmall.pojo.Order;
import cn.hust.hustmall.pojo.OrderItem;
import cn.hust.hustmall.pojo.PayInfo;
import cn.hust.hustmall.service.IOrderService;
import cn.hust.hustmall.util.BigDecimalUtil;
import cn.hust.hustmall.util.DateTimeUtil;
import cn.hust.hustmall.util.FTPUtil;
import cn.hust.hustmall.util.PropertiesUtil;
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
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ServerResponse<Map> pay(Integer userId,Long orderNo,String path) {

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
        if(order.getStatus() != Const.orderStatusEnum.NOT_PAY.getCode()){
            return ServerResponse.createBySuccess("支付宝重复支付");
        }

        //4.判断交易状态是否是TRADE_SUCCESS，是则修改订单的订单状态和支付时间
        if(tradeStatus.equals(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS)){
            order.setStatus(Const.orderStatusEnum.PAY.getCode());
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

        if(order.getStatus() >= Const.orderStatusEnum.PAY.getCode()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createByError();

    }
}
