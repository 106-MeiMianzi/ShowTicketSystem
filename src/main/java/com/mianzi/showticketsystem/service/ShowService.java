package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.model.entity.Show;

import java.util.List;

/**
 * 演出/活动业务逻辑接口
 * 
 * 作用：
 * - 定义演出相关的业务逻辑方法
 * - 提供演出的增删改查功能
 * - 由ShowServiceImpl实现具体的业务逻辑
 * 
 * 职责：
 * - 演出发布和管理
 * - 演出查询和搜索
 * - 分页查询
 */
public interface ShowService {

    /**
     * 发布新的演出/活动
     * 
     * 功能说明：
     * - 管理员发布新演出
     * - 初始化库存（availableTickets = totalTickets）
     * - 设置状态为正常可售
     * 
     * @param show 演出实体对象，包含演出的所有信息
     * @return 发布成功返回true，失败返回false
     */
    boolean publishShow(Show show);

    /**
     * 查询所有已发布的演出列表
     * 
     * 功能说明：
     * - 查询所有正常状态的演出
     * - 用户端使用，不包含已取消的演出
     * 
     * @return 演出列表
     */
    List<Show> findAllShows();

    /**
     * 根据ID查询演出详情
     * 
     * 功能说明：
     * - 用户端查询演出详情
     * - 只返回正常状态的演出
     * 
     * @param id 演出ID
     * @return 演出对象，如果不存在或已取消则返回null
     */
    Show getShowById(Long id);

    /**
     * 更新演出信息
     * 
     * 功能说明：
     * - 管理员修改演出信息（名称、时间、价格等）
     * - 可以更新部分字段（只更新非空字段）
     * 
     * @param show 包含新信息的演出对象，必须包含id
     * @return 成功返回true，失败返回false
     */
    boolean updateShow(Show show);

    /**
     * 删除演出信息
     * 
     * 功能说明：
     * - 管理员删除演出（物理删除）
     * - 删除前需要检查是否有关联订单
     * 
     * @param id 演出ID
     * @return 成功返回true，失败返回false
     */
    boolean deleteShow(Long id);

    /**
     * 在首页获取地区和分类演出列表
     * 
     * 功能说明：
     * - 首页推荐演出
     * - 支持按地区和分类筛选
     * - 支持限制返回数量
     * 
     * @param region 地区（可选，null则返回默认北京地区）
     * @param category 分类（可选，null则返回所有分类）
     * @param limit 限制数量（可选）
     * @return 演出列表
     */
    List<Show> getHomeShows(String region, String category, Integer limit);

    /**
     * 搜索演出（匹配演出名或场馆名）
     * 
     * 功能说明：
     * - 支持模糊搜索演出名称或场馆名称
     * - 用户端使用，公开接口
     * 
     * @param keyword 关键词
     * @return 演出列表
     */
    List<Show> searchShows(String keyword);

    /**
     * 条件查询演出（分页）
     * 
     * 功能说明：
     * - 用户端演出列表页面
     * - 支持按地区和分类筛选
     * - 支持分页查询
     * 
     * @param region 城市（可选）
     * @param category 分类（可选）
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页结果，包含演出列表和分页信息
     */
    PageResult<Show> findShowsByConditions(String region, String category, int pageNum, int pageSize);

    /**
     * 管理端 - 分页查询演出列表（条件查询）
     * 
     * 功能说明：
     * - 管理端演出列表页面
     * - 支持多条件筛选（名称、地区、分类、状态）
     * - 支持分页查询
     * - 可以查看所有状态的演出（包括已取消的）
     * 
     * @param name 演出名称（可选），用于模糊查询
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选），1=正常，0=已取消
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页结果，包含演出列表和分页信息
     */
    PageResult<Show> getShowListForAdmin(String name, String region, String category, Integer status, int pageNum, int pageSize);

    /**
     * 管理端 - 查询演出详情（包含明确库存数字）
     * 
     * 功能说明：
     * - 管理端查询演出详情
     * - 可以查看所有状态的演出（包括已取消的）
     * - 显示明确的库存数字
     * 
     * @param id 演出ID
     * @return 演出对象，如果不存在则返回null
     */
    Show getShowByIdForAdmin(Long id);
}
