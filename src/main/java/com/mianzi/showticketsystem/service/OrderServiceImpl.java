package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.OrderMapper;
import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.Show; // 确保导入了 Show 实体
import com.mianzi.showticketsystem.service.OrderService;
import com.mianzi.showticketsystem.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.mianzi.showticketsystem.model.entity.PageResult; // 确保导入 PageResult
import java.util.List; // 确保导入 List

@Service
public class OrderServiceImpl implements OrderService {

    // 注入必要的 Mapper 和 Service
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShowMapper showMapper; // 用于减库存

    @Autowired
    private ShowService showService; // 用于查询演出信息

    /**
     * 预订票务的核心方法，确保减库存和创建订单在同一个事务中
     */
    @Override
    @Transactional // 确保方法中的数据库操作是原子性的
    public Order createOrder(Long userId, Long showId, Integer quantity) {

        // --- 步骤 1: 业务校验和获取价格 ---

        // 1.1 获取演出信息，用于价格计算 (<<<<< 主要修改开始 >>>>>)
        Show show = showService.getShowById(showId);

        if (show == null) {
            // 演出不存在或状态不正常（getShowById 已在 Mapper 中通过 status = 1 过滤）
            return null;
        }

        // 1.2 简单校验购买数量
        if (quantity == null || quantity <= 0) {
            return null;
        }

        // 1.3 校验购买数量是否超出当前可用库存 (虽然 Mapper 层面会原子性检查，但 Service 层面也做判断更友好)
        if (quantity > show.getAvailableTickets()) {
            return null; // 库存不足
        }

        // 1.4 计算总金额
        BigDecimal price = show.getPrice(); // 从数据库获取实时价格
        BigDecimal totalPrice = price.multiply(new BigDecimal(quantity));
        // (<<<<< 主要修改结束 >>>>>)


        // --- 步骤 2: 关键的减库存操作（并发控制）---

        // 2.1 调用 ShowMapper 执行原子性减库存操作
        // 如果库存不足或演出状态不对，该操作将返回 0
        int updatedRows = showMapper.updateStock(showId, quantity);

        if (updatedRows == 0) {
            // 减库存失败，可能是并发抢购导致库存不足，直接返回 null
            return null;
        }

        // --- 步骤 3: 创建订单记录 ---

        // 3.1 构建订单对象
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order()
                .setUserId(userId)
                .setShowId(showId)
                .setQuantity(quantity)
                .setTotalPrice(totalPrice)
                .setStatus(1) // 1: 待支付
                .setOrderTime(now)
                .setCreateTime(now)
                .setUpdateTime(now);

        // 3.2 插入订单记录
        int result = orderMapper.insert(order);

        if (result == 1) {
            // 订单创建成功，事务提交，返回订单
            return order;
        } else {
            // 订单插入失败，抛出异常触发事务回滚，库存也会恢复
            throw new RuntimeException("创建订单失败，事务回滚。");
        }
    }

    /**
     * 实现根据订单ID和用户ID查询订单详情的逻辑 (新增)
     */
    @Override
    public Order getOrderDetails(Long orderId, Long userId) {
        // 直接调用 Mapper 层的方法，确保用户只能查询自己的订单
        return orderMapper.getByIdAndUserId(orderId, userId);
    }

    /**
     * 实现用户取消订单并释放库存的逻辑 (新增)
     */
    @Override
    @Transactional // 确保方法中的数据库操作是原子性的
    public boolean cancelOrder(Long orderId, Long userId) {

        // --- 步骤 1: 获取订单信息 ---
        Order order = orderMapper.getByIdAndUserId(orderId, userId);

        if (order == null) {
            // 订单不存在或不属于该用户
            return false;
        }

        // 定义订单状态常量
        final int STATUS_PENDING_PAYMENT = 1; // 待支付
        final int STATUS_CANCELED = 4; // 已取消 (假设 4 为取消状态)

        // 1.1 业务校验：只有“待支付”状态的订单才能取消
        if (order.getStatus() != STATUS_PENDING_PAYMENT) {
            // 订单状态不正确（可能已支付、已完成或已取消）
            // 提示：实际项目中，应该返回更详细的错误信息
            return false;
        }

        // --- 步骤 2: 关键的返还库存操作 ---

        // 2.1 调用 ShowMapper 增加库存
        // ShowMapper.updateStock 是减库存，我们需要一个对应的增库存方法，但为了快速，
        // 我们可以复用 ShowMapper.updateStock 的逻辑（传入负数）
        // ⚠️ 最佳实践是创建一个单独的 addStock 方法
        // 假设我们修改 ShowMapper.updateStock 的逻辑为通用的库存调整

        // 🚨 鉴于我们之前 ShowMapper.updateStock 逻辑是 **减** 库存并校验 `available_tickets >= #{quantity}`，
        // 我们需要新增一个**增库存**方法，或者调整现有的 `updateStock`。

        // 为了不破坏之前的并发抢购逻辑，我们创建一个**增加库存**的 SQL 语句。

        // 💡 暂时跳过对 ShowMapper 的修改，我们假设 `ShowMapper` 中有一个 **`addStock`** 方法。
        // 请注意：你需要在下一步中补上 `ShowMapper.java` 接口和 `ShowMapper.xml` 的 `addStock` 方法！

        // 假设已添加 addStock 方法：
        int stockUpdatedRows = showMapper.addStock(order.getShowId(), order.getQuantity());

        if (stockUpdatedRows == 0) {
            // 库存返还失败，可能演出已删除或 ID 错误，应该抛出异常以回滚事务
            throw new RuntimeException("返还库存失败，事务回滚。");
        }


        // --- 步骤 3: 更新订单状态 ---

        // 3.1 调用 OrderMapper 更新状态
        int orderUpdatedRows = orderMapper.updateStatus(
                orderId,
                userId,
                STATUS_CANCELED,
                STATUS_PENDING_PAYMENT // 期望旧状态必须是待支付
        );

        if (orderUpdatedRows == 1) {
            // 订单状态更新成功，事务提交
            return true;
        } else {
            // 订单状态更新失败（可能是并发操作，或状态已改变），抛出异常回滚库存返还
            throw new RuntimeException("更新订单状态失败，事务回滚。");
        }
    }

