package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.PageResult;

/**
 * 订单业务逻辑接口
 * 
 * 作用：
 * - 定义订单相关的业务逻辑方法
 * - 提供订单的创建、查询、取消、支付等功能
 * - 由OrderServiceImpl实现具体的业务逻辑
 * 
 * 职责：
 * - 订单创建和管理
 * - 订单状态管理
 * - 库存管理（下单减库存，取消还库存）
 * - 分页查询
 */
public interface OrderService {

    /**
     * 用户预订/购买演出票
     * 
     * 功能说明：
     * - 创建订单并减少演出库存
     * - 这是一个核心方法，需要保证原子性和事务性
     * - 减库存和创建订单必须同时成功或同时失败
     * 
     * @param userId 购票用户ID
     * @param showId 演出ID
     * @param quantity 购买数量
     * @return 成功返回订单对象，失败返回null
     * 
     * 业务逻辑：
     * 1. 查询演出信息并验证库存
     * 2. 计算订单总金额
     * 3. 原子性减少库存
     * 4. 创建订单记录
     */
    Order createOrder(Long userId, Long showId, Integer quantity);

    /**
     * 根据订单ID和用户ID查询订单详情
     * 
     * 功能说明：
     * - 用户端查询订单详情（需要验证用户ID）
     * - 管理端查询订单详情（userId可为null）
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（可为null，用于管理端查询）
     * @return 订单对象，如果不存在或不属于该用户则返回null
     */
    Order getOrderDetails(Long orderId, Long userId);

    /**
     * 用户取消订单，并释放库存
     * 
     * 功能说明：
     * - 用户取消自己的订单
     * - 恢复演出库存
     * - 这是一个核心方法，需要保证事务性
     * - 更新订单状态和恢复库存必须同时成功或同时失败
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 成功返回true，失败返回false
     * 
     * 业务规则：
     * - 只有"待支付"状态的订单才能取消
     * - 取消后订单状态变为"已取消"
     * - 取消后恢复演出库存
     */
    boolean cancelOrder(Long orderId, Long userId);

    /**
     * 模拟支付成功逻辑：更新订单状态和支付时间
     * 
     * 功能说明：
     * - 用户支付订单（简化版，直接更新状态）
     * - 实际项目中应该调用支付接口
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 成功返回true，失败返回false
     * 
     * 业务规则：
     * - 只有"待支付"状态的订单才能支付
     * - 支付后订单状态变为"已支付"
     * - 记录支付时间
     */
    boolean payOrder(Long orderId, Long userId);

    /**
     * 分页查询指定用户的订单列表
     * 
     * 功能说明：
     * - 用户端查询自己的订单列表
     * - 支持分页查询
     * 
     * @param userId 用户ID
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    PageResult<Order> getUserOrderList(Long userId, int pageNum, int pageSize);

    /**
     * 用户端 - 条件查询订单列表（分页）
     * 
     * 功能说明：
     * - 用户端查询自己的订单列表，支持按状态筛选
     * - 支持分页查询
     * 
     * @param userId 用户ID
     * @param status 订单状态（可选），1=待支付，2=已支付，3=已取消，4=已退款
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    PageResult<Order> getUserOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 管理端 - 分页查询所有订单列表
     * 
     * 功能说明：
     * - 管理端查询所有订单
     * - 支持分页查询
     * 
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含所有订单列表的分页结果对象
     */
    PageResult<Order> getAllOrderList(int pageNum, int pageSize);

    /**
     * 管理端 - 条件查询订单列表（分页）
     * 
     * 功能说明：
     * - 管理端查询订单，支持多条件筛选
     * - 支持分页查询
     * 
     * @param userId 用户ID（可选），如果为null则查询所有用户
     * @param status 订单状态（可选），1=待支付，2=已支付，3=已取消，4=已退款
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    PageResult<Order> getAllOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 根据商户订单号查询订单
     * 
     * 功能说明：
     * - 支付回调时根据商户订单号查找订单
     * - 用于支付处理
     * 
     * @param outTradeNo 商户订单号（系统生成的唯一订单号）
     * @return 订单对象，如果不存在则返回null
     */
    Order getOrderByOutTradeNo(String outTradeNo);

    /**
     * 管理端 - 手动更新订单状态
     * 
     * 功能说明：
     * - 管理员手动修改订单状态
     * - 用于处理异常订单（如手动退款、强制取消等）
     * 
     * @param orderId 订单ID
     * @param newStatus 新状态（例如：1=待支付, 2=已支付, 3=已取消, 4=已退款）
     * @return 成功返回true，失败返回false
     * 
     * 说明：
     * - 管理端有更高权限，可以强制更新订单状态
     * - 不检查旧状态，可以任意修改
     */
    boolean updateOrderStatus(Long orderId, Integer newStatus);
}
