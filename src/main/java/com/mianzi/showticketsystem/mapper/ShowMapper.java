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
    int addStock(@Param("showId") Long showId, @Param("quantity") Integer quantity); // <-- 确保新增了这行

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
}


