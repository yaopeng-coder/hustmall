package cn.hust.hustmall.controller.common.interceptor;

import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IUserService;
import cn.hust.hustmall.util.CookieUtil;
import cn.hust.hustmall.util.JsonUtil;
import cn.hust.hustmall.util.RedisShardedPoolUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;



/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-13 08:23
 **/
@Slf4j
public class BackendAuthorityInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        //1.得到请求controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) o;
        String methodName = handlerMethod.getMethod().getName();

        //2.解析方法名，得到其控制器类名
        String className = handlerMethod.getBean().getClass().getSimpleName();

        //3.解析request，得到参数
        Map<String, String[]> map = httpServletRequest.getParameterMap();
        Iterator<Map.Entry<String, String[]>> iterator = map.entrySet().iterator();
        StringBuffer requestParamString = new StringBuffer();
        while(iterator.hasNext()){

            Map.Entry<String, String[]> entry = iterator.next();
            String key = entry.getKey();
            String[] value = entry.getValue();

            String param = StringUtils.EMPTY;
            for(int i = 0;i<value.length;i++){
                param = (i == value.length-1)? param + value[i]:param + value[i] + ",";
            }
            requestParamString.append(key).append("=").append(param);
        }

        //处理登陆死循环问题，将登陆从拦截器中去除
        if(StringUtils.equals(className,"UserManageController") && StringUtils.equals(methodName,"login")){
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            log.info("controller的名字：{}，方法的名字：{}",className,methodName);
            return true;
        }

        log.info("controller的名字：{}，方法的名字：{},请求的参数:{}",className,methodName,requestParamString.toString());

        //4.对用户进行逻辑业务处理
        String loginToken = CookieUtil.readLoginCookie(httpServletRequest);
        User user = null;
        if(loginToken != null) {

            String loginUserJson = RedisShardedPoolUtil.get(loginToken);
            user =JsonUtil.string2Object(loginUserJson, User.class);

        }

        //5.对response进行处理，返回json对象
        if(user == null || !userService.checkAdminRole(user).isSuccess()){
            //1.首先对response重置，否则会抛异常，getWriter() has already been called for this response.
            httpServletResponse.reset();//
            //2.设置编码，否则会乱码
            httpServletResponse.setCharacterEncoding("UTF-8");
            //3.设置返回类型
            httpServletResponse.setContentType("application/json;charset=UTF-8");

            //4.设置返回对象，转换成json格式
            PrintWriter writer = httpServletResponse.getWriter();

            if(user == null){

                //这里要对富文本上传进行特殊处理，因为他对返回的格式有特定的要求
                if(StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richtextImgUpload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","请先登陆管理员");
                    writer.write(JsonUtil.obj2String(resultMap));
                }else{
                    writer.write(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，用户尚未登陆")));
                }

            }else {
                if(StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richtextImgUpload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","没有权限上传");
                    writer.write(JsonUtil.obj2String(resultMap));
                }else {
                    writer.write(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，用户没有权限")));
                }
            }

            writer.flush();
            writer.close();

            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
