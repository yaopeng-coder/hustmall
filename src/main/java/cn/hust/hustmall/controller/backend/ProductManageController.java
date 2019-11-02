package cn.hust.hustmall.controller.backend;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.dto.ProductDetailDTO;
import cn.hust.hustmall.pojo.Product;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IFileService;
import cn.hust.hustmall.service.IProductService;
import cn.hust.hustmall.service.IUserService;
import cn.hust.hustmall.util.PropertiesUtil;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 09:40
 **/

@RestController
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IFileService iFileService;

    /**
     *新增或者更新产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("/save.do")
    public ServerResponse<String> productSave(HttpSession session, Product product){
        //1.检查是否登陆
         User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
         if(currentUser == null){
                return ServerResponse.createByErrorMessage("未登录，请先登陆");
         }
        //2.检察是否为管理员

        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //3.进行新增或者更新产品操作
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }

    /**
     * 修改产品上下架
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("/set_sale_status.do")
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer productId, Integer status){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //3.进行新增或者更新产品操作
            return iProductService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }

    /**
     * 查看商品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("/detail.do")
    public ServerResponse<ProductDetailDTO> productDetail(HttpSession session, Integer productId){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //3.进行查看产品详情的操作
           return  iProductService.manageProductDetail(productId);

        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }

    /**
     *查看商品分页列表
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse<PageInfo> getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                            @RequestParam(value = "pageSize" ,defaultValue = "10") Integer pageSize){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //3.进行查看产品详情的操作
        return iProductService.getProductList(pageNum,pageSize);

        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }


    /**
     * 后台根据商品名或者ID实现模糊查询
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/search.do")
    public ServerResponse<PageInfo> searchProduct(HttpSession session, String productName,Integer productId,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                  @RequestParam(value = "pageSize" ,defaultValue = "10") Integer pageSize){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员

        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //3.进行模糊查询商品的操作
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);

        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }
    }

    /**
     * springmvc文件上传，需要配合dispatcher.xml中关于上传文件的配置，MultipartFile才有用
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("/upload.do")
    public ServerResponse<Map<String,String>> upload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file , HttpServletRequest request){
        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("未登录，请先登陆");
        }
        //2.检察是否为管理员
        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //3.1上传文件

            String uploadFilePath = this.getClass().getClassLoader().getResource("").getPath();
          //  String contextPath = request.getSession().getServletContext().getRealPath("upload");
            //3.2返回上传的文件名
            String targetFileName = iFileService.upload(file, uploadFilePath);
            //3.3将文件的uri和url返回给前端
            Map<String ,String> fileMap = new HashMap<>();
            fileMap.put("uri",targetFileName);
            fileMap.put("url", PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName);
            return ServerResponse.createBySuccess(fileMap);

        }else{
            return ServerResponse.createByErrorMessage("不是管理员，无权限操作");
        }


 /*       ServletContext serverContext = request.getSession(true).getServletContext();
        String contextPath2 = request.getSession(true).getServletContext().getRealPath("/");
        String realPath = request.getServletContext().getRealPath("/");
        String realPath1 = request.getSession().getServletContext().getRealPath
                (request.getRequestURI());
        String realPath2 = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath("/");
        String contextPath = request.getSession(true).getServletContext().getRealPath("upload");*/


    }

    /**
     * 富文本上传文件
     * @param session
     * @param file
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/richtext_img_upload.do")
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file , HttpServletRequest request, HttpServletResponse response){

       /* 富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
        {
            "success": true/false,
                "msg": "error message", # optional
            "file_path": "[real file path]"
        }*/

        //1.检查是否登陆
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        Map resultMap = Maps.newHashMap();
        if(currentUser == null){
                resultMap.put("success",false);
                resultMap.put("msg","请先登陆管理员");
            return resultMap;
        }
        //2.检察是否为管理员
        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //3.1上传文件

            String uploadFilePath = this.getClass().getClassLoader().getResource("").getPath();
        //    String contextPath = request.getSession().getServletContext().getRealPath("upload");
            //3.2返回上传的文件名
            String targetFileName = iFileService.upload(file, uploadFilePath);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            //3.3将文件的uri和url返回给前端
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path", PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName);
            return resultMap;

        }else{
            resultMap.put("success",false);
            resultMap.put("msg","没有权限上传");
            return resultMap;
        }

    }





}
