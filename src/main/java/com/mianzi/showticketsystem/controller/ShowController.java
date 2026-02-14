package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演出接口控制器
 * 
 * 作用：
 * - 提供演出相关的API接口
 * - 包括演出查询、搜索、详情等功能
 * - 部分接口需要管理员权限（如发布、更新、删除）
 * 
 * 说明：
 * - 用户端接口：查询、搜索、详情等（公开接口，不需要登录）
 * - 管理端接口：发布、更新、删除等（需要管理员权限）
 */
@RestController
/**
 * @RestController 注解：
 * - 标识这是一个REST控制器
 * - 方法返回值自动转换为JSON
 */
@RequestMapping("/api/show")
/**
 * @RequestMapping 注解：
 * - 定义控制器的基础路径为/api/show
 * - 所有方法的URL都会以/api/show开头
 */
public class ShowController {

    /**
     * 注入演出服务
     * 
     * 用于处理演出相关的业务逻辑
     */
    @Autowired
    private ShowService showService;

    /**
     * 发布新的演出活动（只有管理员可以操作）
     * 
     * 请求路径: POST /api/show/publish
     * 
     * 功能：
     * - 管理员发布新演出
     * - 需要管理员权限（由JwtAuthenticationFilter验证）
     * 
     * 接收参数：
     * - name: 演出名称（必填）
     * - venue: 场馆（必填）
     * - region: 地区（可选）
     * - category: 分类（可选）
     * - startTime: 开始时间（必填，ISO 8601格式）
     * - endTime: 结束时间（必填，ISO 8601格式）
     * - totalTickets: 总票数（必填）
     * - price: 票价（必填）
     * - sessionInfo: 场次信息（可选，JSON格式）
     * - ticketTier: 票档信息（可选，JSON格式）
     * - isOnSale: 是否已开票（可选，1=已开票，0=未开票）
     * 
     * @return 发布结果信息（字符串）
     */
    @PostMapping("/publish")
    public String publishShow(
            @RequestParam String name,
            @RequestParam String venue,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            /**
             * @DateTimeFormat 注解说明：
             * - 指定日期时间格式为ISO 8601
             * - 例如：2024-12-31T20:00:00
             */
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam Integer totalTickets,
            @RequestParam java.math.BigDecimal price,
            @RequestParam(required = false) String sessionInfo,
            @RequestParam(required = false) String ticketTier,
            @RequestParam(required = false) Integer isOnSale)
    {
        /**
         * 步骤1：基础参数验证
         */
        if (name == null || name.isEmpty() || totalTickets == null || totalTickets <= 0) {
            return "发布失败：名称或总票数不能为空。";
        }

        /**
         * 步骤2：创建Show实体对象
         * 
         * 使用链式调用设置属性
         */
        Show show = new Show()
                .setName(name)
                .setVenue(venue)
                .setRegion(region)
                .setCategory(category)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setTotalTickets(totalTickets)
                .setPrice(price)
                .setSessionInfo(sessionInfo)
                .setTicketTier(ticketTier)
                .setIsOnSale(isOnSale);

        /**
         * 步骤3：调用Service层发布演出
         */
        boolean success = showService.publishShow(show);

        if (success) {
            return "演出发布成功！ID: " + show.getId();
        } else {
            return "演出发布失败。";
        }
    }

    /**
     * 查询所有已发布的演出列表
     * 
     * 请求路径: GET /api/show/list
     * 
     * 说明：
     * - 公开接口，不需要登录
     * - 返回所有正常状态的演出
     * 
     * @return 演出列表（JSON格式）
     */
    @GetMapping("/list")
    public List<Show> findAll() {
        /**
         * 直接调用Service层的方法
         */
        return showService.findAllShows();
    }

