package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.model.entity.PageResult;
import com.mianzi.showticketsystem.model.entity.Show;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ShowService 接口的实现类
 * 
 * 作用：
 * - 实现ShowService接口定义的所有业务逻辑方法
 * - 调用Mapper层进行数据库操作
 * - 处理业务逻辑和异常情况
 * 
 * 职责：
 * - 演出发布和管理逻辑
 * - 演出查询和搜索逻辑
 * - 分页查询逻辑
 */
@Service
/**
 * @Service 注解说明：
 * 
 * 当 Spring Boot 启动时会进行组件扫描
 * 一旦发现 ShowServiceImpl 类上有 @Service 注解
 * Spring 就会自动创建一个这个类的实例，并将其存储在 IOC 容器（中央管理系统）中
 * 这个实例(Bean)准备就绪，可以被其他组件通过 @Autowired 来注入和使用
 * 
 * 标记这是一个 Spring Service 组件
 */
public class ShowServiceImpl implements ShowService {

    /**
     * 自动注入 ShowMapper 接口
     * 
     * @Autowired 注解：
     * - 自动注入ShowMapper的实现类（MyBatis自动生成）
     * - 用于执行数据库操作
     */
    @Autowired
    private ShowMapper showMapper;

    /**
     * 实现发布新的演出逻辑
     * 
     * 业务逻辑：
     * 1. 初始化可用票数（等于总票数）
     * 2. 设置状态为正常可售
     * 3. 设置创建时间和更新时间
     * 4. 插入数据库
     * 
     * @param show 演出实体对象
     * @return 发布成功返回true，失败返回false
     */
    @Override
    public boolean publishShow(Show show) {
        /**
         * 获取当前时间
         */
        LocalDateTime now = LocalDateTime.now();

        /**
         * 步骤1：初始化可用票数
         * 
         * 确保可用/剩余票数等于总票数
         * 新发布的演出，所有票都是可用的
         */
        show.setAvailableTickets(show.getTotalTickets());

        /**
         * 步骤2：设置状态为正常可售
         * 
         * status = 1 表示正常可售
         * status = 0 表示已取消
         */
        show.setStatus(1);

        /**
         * 步骤3：设置创建时间和更新时间
         */
        show.setCreateTime(now);
        show.setUpdateTime(now);

        /**
         * 步骤4：插入数据库
         * 
         * result = 1 表示插入成功（影响1行）
         * result = 0 表示插入失败
         */
        int result = showMapper.insert(show);

        /**
         * 返回结果
         */
        return result == 1;
    }

    /**
     * 实现查询所有已发布的演出列表的逻辑
     * 
     * 功能说明：
     * - 查询所有正常状态的演出
     * - 用户端使用
     * 
     * @return 演出列表
     */
    @Override
    public List<Show> findAllShows() {
        /**
         * 直接调用Mapper层的方法
         * 
         * Mapper层会过滤掉已取消的演出（status = 0）
         */
        return showMapper.findAll();
    }

    /**
     * 实现根据ID查询演出详情的逻辑
     * 
     * 功能说明：
     * - 用户端查询演出详情
     * - 只返回正常状态的演出
     * 
     * @param id 演出ID
     * @return 演出对象，如果不存在或已取消则返回null
     */
    @Override
    public Show getShowById(Long id) {
        /**
         * 调用Mapper层查询演出详情
         * 
         * Mapper层会过滤掉已取消的演出（status = 0）
         */
        return showMapper.getById(id);
    }

    /**
     * 更新演出信息
     * 
     * 功能说明：
     * - 管理员修改演出信息
     * - 可以更新部分字段（只更新非空字段）
     * 
     * @param show 包含新信息的演出对象，必须包含id
     * @return 成功返回true，失败返回false
     * 
     * 注意：
     * - 实际项目中，这里需要进行权限校验、字段校验等
     * - 特别是totalTickets的修改逻辑需要确保合理性
     * - 但我们这里只做基本更新
     */
    @Override
    public boolean updateShow(Show show) {
        /**
         * 设置更新时间
         */
        show.setUpdateTime(LocalDateTime.now());
        
        /**
         * 调用Mapper层更新演出信息
         * 
         * updatedRows = 1 表示更新成功（影响1行）
         * updatedRows = 0 表示更新失败（演出不存在）
         */
        int updatedRows = showMapper.update(show);
        return updatedRows == 1;
    }

    /**
     * 删除演出信息
     * 
     * 功能说明：
     * - 管理员删除演出（物理删除）
     * 
     * @param id 演出ID
     * @return 成功返回true，失败返回false
     * 
     * 注意：
     * - 实际项目中，删除演出前需要检查是否有未完成的订单关联
     * - 如果有则通常不允许删除或执行逻辑删除（将status设为0）
     */
    @Override
    public boolean deleteShow(Long id) {
        /**
         * 调用Mapper层删除演出
         * 
         * deletedRows = 1 表示删除成功（影响1行）
         * deletedRows = 0 表示删除失败（演出不存在）
         */
        int deletedRows = showMapper.delete(id);
        return deletedRows == 1;
    }

