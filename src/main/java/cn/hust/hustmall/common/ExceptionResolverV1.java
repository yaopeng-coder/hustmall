package cn.hust.hustmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-12 16:33
 **/

@Component
@Slf4j
public class ExceptionResolverV1 implements HandlerExceptionResolver{

    /**
     *用实现HandlerExceptionResolver接口的方式来进行全局异常处理
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @param e
     * @return
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

        log.error("发生异常的URL:{},exception",httpServletRequest.getRequestURI(),e);

        //因为项目中用到jakson是1.9,所以用了MappingJacksonJsonView，若用了2.x，则用MappingJackson2JsonView
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView() );
        modelAndView.addObject("status",ResponseCode.ERROR.getCode());
        modelAndView.addObject("message","接口异常，请查看服务端日志信息v1");
        modelAndView.addObject("data",e.toString());
        return modelAndView;

    }
}
