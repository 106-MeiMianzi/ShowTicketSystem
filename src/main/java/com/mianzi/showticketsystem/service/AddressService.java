package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.Address;

import java.util.List;

/**
 * 收货地址业务逻辑接口
 */
public interface AddressService {

    /**
     * 添加收货地址
     * @param address 地址实体对象
     * @return 成功返回 true，失败返回 false
     */
    boolean addAddress(Address address);

    /**
     * 获取用户的所有收货地址
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getUserAddresses(Long userId);

    /**
     * 根据ID获取收货地址详情
     * @param id 地址ID
     * @param userId 用户ID
     * @return 地址对象
     */
    Address getAddressById(Long id, Long userId);

    /**
     * 更新收货地址
     * @param address 地址实体对象
     * @return 成功返回 true，失败返回 false
     */
    boolean updateAddress(Address address);

    /**
     * 删除收货地址
     * @param id 地址ID
     * @param userId 用户ID
     * @return 成功返回 true，失败返回 false
     */
    boolean deleteAddress(Long id, Long userId);
}

