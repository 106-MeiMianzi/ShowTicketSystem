package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.Show;

import java.util.List;

/**
 * 演出/活动业务逻辑接口
 */
public interface ShowService {

    /**
     * 发布新的演出/活动
     * @param show 演出实体对象
     * @return 发布成功返回 true，失败返回 false
     */
    boolean publishShow(Show show);

    /**
     * 查询所有已发布的演出列表
     * @return 演出列表
     */
    List<Show> findAllShows(); // <-- 新增这行

    /**
     * 根据ID查询演出详情
     * @param id 演出ID
     * @return 演出对象，如果不存在则返回 null
     */
    Show getShowById(Long id); // <-- 新增这行

    /**
     * 更新演出信息
     * @param show 包含新信息的演出对象
     * @return 成功返回 true，失败返回 false
     */
    boolean updateShow(Show show); // <-- 新增这行

    /**
     * 删除演出信息
     * @param id 演出ID
     * @return 成功返回 true，失败返回 false
     */
    boolean deleteShow(Long id); // <-- 新增这行
}
