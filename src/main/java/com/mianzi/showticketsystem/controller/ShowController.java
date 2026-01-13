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

@RestController
@RequestMapping("/api/show") //演出接口的根目录
public class ShowController {

    @Autowired
    private ShowService showService;

    /**
     * 发布新的演出活动 (只有管理员可以操作)
     * 请求路径: POST /api/show/publish
     * 接收参数:
     * @param name 演出名称
     * @param venue 场馆
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param totalTickets 总票数
     * @param price 票价
     * @return 发布结果信息
     */
    @PostMapping("/publish")
    public String publishShow(
            @RequestParam String name,
            @RequestParam String venue,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam Integer totalTickets,
            @RequestParam java.math.BigDecimal price,
            @RequestParam(required = false) String sessionInfo,
            @RequestParam(required = false) String ticketTier,
            @RequestParam(required = false) Integer isOnSale)
    {
        if (name == null || name.isEmpty() || totalTickets == null || totalTickets <= 0) {
            return "发布失败：名称或总票数不能为空。";
        }

        //创建 Show 实体对象
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

        //调用 Service 层
        boolean success = showService.publishShow(show);

        if (success) {
            return "演出发布成功！ID: " + show.getId();
        } else {
            return "演出发布失败。";
        }
    }

    /**
     * 查询所有已发布的演出列表
     * 请求路径: GET /api/show/list
     * @return 演出列表 (JSON 格式)
     */
    @GetMapping("/list")
    public List<Show> findAll() {
        // 直接调用 Service 层的方法
        return showService.findAllShows();
    }

    /**
     * 在首页获取地区和分类演出列表
     * 请求路径: GET /api/show/home
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
        // 如果已登录，可以根据用户IP返回对应地区（这里简化处理，使用传入的region参数）
        // 如果未指定region，默认返回北京地区
        return showService.getHomeShows(region, category, limit);
    }

    /**
     * 搜索演出（匹配演出名或场馆名）
     * 请求路径: GET /api/show/search
     * @param keyword 关键词
     * @return 演出列表
     */
    @GetMapping("/search")
    public List<Show> searchShows(@RequestParam String keyword) {
        return showService.searchShows(keyword);
    }

    /**
     * 条件查询演出（分页）
     * 请求路径: GET /api/show/query
     * @param region 城市
     * @param category 分类
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/query")
    public PageResult<Show> findShowsByConditions(@RequestParam(required = false) String region,
                                                   @RequestParam(required = false) String category,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return showService.findShowsByConditions(region, category, pageNum, pageSize);
    }

    /**
     * 查询单个演出详情 (用户端)
     * 请求路径: GET /api/show/details
     * @param showId 演出ID
     * @return 演出详情 (Show 实体 JSON)，包含库存信息（是否有库存）、是否已开票等
     *         如果演出不存在，返回 {"show": null}（确保始终返回有效的JSON对象）
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getShowDetails(@RequestParam Long showId) {
        Show show = showService.getShowById(showId);
        // 使用 Map 包装，确保即使 show 为 null 也返回有效的 JSON 对象
        Map<String, Object> result = new HashMap<>();
        result.put("show", show);
        return ResponseEntity.ok(result);
    }

    /**
     * 管理端 - 更新演出信息
     * 请求路径: PUT /api/show/update
     * @param show 包含更新信息的演出对象 (通过 RequestBody 接收 JSON 数据)
     * @return 结果信息
     */
    @PutMapping("/update")
    //@RequestBody表明要用Body-JSON格式传递整个show实体类来测试
    public String updateShow(@RequestBody Show show) {
        //基本校验
        if (show.getId() == null) {
            return "更新失败：演出ID不能为空。";
        }

        //调用 Service 层执行更新
        boolean success = showService.updateShow(show);

        //返回结果
        if (success) {
            return "演出信息更新成功！ID: " + show.getId();
        } else {
            return "更新失败！可能原因：演出ID不存在或数据校验失败。";
        }
    }

    /**
     * 管理端 - 删除演出信息
     * 请求路径: DELETE /api/show/delete?id=...
     * @param id 演出ID (通过 RequestParam 接收)
     * @return 结果信息
     */
    @DeleteMapping("/delete")
    public String deleteShow(@RequestParam Long id) {
        //基本校验
        if (id == null) {
            return "删除失败：演出ID不能为空。";
        }

        //调用 Service 层执行删除
        boolean success = showService.deleteShow(id);

        //返回结果
        if (success) {
            return "演出信息删除成功！ID: " + id;
        } else {
            return "删除失败！可能原因：演出ID不存在或数据库操作失败。";
        }
    }
}
