package com.mianzi.showticketsystem.mapper;

import com.mianzi.showticketsystem.model.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址数据访问接口（Mapper）
 * 
 * 作用：
 * - 定义收货地址相关的数据库操作方法
 * - 提供地址的增删改查功能
 * - 所有操作都需要验证用户ID，确保用户只能操作自己的地址
 */
@Mapper
/**
 * @Mapper 注解：
 * 标识这是一个 MyBatis Mapper 接口，Spring Boot 会自动创建代理实现类
 */
public interface AddressMapper {

    /**
     * 插入新地址
     * 
     * 用途：用户添加新的收货地址
     * 
     * @param address 地址实体对象，必须包含 userId、receiverName、receiverPhone 等字段
     * @return 影响的行数（1 表示成功，0 表示失败）
     * 
     * SQL 说明：
     * - SQL 定义在 resources/mapper/AddressMapper.xml 中
     * - 插入时会自动填充 create_time 和 update_time
     */
    int insert(Address address);

    /**
     * 根据ID和用户ID查询地址
     * 
     * 用途：查询指定地址详情，并验证地址属于指定用户
     * 
     * @param id 地址ID
     * @param userId 用户ID（用于权限校验）
     * @return 地址对象，如果不存在或不属于该用户则返回 null
     * 
     * 安全说明：
     * - 必须同时匹配 id 和 userId，防止用户查询其他用户的地址
     * - 这是防止越权访问的重要措施
     */
    Address getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询用户的所有地址列表
     * 
     * 用途：获取用户的所有收货地址，用于地址选择页面
     * 
     * @param userId 用户ID
     * @return 该用户的所有地址列表，如果没有则返回空列表
     */
    List<Address> findByUserId(@Param("userId") Long userId);

    /**
     * 更新地址信息
     * 
     * 用途：用户修改收货地址信息
     * 
     * @param address 地址实体对象，必须包含 id 和 userId
     * @return 影响的行数（1 表示成功，0 表示地址不存在或不属于该用户）
     * 
     * 说明：
     * - SQL 定义在 XML 文件中，通常使用动态 SQL
     * - 只更新非空字段
     * - 更新时会自动更新 update_time
     */
    int update(Address address);

    /**
     * 删除地址
     * 
     * 用途：用户删除不需要的收货地址
     * 
     * @param id 地址ID
     * @param userId 用户ID（用于权限校验）
     * @return 影响的行数（1 表示成功，0 表示地址不存在或不属于该用户）
     * 
     * 安全说明：
     * - 必须同时匹配 id 和 userId，防止用户删除其他用户的地址
     * - 这是防止越权操作的重要措施
     */
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 取消用户的默认地址（将所有地址设为非默认）
     * 
     * 用途：设置新地址为默认地址时，先取消其他地址的默认状态
     * 
     * @param userId 用户ID
     * @return 影响的行数（更新的地址数量）
     * 
     * 业务逻辑：
     * - 用户只能有一个默认地址
     * - 设置新地址为默认时，需要先将该用户的所有地址设为非默认
     * - 然后再将新地址设为默认
     */
    int cancelDefaultByUserId(@Param("userId") Long userId);
}
