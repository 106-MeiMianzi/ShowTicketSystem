package com.mianzi.showticketsystem.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.mianzi.showticketsystem.config.AlipayConfig;
import com.mianzi.showticketsystem.mapper.OrderMapper;
import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.mapper.UserMapper;
import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付服务实现类（支付宝沙箱环境）
 * 
 * 作用：
 * - 实现PaymentService接口定义的所有业务逻辑方法
 * - 处理支付相关的业务逻辑
 * 
 * 说明：
 * - 集成支付宝SDK实现真实支付功能
 * - 包含定时任务：掉单补偿和超时关单
 */
@Service
/**
 * @Service 注解：
 * - 标识这是一个Service层的Bean
 * - Spring会自动扫描并创建这个类的实例
 */
public class PaymentServiceImpl implements PaymentService {

    /**
     * 注入支付宝客户端
     * 
     * 用于调用支付宝API（创建支付订单、查询订单状态等）
     */
    @Autowired
    private AlipayClient alipayClient;

    /**
     * 注入支付宝配置
     * 
     * 用于获取回调地址等配置信息
     */
    @Autowired
    private AlipayConfig alipayConfig;

    /**
     * 注入订单Mapper
     * 
     * 用于执行订单相关的数据库操作
     */
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 注入邮件服务
     * 
     * 用于发送支付成功通知邮件
     */
    @Autowired
    private EmailService emailService;

    /**
     * 注入演出Mapper
     * 
     * 用于恢复演出库存（超时关单时）
     */
    @Autowired
    private ShowMapper showMapper;

