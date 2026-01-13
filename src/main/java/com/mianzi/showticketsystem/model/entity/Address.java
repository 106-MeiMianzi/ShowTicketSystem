package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 收货地址实体类 (对应数据库表 address)
 */
@Data
@Accessors(chain = true)
public class Address {

    /**
     * 地址ID（主键，自增）
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 省/直辖市
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 是否为默认地址（1:是, 0:否）
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

