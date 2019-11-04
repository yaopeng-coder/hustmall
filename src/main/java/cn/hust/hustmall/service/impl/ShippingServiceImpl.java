package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dao.ShippingMapper;
import cn.hust.hustmall.pojo.Shipping;
import cn.hust.hustmall.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址服务
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-04 10:53
 **/
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 添加地址
     *
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse<Map> addAddress(Integer userId, Shipping shipping) {

        shipping.setUserId(userId);
        //注意，要在Mappper.xml中加入useGeneratedKeys="true" keyProperty="id"，才能保证返回的对象含有主键
        int count = shippingMapper.insert(shipping);
        if (count > 0) {
            Integer shippingId = shipping.getId();
            Map map = new HashMap();
            map.put("shippingId", shippingId);
            return ServerResponse.createBySuccess("新建地址成功", map);
        }

        return ServerResponse.createByErrorMessage("新建地址失败");

    }

    /**
     * 删除地址，防止横向越权
     *
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse delAddress(Integer userId, Integer shippingId) {

        //注意这里不能只根据shippingid删除，会导致横向越权
        int count = shippingMapper.delByUserIdShippingId(userId, shippingId);
        if (count > 0) {
            return ServerResponse.createBySuccess("删除地址成功");
        }

        return ServerResponse.createByErrorMessage("删除地址失败");


    }


    /**
     * 更新地址，记住一定要判断越权问题，两个越权问题
     *
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse updateAddress(Integer userId, Shipping shipping) {
        //1.防止第一个问越权问题，若不设置，别人可能传入别的userId
        shipping.setUserId(userId);
        //2.防止第二个越权问题，必须根据userID和shipingId两者才能更新，否则只要用户登陆，传入别人的shipingId也能修改
        //3.区别于更新用户信息时，因为用户id直接从session中取得，所以直接根据主键更新，这里不行，必须重写
        int count = shippingMapper.updateByShipping(shipping);
        if (count > 0) {
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }

        return ServerResponse.createByErrorMessage("更新地址失败 ");
    }


    /**
     * 查看某个地址信息，考虑越权
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse<Shipping> selectAddress(Integer userId, Integer shippingId) {

        //注意这里不能只根据shippingid删除，会导致横向越权
        Shipping shipping = shippingMapper.selectByUserIdShippingId(userId, shippingId);
        if (shipping != null) {
            return ServerResponse.createBySuccess(shipping);
        }

        return ServerResponse.createByErrorMessage("无该地址");
    }


    /**
     * 查看所有地址
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> listAddress(Integer userId, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectAllAddressByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }



}
