package cn.hust.alipay.demo.trade;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-04 19:49
 **/

@RestController
@RequestMapping("/test")
public class Test {

    @RequestMapping("/test.do")
    public String test(){
        return "test";
    }
}
