package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 收货地址实体类
 * 
 * 对应数据库表：address
 * 
 * 作用：
 * - 映射数据库中的收货地址表结构
 * - 用于在 Java 代码中表示用户的收货地址信息
 * - 用户可以为票务订单设置收货地址（虽然票务通常是电子票，但某些场景可能需要实体票）
 */
@Data
/**
 * @Data 注解：自动生成 getter、setter、toString、equals、hashCode 等方法
 */
@Accessors(chain = true)
/**
 * @Accessors(chain = true) 注解：启用链式调用
 */
public class Address {

    /**
     * 地址ID（主键，自增）
     * 
     * 数据库字段：id BIGINT AUTO_INCREMENT PRIMARY KEY
     * 
     * 作用：唯一标识一个收货地址
     */
    private Long id;

    /**
     * 用户ID（外键）
     * 
     * 数据库字段：user_id BIGINT NOT NULL
     * 
     * 外键关系：
     * - 关联 user 表的 id 字段
     * - FOREIGN KEY (user_id) REFERENCES user(id)
     * 
     * 作用：标识这个地址属于哪个用户
     */
    private Long userId;

    /**
     * 收货人姓名
     * 
     * 数据库字段：receiver_name VARCHAR(50) NOT NULL
     * 
     * 说明：收货人的真实姓名，必填字段
     */
    private String receiverName;

    /**
     * 收货人电话
     * 
     * 数据库字段：receiver_phone VARCHAR(20) NOT NULL
     * 
     * 说明：
     * - 收货人的联系电话，必填字段
     * - 用于快递联系收货人
     * - 通常需要验证格式（11位手机号）
     */
    private String receiverPhone;

    /**
     * 省/直辖市
     * 
     * 数据库字段：province VARCHAR(50)
     * 
     * 示例：北京市、上海市、广东省等
     */
    private String province;

    /**
     * 市
     * 
     * 数据库字段：city VARCHAR(50)
     * 
     * 示例：北京市、上海市、广州市等
     */
    private String city;

    /**
     * 区/县
     * 
     * 数据库字段：district VARCHAR(50)
     * 
     * 示例：朝阳区、浦东新区、天河区等
     */
    private String district;

    /**
     * 详细地址
     * 
     * 数据库字段：detail_address VARCHAR(200)
     * 
     * 说明：
     * - 街道、门牌号等详细地址信息
     * - 与省市区组合成完整地址
     * 
     * 示例：中关村大街1号海龙大厦10层1001室
     */
    private String detailAddress;

    /**
     * 是否为默认地址
     * 
     * 数据库字段：is_default INT DEFAULT 0
     * 
     * 状态说明：
     * - 1：是默认地址
     * - 0：非默认地址
     * 
     * 业务逻辑：
     * - 用户可以有多个地址，但只有一个默认地址
     * - 下单时默认使用默认地址
     * - 设置新地址为默认时，需要将其他地址的 isDefault 设为 0
     */
    private Integer isDefault;

    /**
     * 创建时间
     * 
     * 数据库字段：create_time DATETIME DEFAULT CURRENT_TIMESTAMP
     * 
     * 说明：记录地址创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 
     * 数据库字段：update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     * 
     * 说明：记录地址信息最后一次修改的时间
     */
    private LocalDateTime updateTime;
}

