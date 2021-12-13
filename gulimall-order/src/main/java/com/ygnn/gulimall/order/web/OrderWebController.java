package com.ygnn.gulimall.order.web;

import com.ygnn.common.exception.NoStockException;
import com.ygnn.gulimall.order.service.OrderService;
import com.ygnn.gulimall.order.vo.OrderConfirmVo;
import com.ygnn.gulimall.order.vo.OrderSubmitVo;
import com.ygnn.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author FangKun
 */
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    /**
     * 去结算确认页
     * @param model
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        // 展示订单确认的数据
        return "confirm";
    }

    /**
     * 下单功能
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        // 下单: 去创建订单、验令牌、验价格、锁库存...
        // 下单成功来到支付选择页
        // 下单失败回到订单确认页重新确认订单信息

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode() == 0) {
                // 下单成功来到支付选择页
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            } else {
                String msg = "下单失败: ";
                switch (responseVo.getCode()){
                    case 1: msg += "订单信息过期，请刷新再次提交"; break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交"; break;
                    case 3: msg += "库存锁定失败，商品库存不足"; break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e){
            if (e instanceof NoStockException){
                redirectAttributes.addFlashAttribute("msg", e.getMessage());
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

}
