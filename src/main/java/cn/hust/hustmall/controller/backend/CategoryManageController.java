package cn.hust.hustmall.controller.backend;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.ICategoryService;
import cn.hust.hustmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 *类别管理
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-31 15:23
 **/

@RestController
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("/add_category.do")
    public ServerResponse<String> addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId){
        //1.判断用户是否登陆
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登录");
        }
        //2.校验用户身份，必须为管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //3.添加类别
            return iCategoryService.addCategory(parentId,categoryName);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员,没有权限");
        }
    }




    @RequestMapping("/set_category_name.do")
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName,HttpSession session){
        //1.判断用户是否登陆
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登录");
        }
        //2.校验用户身份，必须为管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //3.修改品类名字
            return iCategoryService.updateCategotyName(categoryId,categoryName);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员,没有权限");
        }
    }
}