    /**
     * 在首页获取地区和分类演出列表
     * 
     * 请求路径: GET /api/show/home
     * 
     * 功能：
     * - 首页推荐演出
     * - 支持按地区和分类筛选
     * - 支持限制返回数量
     * 
     * @param region 地区（可选，默认北京）
     * @param category 分类（可选）
     * @param limit 限制数量（可选，默认20）
     * @param request HTTP请求（用于获取用户IP，这里简化处理）
     * @return 演出列表
     */
    @GetMapping("/home")
    public List<Show> getHomeShows(@RequestParam(required = false) String region,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) Integer limit,
                                    HttpServletRequest request) {
        /**
         * 如果已登录，可以根据用户IP返回对应地区（这里简化处理，使用传入的region参数）
         * 如果未指定region，默认返回北京地区
         */
        return showService.getHomeShows(region, category, limit);
    }

    /**
     * 搜索演出（匹配演出名或场馆名）
     * 
     * 请求路径: GET /api/show/search
     * 
     * 功能：
     * - 支持模糊搜索演出名称或场馆名称
     * - 公开接口，不需要登录
     * 
     * @param keyword 关键词
     * @return 演出列表
     */
    @GetMapping("/search")
    public List<Show> searchShows(@RequestParam String keyword) {
        /**
         * 调用Service层搜索方法
         */
        return showService.searchShows(keyword);
    }

    /**
     * 条件查询演出（分页）
     * 
     * 请求路径: GET /api/show/query
     * 
     * 功能：
     * - 支持按地区和分类筛选
     * - 支持分页查询
     * - 公开接口，不需要登录
     * 
     * @param region 城市（可选）
     * @param category 分类（可选）
     * @param pageNum 当前页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 分页结果
     */
    @GetMapping("/query")
    public PageResult<Show> findShowsByConditions(@RequestParam(required = false) String region,
                                                   @RequestParam(required = false) String category,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        /**
         * 调用Service层条件查询方法
         */
        return showService.findShowsByConditions(region, category, pageNum, pageSize);
    }

    /**
     * 查询单个演出详情（用户端）
     * 
     * 请求路径: GET /api/show/details
     * 
     * 功能：
     * - 用户端查询演出详情
     * - 包含库存信息（是否有库存）、是否已开票等
     * - 公开接口，不需要登录
     * 
     * @param showId 演出ID
     * @return ResponseEntity对象，包含演出详情
     *         如果演出存在，返回 {"show": {演出详情JSON对象}}
     *         如果不存在，返回 {"show": null}
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getShowDetails(@RequestParam Long showId) {
        /**
         * 调用Service层查询演出详情（用户端版本）
         */
        Show show = showService.getShowById(showId);
        
        /**
         * 使用Map包装，确保即使show为null也返回有效的JSON对象
         */
        Map<String, Object> result = new HashMap<>();
        result.put("show", show);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 更新演出信息
     * 
     * 请求路径: PUT /api/show/update
     * 
     * 功能：
     * - 管理员修改演出信息
     * - 需要管理员权限
     * 
     * @param show 包含更新信息的演出对象（通过RequestBody接收JSON数据）
     * @return 结果信息（字符串）
     */
    @PutMapping("/update")
    /**
     * @RequestBody 注解说明：
     * - 从HTTP请求体中获取JSON数据
     * - 自动将JSON转换为Show对象
     * - 适用于POST、PUT等请求
     */
    public String updateShow(@RequestBody Show show) {
        /**
         * 步骤1：基本校验
         * 
         * 验证演出ID不能为空
         */
        if (show.getId() == null) {
            return "更新失败：演出ID不能为空。";
        }

        /**
         * 步骤2：调用Service层执行更新
         */
        boolean success = showService.updateShow(show);

        /**
         * 步骤3：返回结果
         */
        if (success) {
            return "演出信息更新成功！ID: " + show.getId();
        } else {
            return "更新失败！可能原因：演出ID不存在或数据校验失败。";
        }
    }

    /**
     * 管理端 - 删除演出信息
     * 
     * 请求路径: DELETE /api/show/delete?id=...
     * 
     * 功能：
     * - 管理员删除演出
     * - 需要管理员权限
     * 
     * @param id 演出ID（通过RequestParam接收）
     * @return 结果信息（字符串）
     */
    @DeleteMapping("/delete")
    public String deleteShow(@RequestParam Long id) {
        /**
         * 步骤1：基本校验
         * 
         * 验证演出ID不能为空
         */
        if (id == null) {
            return "删除失败：演出ID不能为空。";
        }

        /**
         * 步骤2：调用Service层执行删除
         */
        boolean success = showService.deleteShow(id);

        /**
         * 步骤3：返回结果
         */
        if (success) {
            return "演出信息删除成功！ID: " + id;
        } else {
            return "删除失败！可能原因：演出ID不存在或数据库操作失败。";
        }
    }
}
