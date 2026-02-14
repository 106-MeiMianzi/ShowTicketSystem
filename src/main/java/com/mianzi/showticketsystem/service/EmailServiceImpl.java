package com.mianzi.showticketsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 邮件服务实现类
 * 
 * 作用：
 * - 实现EmailService接口定义的所有业务逻辑方法
 * - 使用Spring Mail发送邮件
 * 
 * 职责：
 * - 发送支付成功通知邮件
 * - 可以扩展其他邮件通知功能
 * 
 * 说明：
 * - 依赖Spring Mail（spring-boot-starter-mail）
 * - 需要在application.properties中配置邮件服务器信息
 * - 如果未配置邮件服务，方法会静默失败（不影响主流程）
 */
@Service
/**
 * @Service 注解：
 * - 标识这是一个Service层的Bean
 * - Spring会自动扫描并创建这个类的实例
 */
public class EmailServiceImpl implements EmailService {

    /**
     * 注入邮件发送器
     * 
     * @Autowired(required = false) 注解说明：
     * - required = false 表示如果找不到Bean，不会抛出异常
     * - 如果邮件服务未配置，mailSender为null
     * - 方法内部会检查mailSender是否为null
     */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * 注入发件人邮箱
     * 
     * @Value 注解说明：
     * - 从application.properties中读取配置值
     * - ${spring.mail.username:} 表示如果配置文件中没有，则使用空字符串
     * 
     * 说明：
     * - 发件人邮箱需要在application.properties中配置
     * - 例如：spring.mail.username=your_email@qq.com
     */
    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * 实现发送支付成功通知邮件的逻辑
     * 
     * 功能说明：
     * - 用户支付成功后，发送邮件通知
     * - 包含订单信息和演出信息
     * 
     * @param to 收件人邮箱
     * @param orderNo 订单号
     * @param showName 演出名称
     * @param totalPrice 订单总金额
     * 
     * 说明：
     * - 如果邮件服务未配置，方法会静默失败（不影响主流程）
     * - 邮件发送失败不应该影响订单处理流程
     */
    @Override
    public void sendPaymentSuccessEmail(String to, String orderNo, String showName, BigDecimal totalPrice) {
        /**
         * 步骤1：检查邮件服务是否配置
         * 
         * 如果mailSender为null或fromEmail为空，说明邮件服务未配置
         * 只打印日志，不发送邮件
         */
        if (mailSender == null || fromEmail == null || fromEmail.isEmpty()) {
            /**
             * 邮件服务未配置，只打印日志（实际项目中应该记录日志）
             * 
             * 不影响主流程，静默失败
             */
            System.out.println("邮件服务未配置，跳过发送邮件到: " + to);
            return;
        }

        try {
            /**
             * 步骤2：创建邮件消息对象
             * 
             * SimpleMailMessage：简单邮件消息（纯文本邮件）
             * 如果需要发送HTML邮件，需要使用MimeMessage
             */
            SimpleMailMessage message = new SimpleMailMessage();
            
            /**
             * 设置发件人邮箱
             */
            message.setFrom(fromEmail);
            
            /**
             * 设置收件人邮箱
             */
            message.setTo(to);
            
            /**
             * 设置邮件主题
             */
            message.setSubject("抢票成功通知 - " + showName);
            
            /**
             * 设置邮件内容
             * 
             * String.format()：格式化字符串
             * %s：字符串占位符
             */
            message.setText(String.format(
                    "恭喜您！抢票成功！\n\n" +
                            "订单号：%s\n" +
                            "演出名称：%s\n" +
                            "订单金额：%s 元\n\n" +
                            "感谢您的使用！",
                    orderNo, showName, totalPrice
            ));
            
            /**
             * 步骤3：发送邮件
             * 
             * mailSender.send()：发送邮件
             * 如果发送失败，会抛出异常
             */
            mailSender.send(message);
        } catch (Exception e) {
            /**
             * 步骤4：捕获异常，记录错误日志
             * 
             * 邮件发送失败不应该影响主流程，只记录异常
             * 实际项目中应该使用日志框架（如Log4j、Logback）记录日志
             */
            System.err.println("发送邮件失败: " + e.getMessage());
        }
    }
}
