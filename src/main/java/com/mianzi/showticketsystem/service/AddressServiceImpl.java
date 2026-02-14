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
 * 
 * 作用：
 * - 实现AddressService接口定义的所有业务逻辑方法
 * - 调用Mapper层进行数据库操作
 * - 处理业务逻辑和异常情况
 * 
 * 职责：
 * - 地址的增删改查逻辑
 * - 默认地址管理逻辑
 * - 权限验证逻辑
 */
@Service
/**
 * @Service 注解：
 * - 标识这是一个Service层的Bean
 * - Spring会自动扫描并创建这个类的实例
 */
public class AddressServiceImpl implements AddressService {

    /**
     * 注入地址Mapper
     * 
     * 用于执行数据库操作
     */
    @Autowired
    private AddressMapper addressMapper;

    /**
     * 实现添加收货地址的逻辑
     * 
     * 业务逻辑：
     * 1. 设置创建时间和更新时间
     * 2. 如果设置为默认地址，先取消其他地址的默认状态
     * 3. 插入新地址到数据库
     * 
     * @param address 地址实体对象
     * @return 成功返回true，失败返回false
     */
    @Override
    @Transactional
    /**
     * @Transactional 注解说明：
     * 
     * 声明式事务管理
     * 
     * 作用：
     * - 确保方法内的所有数据库操作在同一个事务中执行
     * - 如果任何操作失败，所有操作都会回滚
     * - 保证数据一致性
     * 
     * 使用场景：
     * - 需要多个数据库操作作为一个整体执行
     * - 例如：取消默认地址 + 添加新地址，必须同时成功或失败
     */
    public boolean addAddress(Address address) {
        /**
         * 步骤1：设置创建时间和更新时间
         */
        LocalDateTime now = LocalDateTime.now();
        address.setCreateTime(now);
        address.setUpdateTime(now);

        /**
         * 步骤2：如果设置为默认地址，需要先取消其他默认地址
         * 
         * 业务规则：用户只能有一个默认地址
         */
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            /**
             * 取消该用户的所有其他地址的默认状态
             */
            addressMapper.cancelDefaultByUserId(address.getUserId());
        }

        /**
         * 步骤3：插入新地址到数据库
         */
        int result = addressMapper.insert(address);
        
        /**
         * result = 1 表示插入成功（影响1行）
         * result = 0 表示插入失败
         */
        return result == 1;
    }

    /**
     * 实现获取用户的所有收货地址的逻辑
     * 
     * @param userId 用户ID
     * @return 地址列表，如果没有则返回空列表
     */
    @Override
    public List<Address> getUserAddresses(Long userId) {
        /**
         * 直接调用Mapper层查询用户的所有地址
         */
        return addressMapper.findByUserId(userId);
    }

    /**
     * 实现根据ID获取收货地址详情的逻辑
     * 
     * 业务逻辑：
     * 1. 根据ID和用户ID查询地址
     * 2. 双重验证：确保地址属于当前用户
     * 
     * @param id 地址ID
     * @param userId 用户ID（用于权限校验）
     * @return 地址对象，如果不存在或不属于该用户则返回null
     */
    @Override
    public Address getAddressById(Long id, Long userId) {
        /**
         * 步骤1：根据ID和用户ID查询地址
         * 
         * Mapper层会验证地址属于该用户
         */
        Address address = addressMapper.getByIdAndUserId(id, userId);
        
        /**
         * 步骤2：双重验证：确保返回的地址确实属于当前用户
         * 
         * 这是额外的安全措施，防止SQL注入或其他安全问题
         */
        if (address != null && !address.getUserId().equals(userId)) {
            /**
             * 地址不属于当前用户，返回null
             */
            return null;
        }
        
        /**
         * 地址属于当前用户，返回地址对象
         */
        return address;
    }

    /**
     * 实现更新收货地址的逻辑
     * 
     * 业务逻辑：
     * 1. 验证地址是否存在且属于当前用户
     * 2. 设置更新时间
     * 3. 如果设置为默认地址，先取消其他地址的默认状态
     * 4. 更新地址信息
     * 
     * @param address 地址实体对象，必须包含id和userId
     * @return 成功返回true，失败返回false
     */
    @Override
    @Transactional
    /**
     * @Transactional 注解：
     * - 确保取消默认地址和更新地址在同一个事务中执行
     */
    public boolean updateAddress(Address address) {
        /**
         * 步骤1：先验证地址是否存在且属于当前用户
         */
        if (address.getId() != null && address.getUserId() != null) {
            Address existingAddress = addressMapper.getByIdAndUserId(address.getId(), address.getUserId());
            if (existingAddress == null) {
                /**
                 * 地址不存在或不属于该用户，更新失败
                 */
                return false;
            }
            /**
             * 双重验证：确保地址确实属于当前用户
             */
            if (!existingAddress.getUserId().equals(address.getUserId())) {
                /**
                 * 地址不属于当前用户，更新失败
                 */
                return false;
            }
        }
        
        /**
         * 步骤2：设置更新时间
         */
        address.setUpdateTime(LocalDateTime.now());

        /**
         * 步骤3：如果设置为默认地址，需要先取消其他默认地址
         */
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            /**
             * 取消该用户的所有其他地址的默认状态
             */
            addressMapper.cancelDefaultByUserId(address.getUserId());
        }

        /**
         * 步骤4：更新地址信息
         */
        int result = addressMapper.update(address);
        
        /**
         * result = 1 表示更新成功（影响1行）
         * result = 0 表示更新失败
         */
        return result == 1;
    }

    /**
     * 实现删除收货地址的逻辑
     * 
     * 业务逻辑：
     * 1. 验证地址是否存在且属于当前用户
     * 2. 删除地址
     * 
     * @param id 地址ID
     * @param userId 用户ID（用于权限校验）
     * @return 成功返回true，失败返回false
     */
    @Override
    public boolean deleteAddress(Long id, Long userId) {
        /**
         * 步骤1：先验证地址是否存在且属于当前用户
         */
        Address address = addressMapper.getByIdAndUserId(id, userId);
        if (address == null) {
            /**
             * 地址不存在或不属于该用户，删除失败
             */
            return false;
        }
        
        /**
         * 步骤2：双重验证：确保地址确实属于当前用户
         */
        if (!address.getUserId().equals(userId)) {
            /**
             * 地址不属于当前用户，删除失败
             */
            return false;
        }
        
        /**
         * 步骤3：删除地址
         */
        int result = addressMapper.delete(id, userId);
        
        /**
         * result = 1 表示删除成功（影响1行）
         * result = 0 表示删除失败
         */
        return result == 1;
    }
}
