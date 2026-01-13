package com.mianzi.showticketsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * 实现发送支付成功通知邮件的逻辑
     */
    @Override
    public void sendPaymentSuccessEmail(String to, String orderNo, String showName, BigDecimal totalPrice) {
        if (mailSender == null || fromEmail == null || fromEmail.isEmpty()) {
            // 如果没有配置邮件服务，只打印日志（实际项目中应该记录日志）
            System.out.println("邮件服务未配置，跳过发送邮件到: " + to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("抢票成功通知 - " + showName);
            message.setText(String.format(
                    "恭喜您！抢票成功！\n\n" +
                            "订单号：%s\n" +
                            "演出名称：%s\n" +
                            "订单金额：%s 元\n\n" +
                            "感谢您的使用！",
                    orderNo, showName, totalPrice
            ));
            mailSender.send(message);
        } catch (Exception e) {
            // 邮件发送失败不应该影响主流程，只记录异常
            System.err.println("发送邮件失败: " + e.getMessage());
        }
    }
}

