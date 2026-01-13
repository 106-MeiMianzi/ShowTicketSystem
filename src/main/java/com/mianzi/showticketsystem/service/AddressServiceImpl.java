package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.AddressMapper;
import com.mianzi.showticketsystem.model.entity.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AddressService 接口的实现类
 */
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    /**
     * 实现添加收货地址的逻辑
     */
    @Override
    @Transactional
    public boolean addAddress(Address address) {
        LocalDateTime now = LocalDateTime.now();
        address.setCreateTime(now);
        address.setUpdateTime(now);

        // 如果设置为默认地址，需要先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(address.getUserId());
        }

        int result = addressMapper.insert(address);
        return result == 1;
    }

    /**
     * 实现获取用户的所有收货地址的逻辑
     */
    @Override
    public List<Address> getUserAddresses(Long userId) {
        return addressMapper.findByUserId(userId);
    }

    /**
     * 实现根据ID获取收货地址详情的逻辑
     */
    @Override
    public Address getAddressById(Long id, Long userId) {
        Address address = addressMapper.getByIdAndUserId(id, userId);
        // 双重验证：确保返回的地址确实属于当前用户（防止SQL注入或其他安全问题）
        if (address != null && !address.getUserId().equals(userId)) {
            return null; // 地址不属于当前用户
        }
        return address;
    }

    /**
     * 实现更新收货地址的逻辑
     */
    @Override
    @Transactional
    public boolean updateAddress(Address address) {
        // 先验证地址是否存在且属于当前用户
        if (address.getId() != null && address.getUserId() != null) {
            Address existingAddress = addressMapper.getByIdAndUserId(address.getId(), address.getUserId());
            if (existingAddress == null) {
                return false; // 地址不存在或不属于该用户
            }
            // 双重验证：确保地址确实属于当前用户
            if (!existingAddress.getUserId().equals(address.getUserId())) {
                return false; // 地址不属于当前用户
            }
        }
        
        address.setUpdateTime(LocalDateTime.now());

        // 如果设置为默认地址，需要先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(address.getUserId());
        }

        int result = addressMapper.update(address);
        return result == 1;
    }

    /**
     * 实现删除收货地址的逻辑
     */
    @Override
    public boolean deleteAddress(Long id, Long userId) {
        // 先验证地址是否存在且属于当前用户
        Address address = addressMapper.getByIdAndUserId(id, userId);
        if (address == null) {
            return false; // 地址不存在或不属于该用户
        }
        // 双重验证：确保地址确实属于当前用户
        if (!address.getUserId().equals(userId)) {
            return false; // 地址不属于当前用户
        }
        
        int result = addressMapper.delete(id, userId);
        return result == 1;
    }
}