    /**
     * 注入用户Mapper
     * 
     * 用于查询用户信息（发送邮件时）
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 实现创建支付订单的逻辑（集成支付宝SDK）
     * 
     * 功能说明：
     * - 用户创建支付订单
     * - 调用支付宝SDK创建支付订单
     * - 返回支付页面HTML（前端可以直接展示）
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 包含支付信息的Map，如果失败返回null
     */
    @Override
    public Map<String, String> createPayment(Long orderId, Long userId) {
        try {
            /**
             * 步骤1：查询订单并验证权限
             */
            Order order = orderMapper.getByIdAndUserId(orderId, userId);
            if (order == null || order.getStatus() != 1) {
                /**
                 * 订单不存在或状态不对（只有待支付订单才能支付）
                 */
                return null;
            }
            
            /**
             * 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
             */
            if (!order.getUserId().equals(userId)) {
                /**
                 * 订单不属于当前用户
                 */
                return null;
            }

            /**
             * 步骤2：查询演出信息，用于支付页面显示
             */
            Show show = showMapper.getById(order.getShowId());
            String subject = show != null ? show.getName() : "演出票务";

            /**
             * 步骤3：调用支付宝SDK创建支付订单
             */
            
            /**
             * 步骤3.1：创建支付请求对象
             */
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            
            /**
             * 步骤3.2：设置支付完成后的回调地址
             * 
             * notifyUrl：支付完成后，支付宝会调用这个地址通知支付结果（必须）
             * returnUrl：用户支付完成后跳转的地址（可选）
             */
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            if (alipayConfig.getReturnUrl() != null && !alipayConfig.getReturnUrl().isEmpty()) {
                request.setReturnUrl(alipayConfig.getReturnUrl());
            }

            /**
             * 步骤3.3：构建支付业务参数
             */
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(order.getOutTradeNo());  // 商户订单号（必填）
            model.setTotalAmount(order.getTotalPrice().toString());  // 订单总金额（必填）
            model.setSubject(subject);  // 订单标题（必填）
            model.setProductCode("FAST_INSTANT_TRADE_PAY");  // 产品码（固定值，表示电脑网站支付）
            
            /**
             * 步骤3.4：设置业务参数到请求对象
             */
            request.setBizModel(model);

            /**
             * 步骤3.5：调用支付宝API，获取支付页面HTML
             * 
             * pageExecute()：执行请求并返回HTML页面
             * 前端可以直接将这个HTML展示给用户，用户扫码或输入密码完成支付
             */
            String payHtml = alipayClient.pageExecute(request).getBody();

            /**
             * 步骤4：返回支付信息
             */
            Map<String, String> result = new HashMap<>();
            result.put("payHtml", payHtml);  // 支付页面HTML（前端可以直接展示）
            result.put("orderNo", order.getOutTradeNo());  // 商户订单号
            result.put("message", "支付订单创建成功，请完成支付");
            return result;
        } catch (Exception e) {
            /**
             * 捕获异常，记录错误日志
             */
            System.err.println("创建支付订单失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 实现处理支付宝支付回调的逻辑
     * 
     * 功能说明：
     * - 接收支付宝的支付回调通知
     * - 验证签名，确保回调来自支付宝
     * - 更新订单状态和支付信息
     * - 发送支付成功邮件通知
     * 
     * @param params 支付宝回调参数（包含订单号、交易号、支付状态等）
     * @return 处理结果（"success"或"failure"）
     */
    @Override
    @Transactional
    /**
     * @Transactional 注解：
     * - 确保支付回调处理的所有数据库操作在同一个事务中执行
     * - 如果任何操作失败，所有操作都会回滚
     */
    public String handleAlipayCallback(Map<String, String> params) {
        try {
            /**
             * 步骤1：验证签名，确保回调请求来自支付宝
             * 
             * 这是非常重要的安全措施，防止伪造回调
             */
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,                                    // 回调参数
                    alipayConfig.getAlipayPublicKey(),         // 支付宝公钥
                    AlipayConstants.CHARSET_UTF8,              // 字符编码
                    AlipayConstants.SIGN_TYPE_RSA2             // 签名算法
            );

            if (!signVerified) {
                /**
                 * 签名验证失败，可能是伪造的回调请求
                 * 记录日志并返回失败
                 */
                System.err.println("支付宝回调签名验证失败");
                return "failure";
            }

            /**
             * 步骤2：从回调参数中提取关键信息
             */
            String outTradeNo = params.get("out_trade_no");      // 商户订单号
            String tradeStatus = params.get("trade_status");      // 交易状态
            String tradeNo = params.get("trade_no");              // 支付宝交易号

            if (outTradeNo == null) {
                /**
                 * 商户订单号为空，返回失败
                 */
                return "failure";
            }

            /**
             * 步骤3：根据商户订单号查询订单
             */
            Order order = orderMapper.getByOutTradeNo(outTradeNo);
            if (order == null) {
                /**
                 * 订单不存在，返回失败
                 */
                return "failure";
            }

            /**
             * 步骤4：如果订单已支付，直接返回成功
             * 
             * 防止重复处理回调
             */
            if (order.getStatus() == 2) {
                /**
                 * 订单已支付，直接返回成功
                 */
                return "success";
            }

            /**
             * 步骤5：处理支付成功
             * 
             * 支付宝交易状态：
             * - TRADE_SUCCESS：交易成功（即时到账）
             * - TRADE_FINISHED：交易完成（担保交易）
             */
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                /**
                 * 步骤5.1：更新订单状态和支付时间
                 */
                orderMapper.updateStatusAndPayTime(
                        order.getId(),
                        order.getUserId(),
                        2, // 已支付
                        1  // 待支付
                );

                /**
                 * 步骤5.2：更新支付宝交易号
                 * 
                 * 保存支付宝返回的交易号，用于后续查询和退款
                 */
                orderMapper.updateAlipayTradeNo(order.getId(), tradeNo);

                /**
                 * 步骤5.3：发送邮件通知（需要查询用户和演出信息）
                 */
                User user = userMapper.selectById(order.getUserId());
                if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                    /**
                     * 查询演出信息
                     */
                    Show show = showMapper.getById(order.getShowId());
                    String showName = show != null ? show.getName() : "演出票务";
                    
                    /**
                     * 发送支付成功邮件
                     */
                    emailService.sendPaymentSuccessEmail(
                            user.getEmail(),
                            order.getOutTradeNo(),
                            showName,
                            order.getTotalPrice()
                    );
                }

                /**
                 * 处理成功，返回"success"
                 * 
                 * 支付宝收到"success"后，不会再回调此订单
                 */
                return "success";
            }

            /**
             * 其他交易状态（如TRADE_CLOSED等），返回失败
             */
            return "failure";
        } catch (Exception e) {
            /**
             * 捕获所有异常，记录错误日志
             * 
             * 支付回调处理失败不应该影响主流程
             * 支付宝会继续回调，直到收到"success"
             */
            System.err.println("处理支付回调失败: " + e.getMessage());
            return "failure";
        }
    }

