package com.ygnn.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.ygnn.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author FangKun
 */
@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    /**
     * 在支付宝创建的应用的id
     */
    private String app_id = "2021000116675917";

    /**
     * 商户私钥，您的PKCS8格式RSA2私钥
     */
    private String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7exlqzvp9bFyM/lCHqj5QtA/802STINCyeRjZYVk1T64mROsDuicJS5/PBt7KzI/F2Wo5rvKPZbeMKSCLIDK0OofFEU+HBQA7RLvS0Z0Jt3mLEhKniXeMryR/YZHTIDPcW5yaXo6LcSjyeEK+P6/DRXLNeLmAQoKmSQ7mR3yxGtEeX316jBJmNWw4mrEYK+AHjXr7pvU79iS5967uvZzJ1Q+0Boa07BnJwlH1NpE3lbPXLGznczVaCEQNMbxuSSApZUFCsBJ0ke1/a+57pS7PSQkNRs63bcgJnCZXYQXNeRu38IBJ4UOwEZd+C57n8pQDq59qgvjCZ7T7xZ2TkBcHAgMBAAECggEAZ8KOY6WfzrCdXE8X5K2dBW9SddvNCvybZFtPHGgbRj0OJhH4e7yYBDX9gUfNIDIs1BQrDTe8+Q4Tkpfkcftk9Ih8Qd39xL1IzoSQ6vGl1w3bT9O1zVFwf8UjDvjCCzYEXc26E1mHzLbZMbDLR53ZudoC2qMZD1pAEqXWgC5KXX2GgvE1aEX4JTi+5E20wC9qmkkRqQ+PGFUeE6LyTanmhiptXJPv4F5ZdaWkQ2QA6s4A7SpHr+j9QClCSP0eri/NhQ9mc20o8l4/5qU2nKBji5kAm9DGEGAlPNEx5+Tz6La7k1u3CoAHs8dWnNy1TjVSJExYEUdoG8k4MNyh7EMSGQKBgQDdJZVcyHjHHTtACUWY2qUIgVnZVgxme4Wx5eRt3QE7rNCWpMTHkH48Umh7fQO+k6tg9oDSx6JSe5PhmIJNk5pE69u6za7640O2e0R977FxOYb6DGFakHl+P1cI5Flfi/Q8P2iVVzGkeOxTnbno9PRQahR1osmUyEbTLamF2NKahQKBgQDZBzmgkZW3LdcO76+2Mt41AmADLFMt3ho5siBMOUCGS1itjL0RSbjQpFLbqfoA/5sp0Cc3cDHMVr187bSjGGFnefJwQP1c4VsSmQjHKALX6x0HB34pOT1wm/lAMfTQJbv6xwq2o8jL28XNnk26lj1oyoi9vUD8V+fPtwY6hNwPGwKBgQDJu+cs5JOIz9mk6NBcR9gUkirsX9qgAj5LsnNW5Syiy1rckSIRCnadgG9fdwNbbkoAAd4yaXph0+lq+jyjl+o6xQ2EsuzUYUz7wicQ2v77UocWwwRsIS8zQ4SZz/TXfEwoSY+V7ByU9NpgzJkzMYFNefd/+Cf7WVeCfT0PpGM6IQKBgGd/sJXImBORgtwWpj9Hpvy0s0EQJGLdZrhZIFn5e7IPwKyTT58s/zxRAUrMlvNe8opQQEOJ99WxEBDB88FL4TfNyjaKZ8mhlyMZZDxF8oUyFNfDVQEn0Qsg9w8MQ+n39Nu1jIBpP1so5f4XHo57E7Ij1G5YEOPGsEA40nmZ3rA9AoGBAK62zYuwbjL4UTsVpCcx3OCvU9DHyOr30sP4ExfR484BzGyZRFhzHOnQLAtUbvc4MJ1AJu3T7D/Sgi2mfSr9LEnwvCnzxk/qXxcw9qmqWw+3MpNHZOibtAftoamOMxQ8gLZcqFrTvpWCXMYqDIQXTvgOMlZL/MuCOf7ahyt8J1pS";

    /**
     * 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
     */
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2zpZIDIkRgKO9B9fdRSsB2PuUP2WxLQdFX9woLn19liY6cNF/5nFGaFl50PbsiEgOtKfGpvKhpSODPCSKs9BXJjnWasKq761TK1CYf7TrcyfdDuuKUYn3VKrOL8CCglrKTLVpaXdsJSyAykjdAubcFYnYC0L0exvYsGi256b2H7b3cHEW3o/bfJ8GRm0+5H2/R3TOvGkZTjOPMGn7BM6g7CDx06WqsK6ucl+HmQOE+RPsgp5owUmGVddMnZ1ubrPnef6eaWRXymEwUDKCdJOyA19gze6o8H1BDixpA46xCXh4DBExoy3UOEGJltgQMi2CMX+Kp/e3PfBahR83cCEdQIDAQAB";

    /**
     * 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     * 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
     */
    private String notify_url = "http://kcvjwa.natappfree.cc/payed/notify";

    /**
     * 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     * 同步通知，支付成功，一般跳转到成功页
     */
    private String return_url = "http://member.gulimall.com/memberOrder.html";

    /**
     * 签名方式
     */
    private String sign_type = "RSA2";

    /**
     * 字符编码格式
     */
    private String charset = "utf-8";

    /**
     * 支付宝网关； https://openapi.alipaydev.com/gateway.do
     */
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 订单超时时间
     */
    private String timeout = "1m";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