    /**
     * 模拟支付成功逻辑：更新订单状态和支付时间 (新增)
     */
    /**
     * 模拟支付成功逻辑：更新订单状态和支付时间 (新增)
     */
    @Override
    public boolean payOrder(Long orderId, Long userId) {

        // --- 步骤 1: 定义订单状态常量 ---
        final int STATUS_PENDING_PAYMENT = 1; // 待支付
        final int STATUS_PAID = 2;            // 已支付 (假设 2 为已支付状态)

        // --- 步骤 2: 调用 Mapper 更新订单状态和支付时间 ---
        // 我们使用 updateStatusAndPayTime 方法，它同时确保：
        // 1. 只有 status = 1 (待支付) 的订单才会被更新（乐观锁/状态机校验）。
        // 2. 只有 user_id = userId 的订单才会被更新（权限校验）。
        // 3. 成功时更新 status, pay_time, update_time 三个字段。

        int updatedRows = orderMapper.updateStatusAndPayTime(
                orderId,
                userId,
                STATUS_PAID,            // 新状态：已支付
                STATUS_PENDING_PAYMENT  // 期望旧状态：待支付
        );

        // --- 步骤 3: 返回结果 ---
        return updatedRows == 1;
    }

    /**
     * 实现分页查询指定用户的订单列表的逻辑 (新增)
     */
    @Override
    public PageResult<Order> getUserOrderList(Long userId, int pageNum, int pageSize) {

        // 1. 参数校验，确保 pageNum 和 pageSize 有效
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;

        // 2. 计算偏移量 offset
        int offset = (pageNum - 1) * pageSize;

        // 3. 查询总记录数
        long total = orderMapper.countOrdersByUserId(userId);

        // 4. 如果总记录数为 0，直接返回空结果
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of()); // 使用 List.of() 返回空列表
        }

        // 5. 分页查询列表数据
        List<Order> records = orderMapper.findOrdersByUserId(userId, offset, pageSize);

        // 6. 封装为 PageResult 并返回
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 管理端 - 实现分页查询所有订单列表的逻辑 (新增)
     */
    @Override
    public PageResult<Order> getAllOrderList(int pageNum, int pageSize) {

        // 1. 参数校验，确保 pageNum 和 pageSize 有效
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;

        // 2. 计算偏移量 offset
        int offset = (pageNum - 1) * pageSize;

        // 3. 查询总记录数 (调用 countAllOrders)
        long total = orderMapper.countAllOrders();

        // 4. 如果总记录数为 0，直接返回空结果
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        // 5. 分页查询列表数据 (调用 findAllOrders)
        List<Order> records = orderMapper.findAllOrders(offset, pageSize);

        // 6. 封装为 PageResult 并返回
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 管理端 - 手动更新订单状态的逻辑 (新增)
     */
    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, Integer newStatus) {
        // 1. 参数校验 (实际项目需要更严格的校验，确保状态转换合理)
        if (orderId == null || newStatus == null) {
            return false;
        }

        // 2. 调用 管理员专用的 Mapper 方法
        // int updatedRows = orderMapper.updateStatus(orderId, newStatus); // ❌ 之前的错误调用
        int updatedRows = orderMapper.adminUpdateStatus(orderId, newStatus); // ✅ 正确调用

        // 3. 提示：如果状态改为“已取消(4)”，理论上应该返还库存，
        //    但管理员操作涉及人工判断，我们暂时只实现状态更新，不自动触发库存逻辑。
        return updatedRows > 0;
    }
}
