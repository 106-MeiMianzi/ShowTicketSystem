package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 订单数据访问接口（Mapper）
 * 
 * 作用：
 * - 定义订单相关的数据库操作方法
 * - 提供订单的增删改查功能
 * - 包含订单状态管理、支付处理、分页查询等功能
 * 
 * 安全说明：
 * - 用户端操作必须验证 userId，防止越权访问
 * - 管理端操作可以查询所有订单
 */
@Mapper
/**
 * @Mapper 注解说明：
 * 
 * 明确告诉框架："这个接口是一个数据库映射器，请根据它的方法去匹配相应的SQL语句"
 * 
 * 作用：
 * - 自动生成代理对象（实现类）
 * - 将该接口注册为 Spring 容器中的一个 Bean
 * - 可以在 Service 层通过 @Autowired 或构造函数直接注入 OrderMapper
 * - 而不需要自己去写 new OrderMapperImpl()
 */
public interface OrderMapper {

    /**
     * 插入新的订单记录
     * 
     * 用途：用户下单时创建订单
     * 
     * @param order 订单实体对象，包含订单的所有信息
     * @return 影响的行数（1 表示成功，0 表示失败）
     * 
     * 说明：
     * - SQL 定义在 resources/mapper/OrderMapper.xml 中
     * - 插入成功后，order.getId() 会返回数据库生成的主键ID
     * - 插入时会自动填充 order_time、create_time、update_time
     */
    int insert(Order order);

    /**
     * 根据订单ID和用户ID查询订单详情
     * 
     * 用途：用户端查询订单详情，并验证订单属于指定用户
     * 
     * @param id 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 订单实体对象，如果不存在或不属于该用户则返回 null
     * 
     * 安全说明：
     * - 必须同时匹配 id 和 userId，防止用户查询其他用户的订单
     * - 这是防止越权访问的重要措施
     */
    Order getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据订单ID查询订单详情（用于管理端）
     * 
     * 用途：管理端查询订单详情，不需要验证用户ID
     * 
     * @param id 订单ID
     * @return 订单实体对象，如果不存在则返回 null
     * 
     * 说明：
     * - 管理端可以查询所有订单，不需要验证 userId
     * - 与 getByIdAndUserId 的区别：不限制用户
     */
    Order getById(@Param("id") Long id);

    /**
     * 更新订单状态，仅允许在特定状态下进行
     * 
     * 用途：用户取消订单、支付成功后更新状态等
     * 
     * @param id 订单ID
     * @param userId 用户ID（用于权限校验）
     * @param newStatus 新状态（1=待支付, 2=已支付, 3=已取消, 4=已退款）
     * @param expectedStatus 期望的旧状态（只有当前状态匹配时才更新）
     * @return 影响的行数（1 表示成功更新，0 表示状态不匹配或订单不存在）
     * 
     * 状态流转说明：
     * - 待支付(1) -> 已支付(2)：支付成功
     * - 待支付(1) -> 已取消(3)：用户取消或超时
     * - 已支付(2) -> 已退款(4)：退款操作
     * 
     * 原子性说明：
     * - 使用 WHERE 条件确保状态匹配：WHERE id = #{id} AND user_id = #{userId} AND status = #{expectedStatus}
     * - 确保并发情况下状态更新的正确性
     */
    int updateStatus(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("newStatus") Integer newStatus,
            @Param("expectedStatus") Integer expectedStatus);

    /**
     * 更新订单状态和支付时间
     * 
     * 用途：支付成功后，同时更新订单状态和支付时间
     * 
     * @param id 订单ID
     * @param userId 用户ID（用于权限校验）
     * @param newStatus 新状态（通常是 2=已支付）
     * @param expectedStatus 期望的旧状态（通常是 1=待支付）
     * @return 影响的行数（1 表示成功更新）
     * 
     * 说明：
     * - 支付成功时，需要同时更新 status 和 pay_time
     * - 使用原子操作确保数据一致性
     */
    int updateStatusAndPayTime(
                                @Param("id") Long id,
                                @Param("userId") Long userId,
                                @Param("newStatus") Integer newStatus,
                                @Param("expectedStatus") Integer expectedStatus);

    /**
     * 查询指定用户订单的总数（用于分页）
     * 
     * 用途：用户端订单列表分页，计算该用户的订单总数
     * 
     * @param userId 用户ID
     * @return 订单总数
     * 
     * 说明：
     * - 用于计算总页数：pages = total / pageSize
     * - 只统计该用户的订单
     */
    int countOrdersByUserId(@Param("userId") Long userId);

