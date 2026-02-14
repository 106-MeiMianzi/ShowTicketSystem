package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.Show;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 演出/活动数据访问接口（Mapper）
 * 
 * 作用：
 * - 定义演出相关的数据库操作方法
 * - 提供演出的增删改查功能
 * - 包含库存管理、条件查询、分页查询等功能
 */
@Mapper
/**
 * @Mapper 注解：
 * 标识这是一个 MyBatis Mapper 接口，Spring Boot 会自动创建代理实现类
 */
public interface ShowMapper {

    /**
     * 插入新的演出记录
     * 
     * 用途：管理员发布新演出
     * 
     * @param show 演出实体对象，包含演出的所有信息
     * @return 影响的行数（1 表示成功，0 表示失败）
     * 
     * 说明：
     * - SQL 定义在 resources/mapper/ShowMapper.xml 中
     * - 插入成功后，show.getId() 会返回数据库生成的主键ID
     */
    int insert(Show show);

    /**
     * 查询所有演出列表
     * 
     * 用途：获取所有演出（通常不推荐，数据量大时性能差）
     * 
     * @return 所有演出列表
     * 
     * 注意：
     * - 没有分页，如果数据量大可能影响性能
     * - 建议使用分页查询方法
     */
    List<Show> findAll();

    /**
     * 原子性地减少指定演出的库存
     * 
     * 用途：下单时减少演出库存，确保库存不会超卖
     * 
     * @param showId 演出ID
     * @param quantity 减少的数量（购买数量）
     * @return 影响的行数（1 表示成功减少库存，0 表示库存不足或演出不存在）
     * 
     * 原子性说明：
     * - 使用数据库的原子操作（UPDATE ... WHERE available_tickets >= quantity）
     * - 确保并发情况下不会出现超卖问题
     * - 如果库存不足，SQL 不会执行，返回 0
     * 
     * SQL 示例（在 XML 中）：
     * UPDATE show SET available_tickets = available_tickets - #{quantity}
     * WHERE id = #{showId} AND available_tickets >= #{quantity}
     */
    int updateStock(@Param("showId") Long showId, @Param("quantity") Integer quantity);

    /**
     * 根据ID查询演出详情
     * 
     * 用途：用户端查询演出详情（只查询正常状态的演出）
     * 
     * @param id 演出ID
     * @return 演出对象，如果不存在或已取消则返回 null
     * 
     * 说明：
     * - 用户端查询时，通常只返回正常状态的演出
     * - 已取消的演出不应该显示给用户
     */
    Show getById(Long id);

    /**
     * 增加演出库存
     * 
     * 用途：订单取消或退款时，恢复演出库存
     * 
     * @param showId 演出ID
     * @param quantity 增加数量
     * @return 影响的行数（1 表示成功）
     * 
     * 业务场景：
     * - 用户取消订单：恢复库存
     * - 订单退款：恢复库存
     * - 管理员手动调整库存
     */
    int addStock(@Param("showId") Long showId, @Param("quantity") Integer quantity);

    /**
     * 更新演出信息
     * 
     * 用途：管理员修改演出信息（名称、时间、价格等）
     * 
     * @param show 包含新信息的演出对象，必须包含 id
     * @return 影响的行数（1 表示成功，0 表示演出不存在）
     * 
     * 说明：
     * - SQL 定义在 XML 文件中，通常使用动态 SQL
     * - 只更新非空字段
     * - 更新时会自动更新 update_time
     */
    int update(Show show);

    /**
     * 删除演出信息
     * 
     * 用途：管理员删除演出（物理删除）
     * 
     * @param id 演出ID
     * @return 影响的行数（1 表示成功，0 表示演出不存在）
     * 
     * 注意：
     * - 这是物理删除，数据会从数据库中永久删除
     * - 如果演出有关联订单，删除前需要检查
     * - 通常建议使用逻辑删除（将 status 设为 0）
     */
    int delete(Long id);

    /**
     * 根据地区和分类查询演出列表（首页使用）
     * 
     * 用途：首页推荐演出，按地区和分类筛选
     * 
     * @param region 地区（可选），如果为 null 则查询所有地区
     * @param category 分类（可选），如果为 null 则查询所有分类
     * @param limit 限制数量，限制返回的记录数
     * @return 演出列表
     * 
     * 使用场景：
     * - 首页推荐：显示热门演出
     * - 地区筛选：显示某个城市的演出
     * - 分类筛选：显示某个类型的演出
     */
    List<Show> findByRegionAndCategory(@Param("region") String region,
                                       @Param("category") String category,
                                       @Param("limit") Integer limit);

    /**
     * 搜索演出（匹配演出名或场馆名）
     * 
     * 用途：用户搜索演出，支持模糊匹配
     * 
     * @param keyword 关键词，用于模糊查询演出名称或场馆名称
     * @return 匹配的演出列表
     * 
     * SQL 示例（在 XML 中）：
     * SELECT * FROM show 
     * WHERE (name LIKE CONCAT('%', #{keyword}, '%') 
     *    OR venue LIKE CONCAT('%', #{keyword}, '%'))
     * AND status = 1
     */
    List<Show> searchShows(@Param("keyword") String keyword);

    /**
     * 条件查询演出（分页）
     * 
     * 用途：用户端演出列表页面，支持按地区和分类筛选，支持分页
     * 
     * @param region 城市（可选），如果为 null 则不过滤
     * @param category 分类（可选），如果为 null 则不过滤
     * @param offset 偏移量，计算公式：(pageNum - 1) * pageSize
     * @param limit 每页数量，限制返回的记录数
     * @return 演出列表
     */
    List<Show> findShowsByConditions(@Param("region") String region,
                                     @Param("category") String category,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);

    /**
     * 统计条件查询的演出总数
     * 
     * 用途：配合分页查询，计算符合条件的演出总数
     * 
     * @param region 城市（可选）
     * @param category 分类（可选）
     * @return 符合条件的演出总数
     * 
     * 说明：
     * - 用于计算总页数：pages = total / pageSize
     * - 查询条件与 findShowsByConditions 方法保持一致
     */
    long countShowsByConditions(@Param("region") String region,
                                @Param("category") String category);

    /**
     * 管理端 - 分页查询演出列表（条件查询）
     * 
     * 用途：管理端演出列表页面，支持多条件筛选和分页
     * 
     * @param name 演出名称（可选），用于模糊查询
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选），1=正常，0=已取消
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 演出列表
     * 
     * 说明：
     * - 管理端可以查看所有状态的演出（包括已取消的）
     * - 用户端只能查看正常状态的演出
     */
    List<Show> findShowsForAdmin(@Param("name") String name,
                                 @Param("region") String region,
                                 @Param("category") String category,
                                 @Param("status") Integer status,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    /**
     * 管理端 - 统计演出总数（条件查询）
     * 
     * 用途：配合分页查询，计算符合条件的演出总数
     * 
     * @param name 演出名称（可选）
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @return 符合条件的演出总数
     */
    long countShowsForAdmin(@Param("name") String name,
                           @Param("region") String region,
                           @Param("category") String category,
                           @Param("status") Integer status);

    /**
     * 管理端 - 根据ID查询演出详情（不限制status）
     * 
     * 用途：管理端查询演出详情，可以查看已取消的演出
     * 
     * @param id 演出ID
     * @return 演出对象，如果不存在则返回 null
     * 
     * 说明：
     * - 与 getById 的区别：不限制 status，可以查询已取消的演出
     * - 管理端需要查看所有状态的演出详情
     */
    Show getByIdForAdmin(Long id);
}
