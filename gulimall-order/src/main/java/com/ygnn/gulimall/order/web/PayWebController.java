package com.ygnn.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.ygnn.gulimall.order.config.AlipayTemplate;
import com.ygnn.gulimall.order.service.OrderService;
import com.ygnn.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 1、将支付页让浏览器展示
     * 2、支付成功以后，我们要跳到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

//        PayVo payVo = new PayVo();
//        payVo.setBody();
//        payVo.setOut_trade_no();
//        payVo.setSubject();
//        payVo.setTotal_amount();
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);

        return pay;
    }

}