    /**
     * 实现在首页获取地区和分类演出列表的逻辑
     * 
     * 功能说明：
     * - 首页推荐演出
     * - 支持按地区和分类筛选
     * - 支持限制返回数量
     * 
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param limit 限制数量（可选）
     * @return 演出列表
     */
    @Override
    public List<Show> getHomeShows(String region, String category, Integer limit) {
        /**
         * 步骤1：如果没有指定地区，默认使用北京
         */
        if (region == null || region.isEmpty()) {
            region = "北京";
        }
        
        /**
         * 步骤2：如果没有指定数量限制，默认返回20条
         */
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        
        /**
         * 步骤3：调用Mapper层查询演出列表
         */
        return showMapper.findByRegionAndCategory(region, category, limit);
    }

    /**
     * 实现搜索演出的逻辑
     * 
     * 功能说明：
     * - 支持模糊搜索演出名称或场馆名称
     * - 用户端使用，公开接口
     * 
     * @param keyword 关键词
     * @return 演出列表
     */
    @Override
    public List<Show> searchShows(String keyword) {
        /**
         * 步骤1：空值检查
         */
        if (keyword == null || keyword.isEmpty()) {
            /**
             * 关键词为空，返回空列表
             * 
             * List.of()：创建不可变的空列表
             */
            return List.of();
        }
        
        /**
         * 步骤2：调用Mapper层搜索演出
         * 
         * Mapper层会使用LIKE查询匹配演出名称或场馆名称
         */
        return showMapper.searchShows(keyword);
    }

    /**
     * 实现条件查询演出的逻辑（分页）
     * 
     * 功能说明：
     * - 用户端演出列表页面
     * - 支持按地区和分类筛选
     * - 支持分页查询
     * 
     * @param region 城市（可选）
     * @param category 分类（可选）
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页结果，包含演出列表和分页信息
     */
    @Override
    public PageResult<Show> findShowsByConditions(String region, String category, int pageNum, int pageSize) {
        /**
         * 步骤1：参数校验
         */
        if (pageNum <= 0) pageNum = 1;      // 页码最小为1
        if (pageSize <= 0) pageSize = 10;    // 每页数量最小为10
        
        /**
         * 页大小上限限制，防止恶意请求导致数据库压力过大
         */
        if (pageSize > 100) pageSize = 100;

        /**
         * 步骤2：计算偏移量
         * 
         * offset = (pageNum - 1) * pageSize
         */
        int offset = (pageNum - 1) * pageSize;

        /**
         * 步骤3：查询总记录数
         * 
         * 用于计算总页数
         */
        long total = showMapper.countShowsByConditions(region, category);

        /**
         * 步骤4：如果总记录数为0，直接返回空结果
         * 
         * 避免不必要的数据库查询
         */
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        /**
         * 步骤5：分页查询列表数据
         */
        List<Show> records = showMapper.findShowsByConditions(region, category, offset, pageSize);

        /**
         * 步骤6：封装为PageResult并返回
         */
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 实现管理端分页查询演出列表的逻辑
     * 
     * 功能说明：
     * - 管理端演出列表页面
     * - 支持多条件筛选（名称、地区、分类、状态）
     * - 支持分页查询
     * - 可以查看所有状态的演出（包括已取消的）
     * 
     * @param name 演出名称（可选），用于模糊查询
     * @param region 地区（可选）
     * @param category 分类（可选）
     * @param status 状态（可选），1=正常，0=已取消
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页结果，包含演出列表和分页信息
     */
    @Override
    public PageResult<Show> getShowListForAdmin(String name, String region, String category, Integer status, int pageNum, int pageSize) {
        /**
         * 步骤1：参数校验
         */
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        
        /**
         * 页大小上限限制，防止恶意请求导致数据库压力过大
         */
        if (pageSize > 100) pageSize = 100;

        /**
         * 步骤2：计算偏移量
         */
        int offset = (pageNum - 1) * pageSize;

        /**
         * 步骤3：查询总记录数
         * 
         * 管理端可以查询所有状态的演出
         */
        long total = showMapper.countShowsForAdmin(name, region, category, status);

        /**
         * 步骤4：如果总记录数为0，直接返回空结果
         */
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        /**
         * 步骤5：分页查询列表数据
         */
        List<Show> records = showMapper.findShowsForAdmin(name, region, category, status, offset, pageSize);

        /**
         * 步骤6：封装为PageResult并返回
         */
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 实现管理端查询演出详情的逻辑
     * 
     * 功能说明：
     * - 管理端查询演出详情
     * - 可以查看所有状态的演出（包括已取消的）
     * - 显示明确的库存数字
     * 
     * @param id 演出ID
     * @return 演出对象，如果不存在则返回null
     */
    @Override
    public Show getShowByIdForAdmin(Long id) {
        /**
         * 调用Mapper层查询演出详情（管理端版本）
         * 
         * 与getById的区别：不限制status，可以查询已取消的演出
         */
        return showMapper.getByIdForAdmin(id);
    }
}
