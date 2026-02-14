package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.model.entity.Address;

import java.util.List;

/**
 * 收货地址业务逻辑接口
 * 
 * 作用：
 * - 定义收货地址相关的业务逻辑方法
 * - 提供地址的增删改查功能
 * - 由AddressServiceImpl实现具体的业务逻辑
 * 
 * 安全说明：
 * - 所有操作都需要验证用户ID，确保用户只能操作自己的地址
 */
public interface AddressService {

    /**
     * 添加收货地址
     * 
     * 功能说明：
     * - 用户添加新的收货地址
     * - 如果设置为默认地址，会自动取消其他地址的默认状态
     * 
     * @param address 地址实体对象，必须包含userId、receiverName、receiverPhone等字段
     * @return 成功返回true，失败返回false
     */
    boolean addAddress(Address address);

    /**
     * 获取用户的所有收货地址
     * 
     * 功能说明：
     * - 查询指定用户的所有收货地址
     * - 用于地址选择页面
     * 
     * @param userId 用户ID
     * @return 地址列表，如果没有则返回空列表
     */
    List<Address> getUserAddresses(Long userId);

    /**
     * 根据ID获取收货地址详情
     * 
     * 功能说明：
     * - 查询指定地址的详细信息
     * - 会验证地址属于指定用户（防止越权访问）
     * 
     * @param id 地址ID
     * @param userId 用户ID（用于权限校验）
     * @return 地址对象，如果不存在或不属于该用户则返回null
     */
    Address getAddressById(Long id, Long userId);

    /**
     * 更新收货地址
     * 
     * 功能说明：
     * - 用户修改收货地址信息
     * - 如果设置为默认地址，会自动取消其他地址的默认状态
     * - 会验证地址属于指定用户
     * 
     * @param address 地址实体对象，必须包含id和userId
     * @return 成功返回true，失败返回false
     */
    boolean updateAddress(Address address);

    /**
     * 删除收货地址
     * 
     * 功能说明：
     * - 用户删除不需要的收货地址
     * - 会验证地址属于指定用户（防止越权删除）
     * 
     * @param id 地址ID
     * @param userId 用户ID（用于权限校验）
     * @return 成功返回true，失败返回false
     */
    boolean deleteAddress(Long id, Long userId);
}
