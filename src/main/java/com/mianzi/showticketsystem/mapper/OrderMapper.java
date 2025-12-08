package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List; // 导入 List
/**
 * 订单数据访问接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入新的订单记录
     * @param order 订单实体对象
     * @return 影响的行数 (1 表示成功)
     */
    int insert(Order order);

    /**
     * 根据订单ID和用户ID查询订单详情 (新增)
     * @param id 订单ID
     * @param userId 用户ID
     * @return 订单实体对象
     */
    Order getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId); // <-- 新增这行
    /**
     * 更新订单状态，仅允许在特定状态下进行 (新增)
     * @param id 订单ID
     * @param userId 用户ID
     * @param newStatus 新状态
     * @param expectedStatus 期望的旧状态
     * @return 影响的行数 (1 表示成功更新)
     */
    int updateStatus(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("newStatus") Integer newStatus,
            @Param("expectedStatus") Integer expectedStatus); // <-- 新增这行

    /**
     * 更新订单状态和支付时间 (新增)
     * @param id 订单ID
     * @param userId 用户ID
     * @param newStatus 新状态
     * @param expectedStatus 期望的旧状态
     * @return 影响的行数 (1 表示成功更新)
     */
    int updateStatusAndPayTime( // <-- 新增这个方法
                                @Param("id") Long id,
                                @Param("userId") Long userId,
                                @Param("newStatus") Integer newStatus,
                                @Param("expectedStatus") Integer expectedStatus);

    /**
     * 查询指定用户订单的总数 (用于分页) (新增)
     * @param userId 用户ID
     * @return 订单总数
     */
    int countOrdersByUserId(@Param("userId") Long userId); // <-- 新增这行

    /**
     * 分页查询指定用户的订单列表 (新增)
     * @param userId 用户ID
     * @param offset 起始记录的偏移量 (例如：(页码-1) * 每页大小)
     * @param limit 每页显示的记录数
     * @return 订单列表
     */
    List<Order> findOrdersByUserId(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit); // <-- 新增这行

    /**
     * 查询所有订单的总数 (用于管理端分页) (新增)
     * @return 订单总数
     */
    int countAllOrders(); // <-- 新增这行

    /**
     * 分页查询所有订单列表 (用于管理端分页) (新增)
     * @param offset 起始记录的偏移量
     * @param limit 每页显示的记录数
     * @return 订单列表
     */
    List<Order> findAllOrders(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit); // <-- 新增这行

    /**
     * 【管理端】强制更新订单状态，不检查旧状态和用户ID (新增)
     * @param orderId 订单ID
     * @param newStatus 新状态
     * @return 影响的行数
     */
    int adminUpdateStatus(
            @Param("orderId") Long orderId,
            @Param("newStatus") Integer newStatus); // <-- 新增此方法
}
