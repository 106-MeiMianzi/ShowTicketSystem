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
    //是为了给方法中的多个参数（id 和 userId）取一个明确的“名字”
    //以便 MyBatis 能够将这些 Java 参数的值
    //准确地匹配并替换到 XML 文件中的 SQL 语句占位符（#{...}）中
    Order getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId); // <-- 新增这行
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
}
