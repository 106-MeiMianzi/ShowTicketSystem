package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.PageResult;
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
    List<Show> findAllShows();

    /**
     * 根据ID查询演出详情
     * @param id 演出ID
     * @return 演出对象，如果不存在则返回 null
     */
    Show getShowById(Long id);

    /**
     * 更新演出信息
     * @param show 包含新信息的演出对象
     * @return 成功返回 true，失败返回 false
     */
    boolean updateShow(Show show);

    /**
     * 删除演出信息
     * @param id 演出ID
     * @return 成功返回 true，失败返回 false
     */
    boolean deleteShow(Long id);

    /**
     * 在首页获取地区和分类演出列表
     * @param region 地区（可选，null则返回默认北京地区）
     * @param category 分类（可选，null则返回所有分类）
     * @param limit 限制数量
     * @return 演出列表
     */
    List<Show> getHomeShows(String region, String category, Integer limit);

    /**
     * 搜索演出（匹配演出名或场馆名）
     * @param keyword 关键词
     * @return 演出列表
     */
    List<Show> searchShows(String keyword);

    /**
     * 条件查询演出（分页）
     * @param region 城市
     * @param category 分类
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<Show> findShowsByConditions(String region, String category, int pageNum, int pageSize);

    /**
     * 管理端 - 分页查询演出列表（条件查询）
     * @param name 演出名称（可选）
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<Show> getShowListForAdmin(String name, String region, String category, Integer status, int pageNum, int pageSize);

    /**
     * 管理端 - 查询演出详情（包含明确库存数字）
     * @param id 演出ID
     * @return 演出对象
     */
    Show getShowByIdForAdmin(Long id);
}
