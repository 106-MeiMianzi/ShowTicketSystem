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

    //注入Mapper和Service
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShowMapper showMapper;

    @Autowired
    private ShowService showService; // 用于查询演出信息

    /**
     * 预订票务的核心方法，确保减库存和创建订单在同一个事务中
     */
    //可读性:这是一个重写的方法
    //如果拼写错误,会立即报错
    @Override
    //例如，在创建订单时，我需要执行插入订单和扣减库存两个数据库操作。
    //如果其中任何一个失败，@Transactional 就能确保 Spring 框架自动将所有已执行的操作回滚
    //防止出现订单创建成功但库存未扣减的数据不一致问题
    @Transactional
    public Order createOrder(Long userId, Long showId, Integer quantity) {

        //步骤 1: 业务校验和获取价格

        //1.1 获取演出信息，用于价格计算
        Show show = showService.getShowById(showId);

        if (show == null) {
            // 演出不存在或状态不正常（getShowById 已在 Mapper 中通过 status = 1 过滤）
            return null;
        }

        //1.2 校验购买数量
        if (quantity == null || quantity <= 0) {
            return null;
        }

        //1.3 校验购买数量是否超出当前可用库存
        //虽然 Mapper 层面会原子性检查，但Service层面也做判断吧
        if (quantity > show.getAvailableTickets()) {
            return null; // 库存不足
        }

        // 1.4 计算总金额
        BigDecimal price = show.getPrice(); // 从数据库获取实时价格
        //BigDecimal 可以精确地表示和计算任何大小和精度的小数，专门用于避免浮点数计算误差
        //计算总金额（价格 * 数量）
        BigDecimal totalPrice = price.multiply(new BigDecimal(quantity));


        //步骤 2: 减库存操作

        // 2.1 调用 ShowMapper 执行原子性减库存操作
        // 如果库存不足或演出状态不对，该操作将返回 0
        int updatedRows = showMapper.updateStock(showId, quantity);

        if (updatedRows == 0) {
            // 减库存失败，可能是并发抢购导致库存不足，直接返回 null
            return null;
        }

        //步骤 3: 创建订单记录

        // 3.1 生成商户订单号（格式：订单前缀 + 时间戳 + 用户ID）
        String outTradeNo = "ORD" + System.currentTimeMillis() + userId;

        // 3.2 构建订单对象
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order()
                .setUserId(userId)
                .setShowId(showId)
                .setOutTradeNo(outTradeNo)
                .setQuantity(quantity)
                .setTotalPrice(totalPrice)
                .setStatus(1) // 1: 待支付
                .setOrderTime(now)
                .setCreateTime(now)
                .setUpdateTime(now);

        // 3.2 插入订单记录
        int result = orderMapper.insert(order);

        if (result == 1) {
            // 订单创建成功,返回订单
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
        if (userId != null) {
            // 用户端查询，需要校验用户ID
            Order order = orderMapper.getByIdAndUserId(orderId, userId);
            // 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
            if (order != null && !order.getUserId().equals(userId)) {
                return null; // 订单不属于当前用户，返回null
            }
            return order;
        } else {
            // 管理端查询，只需要订单ID
            return orderMapper.getById(orderId);
        }
    }

    /**
     * 实现用户取消订单并释放库存的逻辑 (新增)
     */
    @Override
    @Transactional // 确保方法中的数据库操作是原子性的
    public boolean cancelOrder(Long orderId, Long userId) {

        //获取订单信息
        Order order = orderMapper.getByIdAndUserId(orderId, userId);

        if (order == null) {
            // 订单不存在或不属于该用户
            return false;
        }

        // 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
        if (!order.getUserId().equals(userId)) {
            return false; // 订单不属于当前用户
        }

        // 定义订单状态常量
        final int STATUS_PENDING_PAYMENT = 1; // 待支付
        final int STATUS_CANCELED = 4; // 已取消

        //业务校验：只有“待支付”状态的订单才能取消
        if (order.getStatus() != STATUS_PENDING_PAYMENT) {
            // 订单状态不正确（可能已支付、已完成或已取消）
            return false;
        }

        //返还库存操作

        //调用 ShowMapper 增加库存
        // ShowMapper.updateStock 是减库存，我们需要一个对应的增库存方法
        int stockUpdatedRows = showMapper.addStock(order.getShowId(), order.getQuantity());

        if (stockUpdatedRows == 0) {
            // 库存返还失败，可能演出已删除或 ID 错误，应该抛出异常以回滚事务
            throw new RuntimeException("返还库存失败，事务回滚");
        }


        //更新订单状态

        //调用 OrderMapper 更新状态
        int orderUpdatedRows = orderMapper.updateStatus(
                orderId,
                userId,
                STATUS_CANCELED,
                STATUS_PENDING_PAYMENT // 旧状态必须是待支付
        );

        if (orderUpdatedRows == 1) {
            // 订单状态更新成功，事务提交
            return true;
        } else {
            // 订单状态更新失败，抛出异常回滚库存返还
            throw new RuntimeException("更新订单状态失败，事务回滚。");
        }
    }

    /**
     * 模拟支付成功逻辑：更新订单状态和支付时间 (新增)
     */
    @Override
    public boolean payOrder(Long orderId, Long userId) {

        //定义订单状态常量
        final int STATUS_PENDING_PAYMENT = 1; // 待支付
        final int STATUS_PAID = 2;            // 已支付

        // 先查询订单，验证订单是否存在且属于当前用户
        Order order = orderMapper.getByIdAndUserId(orderId, userId);
        if (order == null) {
            return false; // 订单不存在或不属于该用户
        }
        // 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
        if (!order.getUserId().equals(userId)) {
            return false; // 订单不属于当前用户
        }
        // 验证订单状态是否为待支付
        if (order.getStatus() != STATUS_PENDING_PAYMENT) {
            return false; // 订单状态不正确
        }

        //调用 Mapper 更新订单状态和支付时间
        //我们使用 updateStatusAndPayTime 方法，它同时确保：
        //1. 只有 status = 1 (待支付) 的订单才会被更新
        //2. 只有 user_id = userId 的订单才会被更新（权限校验）
        //3. 成功时更新 status, pay_time, update_time 三个字段

        int updatedRows = orderMapper.updateStatusAndPayTime(
                orderId,
                userId,
                STATUS_PAID,            // 新状态：已支付
                STATUS_PENDING_PAYMENT  // 旧状态：待支付
        );

        //返回结果
        return updatedRows == 1;
    }

    /**
     * 实现分页查询指定用户的订单列表的逻辑 (新增)
     */
    @Override
    public PageResult<Order> getUserOrderList(Long userId, int pageNum, int pageSize) {

        //参数校验，确保 pageNum 和 pageSize 有效
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        // 页大小上限限制，防止恶意请求导致数据库压力过大
        if (pageSize > 100) pageSize = 100;

        //计算偏移量 offset(就是跳过用户查询的页数的前几页)
        int offset = (pageNum - 1) * pageSize;

        //查询总记录数
        long total = orderMapper.countOrdersByUserId(userId);

        //如果总记录数为 0，直接返回空结果
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of()); // 使用 List.of() 返回空列表
        }

        //分页查询列表数据
        List<Order> records = orderMapper.findOrdersByUserId(userId, offset, pageSize);

        // 6. 封装为 PageResult 并返回
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 管理端 - 实现分页查询所有订单列表的逻辑
     */
    @Override
    public PageResult<Order> getAllOrderList(int pageNum, int pageSize) {

        // 1. 参数校验，确保 pageNum 和 pageSize 有效
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        // 页大小上限限制，防止恶意请求导致数据库压力过大
        if (pageSize > 100) pageSize = 100;

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
        //参数校验
        if (orderId == null || newStatus == null) {
            return false;
        }

        //调用 管理员专用的 Mapper 方法
        int updatedRows = orderMapper.adminUpdateStatus(orderId, newStatus);

        //提供一个灵活的、强制性的工具，让管理员能够纠正错误或处理异常情况
        //管理员操作涉及人工介入也就是手动操作，我们暂时只实现状态更新，不自动触发库存逻辑
        return updatedRows > 0;
    }

    /**
     * 用户端 - 条件查询订单列表（分页）
     */
    @Override
    public PageResult<Order> getUserOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize) {
        // 参数校验
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        // 页大小上限限制，防止恶意请求导致数据库压力过大
        if (pageSize > 100) pageSize = 100;

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询总记录数
        long total = orderMapper.countOrdersByUserIdWithConditions(userId, status);

        // 如果总记录数为 0，直接返回空结果
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        // 分页查询列表数据
        List<Order> records = orderMapper.findOrdersByUserIdWithConditions(userId, status, offset, pageSize);

        // 封装为 PageResult 并返回
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 管理端 - 条件查询订单列表（分页）
     */
    @Override
    public PageResult<Order> getAllOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize) {
        // 参数校验
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        // 页大小上限限制，防止恶意请求导致数据库压力过大
        if (pageSize > 100) pageSize = 100;

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询总记录数
        long total = orderMapper.countOrdersForAdmin(userId, status);

        // 如果总记录数为 0，直接返回空结果
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        // 分页查询列表数据
        List<Order> records = orderMapper.findOrdersForAdmin(userId, status, offset, pageSize);

        // 封装为 PageResult 并返回
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 根据商户订单号查询订单
     */
    @Override
    public Order getOrderByOutTradeNo(String outTradeNo) {
        return orderMapper.getByOutTradeNo(outTradeNo);
    }
}
