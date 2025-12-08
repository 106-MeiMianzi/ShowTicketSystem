package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // 确保导入了 List

/**
 * ShowService 接口的实现类
 */
@Service // 标记这是一个 Spring Service 组件
public class ShowServiceImpl implements ShowService {

    // 自动注入 ShowMapper 接口
    @Autowired
    private ShowMapper showMapper;

    /**
     * 实现发布新的演出逻辑
     * @param show 演出实体对象
     * @return 发布成功返回 true，失败返回 false
     */
    @Override
    public boolean publishShow(Show show) {
        // 1. 设置系统生成的或业务相关的默认值
        LocalDateTime now = LocalDateTime.now();

        // 确保 availableTickets 等于 totalTickets
        show.setAvailableTickets(show.getTotalTickets());

        // 设置状态为正常可售
        show.setStatus(1);

        // 设置创建和更新时间
        show.setCreateTime(now);
        show.setUpdateTime(now);

        // 2. 插入数据库
        int result = showMapper.insert(show);

        // 3. 返回结果
        return result == 1;
    } // <-- publishShow 方法的结束大括号在这里

    /**
     * 实现查询所有已发布的演出列表的逻辑 (新增)
     */
    @Override // <-- findAllShows 方法作为独立方法
    public List<Show> findAllShows() {
        // 直接调用 Mapper 层的方法，Service 层目前不涉及复杂业务逻辑
        return showMapper.findAll();
    }

    /**
     * 实现根据ID查询演出详情的逻辑 (新增)
     */
    @Override
    public Show getShowById(Long id) {
        return showMapper.getById(id);
    }

    /**
     * 更新演出信息 (新增)
     */
    @Override
    public boolean updateShow(Show show) {
        // 实际项目中，这里需要进行权限校验、字段校验等。
        // 特别是 totalTickets 的修改逻辑需要确保合理性，但我们这里只做基本更新。
        show.setUpdateTime(LocalDateTime.now());
        int updatedRows = showMapper.update(show);
        return updatedRows == 1;
    }

    /**
     * 删除演出信息 (新增)
     */
    @Override
    public boolean deleteShow(Long id) {
        // 实际项目中，删除演出前需要检查是否有未完成的订单关联，如果有则通常不允许删除或执行逻辑删除。
        int deletedRows = showMapper.delete(id);
        return deletedRows == 1;
    }
} // <-- ShowServiceImpl 类的结束大括号在这里
