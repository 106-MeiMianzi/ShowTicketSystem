package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

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
            //使用 @DateTimeFormat 来解析请求中的日期时间字符串
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam Integer totalTickets,
            //使用 BigDecimal 类型来接收价格
            @RequestParam java.math.BigDecimal price)
    {
        if (name == null || name.isEmpty() || totalTickets == null || totalTickets <= 0) {
            return "发布失败：名称或总票数不能为空。";
        }

        //创建 Show 实体对象
        Show show = new Show()
                .setName(name)
                .setVenue(venue)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setTotalTickets(totalTickets)
                .setPrice(price);

        //调用 Service 层
        boolean success = showService.publishShow(show);

        if (success) {
            return "演出发布成功！ID: " + show.getId(); // show.getId() 在 Service 层会被回写
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
     * 3. 查询单个演出详情 (用户和管理员通用)
     * 请求路径: GET /api/show/details
     * @param showId 演出ID
     * @return 演出详情 (Show 实体 JSON)
     */
    @GetMapping("/details")
    public Show getShowDetails(@RequestParam Long showId) {
        return showService.getShowById(showId);
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
