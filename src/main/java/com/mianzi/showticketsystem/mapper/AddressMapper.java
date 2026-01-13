package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址数据访问接口
 */
@Mapper
public interface AddressMapper {

    /**
     * 插入新地址
     * @param address 地址实体对象
     * @return 影响的行数 (1 表示成功)
     */
    int insert(Address address);

    /**
     * 根据ID和用户ID查询地址
     * @param id 地址ID
     * @param userId 用户ID
     * @return 地址对象
     */
    Address getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询用户的所有地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> findByUserId(@Param("userId") Long userId);

    /**
     * 更新地址信息
     * @param address 地址实体对象
     * @return 影响的行数 (1 表示成功)
     */
    int update(Address address);

    /**
     * 删除地址
     * @param id 地址ID
     * @param userId 用户ID（用于权限校验）
     * @return 影响的行数 (1 表示成功)
     */
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 取消用户的默认地址（将所有地址设为非默认）
     * @param userId 用户ID
     * @return 影响的行数
     */
    int cancelDefaultByUserId(@Param("userId") Long userId);
}

