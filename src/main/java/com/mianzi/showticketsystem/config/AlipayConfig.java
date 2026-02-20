package com.mianzi.showticketsystem.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 * 
 * 作用：
 * - 读取application.properties中的支付宝配置
 * - 创建支付宝客户端（AlipayClient）
 * - 供支付服务使用
 */
@Configuration
/**
 * @Configuration 注解：
 * - 标识这是一个Spring配置类
 * - Spring Boot会自动扫描并加载这个类
 */
public class AlipayConfig {

    /**
     * 支付宝应用ID（APPID）
     * 
     * 从application.properties中读取：alipay.appId
     */
    @Value("${alipay.appId}")
    private String appId;

    /**
     * 应用私钥（RSA2私钥）
     * 
     * 从application.properties中读取：alipay.privateKey
     * 
     * 说明：
     * - 用于签名请求参数
     * - 必须保密，不要泄露
     */
    @Value("${alipay.privateKey}")
    private String privateKey;

    /**
     * 支付宝公钥（RSA2公钥）
     * 
     * 从application.properties中读取：alipay.publicKey
     * 
     * 说明：
     * - 用于验证支付宝回调的签名
     * - 确保回调请求来自支付宝
     */
    @Value("${alipay.publicKey}")
    private String alipayPublicKey;

    /**
     * 支付宝网关地址
     * 
     * 从application.properties中读取：alipay.serverUrl
     * 
     * 沙箱环境：https://openapi.alipaydev.com/gateway.do
     * 正式环境：https://openapi.alipay.com/gateway.do
     */
    @Value("${alipay.serverUrl}")
    private String serverUrl;

    /**
     * 支付回调地址（notify_url）
     * 
     * 从application.properties中读取：alipay.notifyUrl
     * 
     * 说明：
     * - 支付完成后，支付宝会调用这个地址通知支付结果
     * - 必须是公网可访问的地址
     * - 本地测试可以使用内网穿透工具（如ngrok）
     */
    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    /**
     * 支付完成后的跳转地址（return_url）
     * 
     * 从application.properties中读取：alipay.returnUrl
     * 
     * 说明：
     * - 用户支付完成后，会跳转到这个地址
     * - 可选配置
     */
    @Value("${alipay.returnUrl:}")
    private String returnUrl;

    /**
     * 签名算法类型
     * 
     * 固定值：RSA2
     * 
     * 说明：
     * - 支付宝推荐使用RSA2签名算法
     * - 更安全，支持更长的密钥
     */
    private static final String SIGN_TYPE = "RSA2";

    /**
     * 字符编码格式
     * 
     * 固定值：UTF-8
     */
    private static final String CHARSET = "UTF-8";

    /**
     * 数据格式
     * 
     * 固定值：JSON
     */
    private static final String FORMAT = "JSON";

    /**
     * 创建支付宝客户端Bean
     * 
     * 作用：
     * - 封装支付宝API调用
     * - 自动处理签名、加密等操作
     * 
     * @return AlipayClient实例
     * 
     * 使用方式：
     * - 在Service中通过@Autowired注入
     * - 调用alipayClient.execute()方法调用支付宝API
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                serverUrl,      // 支付宝网关地址
                appId,          // 应用ID
                privateKey,     // 应用私钥
                FORMAT,         // 数据格式：JSON
                CHARSET,        // 字符编码：UTF-8
                alipayPublicKey,// 支付宝公钥
                SIGN_TYPE       // 签名算法：RSA2
        );
    }

    /**
     * 获取支付回调地址
     * 
     * @return 回调地址
     */
    public String getNotifyUrl() {
        return notifyUrl;
    }

    /**
     * 获取支付完成后的跳转地址
     * 
     * @return 跳转地址
     */
    public String getReturnUrl() {
        return returnUrl;
    }

    /**
     * 获取支付宝公钥
     * 
     * @return 支付宝公钥
     */
    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }
}