    /**
     * 分页查询指定用户的订单列表
     * 
     * 用途：用户端订单列表页面，显示该用户的所有订单
     * 
     * @param userId 用户ID
     * @param offset 起始记录的偏移量，计算公式：(页码-1) * 每页大小
     * @param limit 每页显示的记录数
     * @return 订单列表
     * 
     * 分页说明：
     * - offset：跳过前面的记录数
     * - limit：返回的记录数
     * - 例如：第2页，每页10条 -> offset=10, limit=10
     */
    List<Order> findOrdersByUserId(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 查询所有订单的总数（用于管理端分页）
     * 
     * 用途：管理端订单列表分页，计算所有订单的总数
     * 
     * @return 订单总数
     * 
     * 说明：
     * - 管理端可以查看所有用户的订单
     * - 用于计算总页数
     */
    int countAllOrders();

    /**
     * 分页查询所有订单列表（用于管理端分页）
     * 
     * 用途：管理端订单列表页面，显示所有订单
     * 
     * @param offset 起始记录的偏移量
     * @param limit 每页显示的记录数
     * @return 订单列表
     */
    List<Order> findAllOrders(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 【管理端】强制更新订单状态，不检查旧状态和用户ID
     * 
     * 用途：管理员手动修改订单状态（如手动退款、强制取消等）
     * 
     * @param orderId 订单ID
     * @param newStatus 新状态
     * @return 影响的行数（1 表示成功）
     * 
     * 说明：
     * - 管理端有更高权限，可以强制更新订单状态
     * - 不检查旧状态，可以任意修改
     * - 不检查用户ID，可以操作所有订单
     * 
     * 使用场景：
     * - 手动退款：将订单状态改为已退款
     * - 强制取消：将订单状态改为已取消
     */
    int adminUpdateStatus(
            @Param("orderId") Long orderId,
            @Param("newStatus") Integer newStatus);

    /**
     * 根据商户订单号查询订单
     * 
     * 用途：支付回调时，根据商户订单号查找订单
     * 
     * @param outTradeNo 商户订单号（系统生成的唯一订单号）
     * @return 订单对象，如果不存在则返回 null
     * 
     * 使用场景：
     * - 支付成功回调：根据商户订单号查找订单并更新状态
     * - 查询订单状态：根据商户订单号查询订单详情
     */
    Order getByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * 更新订单的支付宝交易号
     * 
     * 用途：支付成功后，保存支付宝返回的交易号
     * 
     * @param orderId 订单ID
     * @param alipayTradeNo 支付宝交易号（支付宝返回的唯一交易号）
     * @return 影响的行数（1 表示成功）
     * 
     * 说明：
     * - 支付宝交易号用于查询支付详情和退款
     * - 支付成功后需要保存这个交易号
     */
    int updateAlipayTradeNo(@Param("orderId") Long orderId, @Param("alipayTradeNo") String alipayTradeNo);

    /**
     * 用户端 - 条件查询订单列表（分页）
     * 
     * 用途：用户端订单列表，支持按状态筛选和分页
     * 
     * @param userId 用户ID（必须）
     * @param status 订单状态（可选），如果为 null 则查询所有状态
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 订单列表
     * 
     * 说明：
     * - 只查询该用户的订单
     * - 支持按状态筛选（如只查看待支付订单）
     */
    List<Order> findOrdersByUserIdWithConditions(@Param("userId") Long userId,
                                                 @Param("status") Integer status,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);

    /**
     * 用户端 - 统计条件查询的订单总数
     * 
     * 用途：配合分页查询，计算符合条件的订单总数
     * 
     * @param userId 用户ID（必须）
     * @param status 订单状态（可选）
     * @return 总数
     */
    long countOrdersByUserIdWithConditions(@Param("userId") Long userId,
                                          @Param("status") Integer status);

    /**
     * 管理端 - 条件查询订单列表（分页）
     * 
     * 用途：管理端订单列表，支持多条件筛选和分页
     * 
     * @param userId 用户ID（可选），如果为 null 则查询所有用户
     * @param status 订单状态（可选），如果为 null 则查询所有状态
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 订单列表
     * 
     * 说明：
     * - 管理端可以查询所有用户的订单
     * - 支持按用户ID和状态筛选
     */
    List<Order> findOrdersForAdmin(@Param("userId") Long userId,
                                  @Param("status") Integer status,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    /**
     * 管理端 - 统计条件查询的订单总数
     * 
     * 用途：配合分页查询，计算符合条件的订单总数
     * 
     * @param userId 用户ID（可选）
     * @param status 订单状态（可选）
     * @return 总数
     */
    long countOrdersForAdmin(@Param("userId") Long userId,
                            @Param("status") Integer status);

    /**
     * 查询待支付的订单（用于掉单补偿和超时关单）
     * 
     * 用途：定时任务查询超时的待支付订单，自动取消
     * 
     * @param beforeTime 时间点，查询此时间之前的订单
     * @return 订单列表
     * 
     * 业务场景：
     * - 掉单补偿：支付回调失败时，定时查询并补偿
     * - 超时关单：订单创建超过一定时间未支付，自动取消
     * 
     * SQL 示例（在 XML 中）：
     * SELECT * FROM order 
     * WHERE status = 1 AND order_time < #{beforeTime}
     */
    List<Order> findPendingOrdersBefore(@Param("beforeTime") java.time.LocalDateTime beforeTime);
}