    /**
     * 掉单补偿：查询待支付订单并检查支付宝状态
     * 
     * 功能说明：
     * - 定时任务，定期检查待支付订单
     * - 调用支付宝API查询订单状态
     * - 如果订单在支付宝中已支付，但系统未更新，则更新订单状态
     * - 补偿支付回调丢失的情况
     */
    @Override
    @Scheduled(fixedRate = 300000)
    /**
     * @Scheduled 注解说明：
     * 
     * Spring提供的定时任务注解
     * 
     * fixedRate = 300000：
     * - 每5分钟执行一次（300000毫秒 = 5分钟）
     * - 从上次执行开始时间计算，固定间隔执行
     * 
     * 其他选项：
     * - fixedDelay：从上次执行结束时间计算
     * - cron：使用cron表达式（更灵活）
     */
    @Transactional
    public void compensatePendingOrders() {
        try {
            /**
             * 步骤1：查询30分钟前的待支付订单
             * 
             * 只检查创建时间超过30分钟的订单
             * 避免检查刚创建的订单
             */
            LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(30);
            List<Order> pendingOrders = orderMapper.findPendingOrdersBefore(beforeTime);

            System.out.println("掉单补偿任务执行，待处理订单数: " + pendingOrders.size());

            /**
             * 步骤2：遍历每个订单，查询支付宝状态
             */
            for (Order order : pendingOrders) {
                try {
                    /**
                     * 步骤2.1：调用支付宝API查询订单状态
                     */
                    AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                    AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                    model.setOutTradeNo(order.getOutTradeNo());  // 商户订单号
                    request.setBizModel(model);

                    /**
                     * 步骤2.2：执行查询请求
                     */
                    AlipayTradeQueryResponse response = alipayClient.execute(request);

                    /**
                     * 步骤2.3：检查查询结果
                     */
                    if (response.isSuccess()) {
                        /**
                         * 查询成功，获取交易状态
                         */
                        String tradeStatus = response.getTradeStatus();

                        /**
                         * 步骤2.4：如果订单在支付宝中已支付，但系统未更新，则更新订单状态
                         */
                        if (("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))
                                && order.getStatus() == 1) {
                            /**
                             * 订单在支付宝中已支付，但系统状态还是待支付
                             * 执行补偿逻辑：更新订单状态、保存交易号、发送邮件
                             */
                            orderMapper.updateStatusAndPayTime(
                                    order.getId(),
                                    order.getUserId(),
                                    2, // 已支付
                                    1  // 待支付
                            );

                            /**
                             * 更新支付宝交易号
                             */
                            String tradeNo = response.getTradeNo();
                            if (tradeNo != null) {
                                orderMapper.updateAlipayTradeNo(order.getId(), tradeNo);
                            }

                            /**
                             * 发送邮件通知
                             */
                            User user = userMapper.selectById(order.getUserId());
                            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                                Show show = showMapper.getById(order.getShowId());
                                String showName = show != null ? show.getName() : "演出票务";
                                
                                emailService.sendPaymentSuccessEmail(
                                        user.getEmail(),
                                        order.getOutTradeNo(),
                                        showName,
                                        order.getTotalPrice()
                                );
                            }

                            System.out.println("掉单补偿成功，订单号: " + order.getOutTradeNo());
                        }
                    } else {
                        /**
                         * 查询失败，记录日志
                         */
                        System.err.println("查询支付宝订单状态失败，订单号: " + order.getOutTradeNo() 
                                + ", 错误信息: " + response.getSubMsg());
                    }
                } catch (Exception e) {
                    /**
                     * 单个订单处理失败，记录日志但不影响其他订单
                     */
                    System.err.println("掉单补偿处理订单失败，订单号: " + order.getOutTradeNo() 
                            + ", 错误: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            /**
             * 定时任务执行失败，记录日志
             */
            System.err.println("掉单补偿任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 超时关单：关闭超过30分钟未支付的订单
     * 
     * 功能说明：
     * - 定时任务，定期检查超时的待支付订单
     * - 先查询支付宝订单状态，确认未支付后再关闭
     * - 关闭超过30分钟未支付的订单
     * - 恢复演出库存
     * 
     * 注意：
     * - 必须先查询支付宝订单状态，避免关闭已支付但回调失败的订单
     * - 如果订单在支付宝中已支付，执行掉单补偿逻辑
     */
    @Override
    @Scheduled(fixedRate = 600000)
    /**
     * @Scheduled 注解：
     * - fixedRate = 600000：每10分钟执行一次（600000毫秒 = 10分钟）
     */
    @Transactional
    /**
     * @Transactional 注解：
     * - 确保关闭订单和恢复库存在同一个事务中执行
     */
    public void closeTimeoutOrders() {
        try {
            /**
             * 步骤1：查询30分钟前的待支付订单
             */
            LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(30);
            List<Order> pendingOrders = orderMapper.findPendingOrdersBefore(beforeTime);

            System.out.println("超时关单任务执行，待处理订单数: " + pendingOrders.size());

            /**
             * 步骤2：遍历每个订单，先查询支付宝状态再决定是否关闭
             */
            for (Order order : pendingOrders) {
                try {
                    /**
                     * 步骤2.1：调用支付宝API查询订单状态
                     */
                    AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                    AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                    model.setOutTradeNo(order.getOutTradeNo());  // 商户订单号
                    request.setBizModel(model);

                    /**
                     * 步骤2.2：执行查询请求
                     */
                    AlipayTradeQueryResponse response = alipayClient.execute(request);

                    /**
                     * 步骤2.3：检查查询结果
                     */
                    if (response.isSuccess()) {
                        String tradeStatus = response.getTradeStatus();

                        /**
                         * 步骤2.4：如果订单在支付宝中已支付，执行掉单补偿逻辑
                         */
                        if (("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))
                                && order.getStatus() == 1) {
                            /**
                             * 订单在支付宝中已支付，但系统状态还是待支付
                             * 执行补偿逻辑：更新订单状态、保存交易号、发送邮件
                             */
                            orderMapper.updateStatusAndPayTime(
                                    order.getId(),
                                    order.getUserId(),
                                    2, // 已支付
                                    1  // 待支付
                            );

                            /**
                             * 更新支付宝交易号
                             */
                            String tradeNo = response.getTradeNo();
                            if (tradeNo != null) {
                                orderMapper.updateAlipayTradeNo(order.getId(), tradeNo);
                            }

                            /**
                             * 发送邮件通知
                             */
                            User user = userMapper.selectById(order.getUserId());
                            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                                Show show = showMapper.getById(order.getShowId());
                                String showName = show != null ? show.getName() : "演出票务";
                                
                                emailService.sendPaymentSuccessEmail(
                                        user.getEmail(),
                                        order.getOutTradeNo(),
                                        showName,
                                        order.getTotalPrice()
                                );
                            }

                            System.out.println("超时关单任务中发现已支付订单，执行补偿，订单号: " + order.getOutTradeNo());
                            continue;  // 已支付，跳过关闭操作
                        }

                        /**
                         * 步骤2.5：如果订单在支付宝中未支付或交易关闭，执行关闭操作
                         */
                        if ("WAIT_BUYER_PAY".equals(tradeStatus) || "TRADE_CLOSED".equals(tradeStatus)) {
                            /**
                             * 订单在支付宝中未支付或已关闭
                             * 执行关闭操作：更新订单状态、恢复库存
                             */
                            orderMapper.updateStatus(order.getId(), order.getUserId(), 3, 1);
                            
                            /**
                             * 返还库存
                             */
                            showMapper.addStock(order.getShowId(), order.getQuantity());
                            
                            System.out.println("超时关单成功，订单号: " + order.getOutTradeNo());
                        }
                    } else {
                        /**
                         * 查询失败，记录日志
                         * 为了安全起见，不关闭订单（可能是网络问题导致查询失败）
                         */
                        System.err.println("查询支付宝订单状态失败，订单号: " + order.getOutTradeNo() 
                                + ", 错误信息: " + response.getSubMsg());
                    }
                } catch (Exception e) {
                    /**
                     * 单个订单处理失败，记录日志但不影响其他订单
                     */
                    System.err.println("超时关单处理订单失败，订单号: " + order.getOutTradeNo() 
                            + ", 错误: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            /**
             * 定时任务执行失败，记录日志
             */
            System.err.println("超时关单任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
