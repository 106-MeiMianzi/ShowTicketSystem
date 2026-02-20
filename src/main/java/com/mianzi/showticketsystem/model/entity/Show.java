package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 演出/活动实体类
 * 
 * 对应数据库表：show
 * 
 * 作用：
 * - 映射数据库中的演出表结构
 * - 用于在 Java 代码中表示演出/活动数据
 * - 包含演出的基本信息、时间、票价、库存等
 */
@Data
/**
 * @Data 注解：
 * 自动生成 getter、setter、toString、equals、hashCode 等方法
 */
@Accessors(chain = true)
/**
 * @Accessors(chain = true) 注解：
 * 启用链式调用，如：new Show().setId(1L).setName("演唱会").setPrice(new BigDecimal("299"))
 */
public class Show {

    /**
     * 演出ID（主键，自增）
     * 
     * 数据库字段：id BIGINT AUTO_INCREMENT PRIMARY KEY
     * 
     * 作用：唯一标识一个演出
     */
    private Long id;

    /**
     * 演出名称
     * 
     * 数据库字段：name VARCHAR(200) NOT NULL
     * 
     * 示例：周杰伦2024世界巡回演唱会、话剧《雷雨》等
     */
    private String name;

    /**
     * 演出地点/场馆
     * 
     * 数据库字段：venue VARCHAR(200)
     * 
     * 示例：北京工人体育场、上海梅赛德斯奔驰文化中心等
     */
    private String venue;

    /**
     * 地区
     * 
     * 数据库字段：region VARCHAR(50)
     * 
     * 示例：北京、上海、广州、深圳等
     * 
     * 用途：用于按地区筛选演出
     */
    private String region;

    /**
     * 分类
     * 
     * 数据库字段：category VARCHAR(50)
     * 
     * 示例：演唱会、话剧、音乐会、体育赛事、舞蹈等
     * 
     * 用途：用于按分类筛选演出
     */
    private String category;

    /**
     * 演出开始时间
     * 
     * 数据库字段：start_time DATETIME
     * 
     * 说明：演出的开始日期和时间
     */
    private LocalDateTime startTime;

    /**
     * 演出结束时间
     * 
     * 数据库字段：end_time DATETIME
     * 
     * 说明：演出的结束日期和时间
     */
    private LocalDateTime endTime;

    /**
     * 总票数
     * 
     * 数据库字段：total_tickets INT DEFAULT 0
     * 
     * 说明：
     * - 该演出的总票数（固定值）
     * - 用于计算已售出票数：已售 = totalTickets - availableTickets
     */
    private Integer totalTickets;

    /**
     * 可用票数（库存）
     * 
     * 数据库字段：available_tickets INT DEFAULT 0
     * 
     * 说明：
     * - 当前可购买的票数
     * - 每次下单后需要减少这个值
     * - 当 availableTickets = 0 时，表示已售罄
     * 
     * 业务逻辑：
     * - 下单时检查 availableTickets >= 购买数量
     * - 支付成功后减少 availableTickets
     */
    private Integer availableTickets;

    /**
     * 票价
     * 
     * 数据库字段：price DECIMAL(10,2)
     * 
     * 类型说明：
     * - 使用 BigDecimal 而不是 double/float
     * - 原因：金额计算需要精确，避免浮点数精度问题
     * - DECIMAL(10,2) 表示最多10位数字，小数点后2位
     * 
     * 示例：299.00、599.50 等
     */
    private java.math.BigDecimal price;

    /**
     * 场次信息（JSON格式）
     * 
     * 数据库字段：session_info TEXT
     * 
     * 说明：
     * - 存储多场次信息，使用 JSON 格式字符串
     * - 例如：{"sessions": [{"date": "2024-01-01", "time": "19:30"}, ...]}
     * - 如果只有单场次，可以为空或存储简单信息
     */
    private String sessionInfo;

    /**
     * 票档信息（JSON格式）
     * 
     * 数据库字段：ticket_tier TEXT
     * 
     * 说明：
     * - 存储不同价格档位信息，使用 JSON 格式字符串
     * - 例如：{"tiers": [{"name": "VIP", "price": 999}, {"name": "普通票", "price": 299}]}
     * - 如果只有单一票价，price 字段已足够，此字段可以为空
     */
    private String ticketTier;

    /**
     * 封面图URL
     *
     * 数据库字段：cover_url VARCHAR(512)
     *
     * 说明：演出列表/详情展示用的封面图链接，由管理员在发布或编辑时填写
     */
    private String coverUrl;

    /**
     * 是否已开票
     * 
     * 数据库字段：is_on_sale INT DEFAULT 0
     * 
     * 状态说明：
     * - 1：已开票，用户可以购买
     * - 0：未开票，用户还不能购买
     * 
     * 用途：控制演出的销售状态
     */
    private Integer isOnSale;

    /**
     * 演出状态
     * 
     * 数据库字段：status INT DEFAULT 1
     * 
     * 状态说明：
     * - 1：正常/可售，演出正常进行
     * - 0：已取消/结束，演出已取消或已结束
     * 
     * 用途：
     * - 管理员可以取消演出
     * - 用户端不显示已取消的演出
     */
    private Integer status;

    /**
     * 创建时间
     * 
     * 数据库字段：create_time DATETIME DEFAULT CURRENT_TIMESTAMP
     * 
     * 说明：记录演出信息创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 
     * 数据库字段：update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     * 
     * 说明：记录演出信息最后一次修改的时间
     */
    private LocalDateTime updateTime;
}
