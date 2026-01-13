package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 演出/活动实体类
 */
@Data // Lombok 自动生成 getter, setter, toString, equals, hashCode
@Accessors(chain = true) // 启用链式设置方法，如 new Show().setId(1).setName("...")...
public class Show {

    private Long id; // 主键ID
    private String name; // 演出名称
    private String venue; // 演出地点/场馆
    private String region; // 地区（如：北京、上海、广州、深圳等）
    private String category; // 分类（如：演唱会、话剧、音乐会、体育赛事等）
    private LocalDateTime startTime; // 演出开始时间
    private LocalDateTime endTime; // 演出结束时间
    private Integer totalTickets; // 总票数
    private Integer availableTickets; // 可用票数 (库存)
    private java.math.BigDecimal price; // 票价
    private String sessionInfo; // 场次信息（JSON格式，存储多场次信息）
    private String ticketTier; // 票档信息（JSON格式，存储不同价格档位）
    private Integer isOnSale; // 是否已开票（1:已开票, 0:未开票）
    private Integer status; // 演出状态 (1: 正常/可售, 0: 已取消/结束)
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
