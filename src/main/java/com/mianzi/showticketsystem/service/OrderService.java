package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.PageResult; // 确保导入 PageResult
/**
 * 订单业务逻辑接口
 */
public interface OrderService {

    /**
     * 用户预订/购买演出票
     * 这是一个核心方法，需要保证原子性和事务性（即：减库存和创建订单必须同时成功或同时失败）
     * * @param userId 购票用户ID
     * @param showId 演出ID
     * @param quantity 购买数量
     * @return 成功返回订单对象，失败返回 null
     */
    Order createOrder(Long userId, Long showId, Integer quantity);

    /**
     * 根据订单ID和用户ID查询订单详情
     * @param orderId 订单ID
     * @param userId 用户ID（可为null，用于管理端查询）
     * @return 订单对象，如果不存在或不属于该用户则返回 null
     */
    Order getOrderDetails(Long orderId, Long userId);

    /**
     * 用户取消订单，并释放库存
     * 这是一个核心方法，需要保证事务性。
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 成功返回 true，失败返回 false
     */
    boolean cancelOrder(Long orderId, Long userId);

    /**
     * 模拟支付成功逻辑：更新订单状态和支付时间
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 成功返回 true，失败返回 false
     */
    boolean payOrder(Long orderId, Long userId);

    /**
     * 分页查询指定用户的订单列表
     * @param userId 用户ID
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    PageResult<Order> getUserOrderList(Long userId, int pageNum, int pageSize);

    /**
     * 用户端 - 条件查询订单列表（分页）
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    PageResult<Order> getUserOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 管理端 - 分页查询所有订单列表
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 包含所有订单列表的分页结果对象
     */
    PageResult<Order> getAllOrderList(int pageNum, int pageSize);

    /**
     * 管理端 - 条件查询订单列表（分页）
     * @param userId 用户ID（可选）
     * @param status 订单状态（可选）
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    PageResult<Order> getAllOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 根据商户订单号查询订单
     * @param outTradeNo 商户订单号
     * @return 订单对象
     */
    Order getOrderByOutTradeNo(String outTradeNo);

    /**
     * 管理端 - 手动更新订单状态
     * @param orderId 订单ID
     * @param newStatus 新状态 (例如：1=待支付, 2=已支付, 4=已取消)
     * @return 成功返回 true，失败返回 false
     */
    boolean updateOrderStatus(Long orderId, Integer newStatus); // <-- 新增这行
}
