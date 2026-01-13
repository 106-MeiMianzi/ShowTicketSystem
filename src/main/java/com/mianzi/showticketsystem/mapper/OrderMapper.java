package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List; // 导入 List
/**
 * 订单数据访问接口
 */
//明确告诉框架：“这个接口是一个数据库映射器，请根据它的方法去匹配相应的SQL语句”
//自动生成代理对象（实现类）
//将该接口注册为 Spring 容器中的一个 Bean。
//这意味着你可以在 Service 层通过 @Autowired 或构造函数直接注入 OrderMapper
//而不需要自己去写 new OrderMapperImpl()
@Mapper
public interface OrderMapper {

    /**
     * 插入新的订单记录
     * @param order 订单实体对象
     * @return 影响的行数 (1 表示成功)
     */
    int insert(Order order);

    /**
     * 根据订单ID和用户ID查询订单详情
     * @param id 订单ID
     * @param userId 用户ID
     * @return 订单实体对象
     */
    Order getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据订单ID查询订单详情（用于管理端）
     * @param id 订单ID
     * @return 订单实体对象
     */
    Order getById(@Param("id") Long id);
    /**
     * 更新订单状态，仅允许在特定状态下进行
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
            @Param("expectedStatus") Integer expectedStatus);

    /**
     * 更新订单状态和支付时间
     * @param id 订单ID
     * @param userId 用户ID
     * @param newStatus 新状态
     * @param expectedStatus 期望的旧状态
     * @return 影响的行数 (1 表示成功更新)
     */
    int updateStatusAndPayTime(
                                @Param("id") Long id,
                                @Param("userId") Long userId,
                                @Param("newStatus") Integer newStatus,
                                @Param("expectedStatus") Integer expectedStatus);

    /**
     * 查询指定用户订单的总数 (用于分页)
     * @param userId 用户ID
     * @return 订单总数
     */
    int countOrdersByUserId(@Param("userId") Long userId);

    /**
     * 分页查询指定用户的订单列表
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
     * 查询所有订单的总数 (用于管理端分页)
     * @return 订单总数
     */
    int countAllOrders(); // <-- 新增这行

    /**
     * 分页查询所有订单列表 (用于管理端分页)
     * @param offset 起始记录的偏移量
     * @param limit 每页显示的记录数
     * @return 订单列表
     */
    List<Order> findAllOrders(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit); // <-- 新增这行

    /**
     * 【管理端】强制更新订单状态，不检查旧状态和用户ID
     * @param orderId 订单ID
     * @param newStatus 新状态
     * @return 影响的行数
     */
    int adminUpdateStatus(
            @Param("orderId") Long orderId,
            @Param("newStatus") Integer newStatus);

    /**
     * 根据商户订单号查询订单
     * @param outTradeNo 商户订单号
     * @return 订单对象
     */
    Order getByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * 更新订单的支付宝交易号
     * @param orderId 订单ID
     * @param alipayTradeNo 支付宝交易号
     * @return 影响的行数
     */
    int updateAlipayTradeNo(@Param("orderId") Long orderId, @Param("alipayTradeNo") String alipayTradeNo);

    /**
     * 用户端 - 条件查询订单列表（分页）
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 订单列表
     */
    List<Order> findOrdersByUserIdWithConditions(@Param("userId") Long userId,
                                                 @Param("status") Integer status,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);

    /**
     * 用户端 - 统计条件查询的订单总数
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @return 总数
     */
    long countOrdersByUserIdWithConditions(@Param("userId") Long userId,
                                          @Param("status") Integer status);

    /**
     * 管理端 - 条件查询订单列表（分页）
     * @param userId 用户ID（可选）
     * @param status 订单状态（可选）
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 订单列表
     */
    List<Order> findOrdersForAdmin(@Param("userId") Long userId,
                                  @Param("status") Integer status,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    /**
     * 管理端 - 统计条件查询的订单总数
     * @param userId 用户ID（可选）
     * @param status 订单状态（可选）
     * @return 总数
     */
    long countOrdersForAdmin(@Param("userId") Long userId,
                            @Param("status") Integer status);

    /**
     * 查询待支付的订单（用于掉单补偿和超时关单）
     * @param beforeTime 时间点（查询此时间之前的订单）
     * @return 订单列表
     */
    List<Order> findPendingOrdersBefore(@Param("beforeTime") java.time.LocalDateTime beforeTime);
}
