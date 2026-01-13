package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.Show;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 演出/活动数据访问接口
 */
@Mapper
public interface ShowMapper {

    /**
     * 插入新的演出记录
     * @param show 演出实体对象
     * @return 影响的行数 (1 表示成功)
     */
    int insert(Show show);

    //查询所有演出列表
    List<Show> findAll();

    /**
     * 原子性地减少指定演出的库存
     * @param showId 演出ID
     * @param quantity 减少的数量
     * @return 影响的行数 (1表示成功减少库存，0表示库存不足或演出不存在)
     */
    int updateStock(@Param("showId") Long showId, @Param("quantity") Integer quantity); // <-- 新增这行

    /**
     * 根据ID查询演出详情
     * @param id 演出ID
     * @return 演出对象，如果不存在则返回 null
     */
    Show getById(Long id);

    /**
     * 增加演出库存
     * @param showId 演出ID
     * @param quantity 增加数量
     * @return 影响的行数
     */
    int addStock(@Param("showId") Long showId, @Param("quantity") Integer quantity);

    /**
     * 更新演出信息
     * @param show 包含新信息的演出对象
     * @return 影响的行数 (1 表示成功)
     */
    int update(Show show);

    /**
     * 删除演出信息 (通常是逻辑删除，这里我们实现物理删除以简化)
     * @param id 演出ID
     * @return 影响的行数 (1 表示成功)
     */
    int delete(Long id);

    /**
     * 根据地区和分类查询演出列表（首页使用）
     * @param region 地区（可选，如果为null则查询所有地区）
     * @param category 分类（可选，如果为null则查询所有分类）
     * @param limit 限制数量
     * @return 演出列表
     */
    List<Show> findByRegionAndCategory(@Param("region") String region,
                                       @Param("category") String category,
                                       @Param("limit") Integer limit);

    /**
     * 搜索演出（匹配演出名或场馆名）
     * @param keyword 关键词
     * @return 演出列表
     */
    List<Show> searchShows(@Param("keyword") String keyword);

    /**
     * 条件查询演出（分页）
     * @param region 城市（可选）
     * @param category 分类（可选）
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 演出列表
     */
    List<Show> findShowsByConditions(@Param("region") String region,
                                     @Param("category") String category,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);

    /**
     * 统计条件查询的演出总数
     * @param region 城市（可选）
     * @param category 分类（可选）
     * @return 总数
     */
    long countShowsByConditions(@Param("region") String region,
                                @Param("category") String category);

    /**
     * 管理端 - 分页查询演出列表（条件查询）
     * @param name 演出名称（可选，模糊查询）
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 演出列表
     */
    List<Show> findShowsForAdmin(@Param("name") String name,
                                 @Param("region") String region,
                                 @Param("category") String category,
                                 @Param("status") Integer status,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    /**
     * 管理端 - 统计演出总数（条件查询）
     * @param name 演出名称（可选）
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @return 总数
     */
    long countShowsForAdmin(@Param("name") String name,
                           @Param("region") String region,
                           @Param("category") String category,
                           @Param("status") Integer status);

    /**
     * 管理端 - 根据ID查询演出详情（不限制status）
     * @param id 演出ID
     * @return 演出对象
     */
    Show getByIdForAdmin(Long id);
}


