package cn.hust.hustmall.controller.backend;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ResponseCode;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.Category;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.ICategoryService;
import cn.hust.hustmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

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

        /**
         * 添加产品类别
         * @param session
         * @param categoryName
         * @param parentId
         * @return
         */
        @RequestMapping("/add_category.do")
        public ServerResponse<String> addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId){
            //1.判断用户是否登陆
            User user = (User)session.getAttribute(Const.CURRENT_USER);
            if(user == null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登录");
            }
            //2.校验用户身份，必须为管理员，防止横向越权，不能有session信息就不判断
            if(iUserService.checkAdminRole(user).isSuccess()){
                //3.添加类别
                return iCategoryService.addCategory(parentId,categoryName);
            }else{
                return ServerResponse.createByErrorMessage("不是管理员,没有权限");
            }
        }


        /**
         * 修改类别名字
         * @param categoryId
         * @param categoryName
         * @param session
         * @return
         */
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

        /**
         * 查找该品类的子节点categoty信息（不递归）
         * @param categoryId
         * @param session
         * @return
         */
        @RequestMapping("/get_category.do")
        public ServerResponse<List<Category>> getChildrenParallelCategory(@RequestParam(value = "categoryId",defaultValue = "0")
                                                                                  Integer categoryId,HttpSession session){
            //1.判断用户是否登陆
            User user = (User)session.getAttribute(Const.CURRENT_USER);
            if(user == null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登录");
            }
            //2.校验用户身份，必须为管理员
            if(iUserService.checkAdminRole(user).isSuccess()){
                //3.获取该类别子节点的category信息，并且不递归，保持平级
                return iCategoryService.getChildrenParallelCategory(categoryId);
            }else{
                return ServerResponse.createByErrorMessage("不是管理员,没有权限");
            }
        }

        /**
         *获取当前分类id和递归子节点categoryid
         * @param categoryId
         * @param session
         * @return
         */
        @RequestMapping("get_deep_category.do")
        public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategoty(@RequestParam(value = "categoryId",defaultValue = "0")
                                                                                                Integer categoryId, HttpSession session){
            //1.判断用户是否登陆
            User user = (User)session.getAttribute(Const.CURRENT_USER);
            if(user == null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登录");
            }
            //2.校验用户身份，必须为管理员
            if(iUserService.checkAdminRole(user).isSuccess()){
                //3.获取当前分类id和递归子节点categoryid
                return iCategoryService.getCategoryAndDeepCategoryById(categoryId);
            }else{
                return ServerResponse.createByErrorMessage("不是管理员,没有权限");
            }
        }
}
