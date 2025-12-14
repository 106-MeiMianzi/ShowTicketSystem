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
//当 Spring Boot 启动时会进行组件扫描
//一旦发现 OrderServiceImpl 类上有 @Service 注解
//Spring 就会自动创建一个这个类的实例，并将其存储在 IOC 容器（中央管理系统）中。
//这个实例(Bean)准备就绪，可以被其他组件通过 @Autowired 来注入和使用
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
        LocalDateTime now = LocalDateTime.now();

        //初始化:确保可用/剩余票数等于总票数
        show.setAvailableTickets(show.getTotalTickets());

        //设置状态为正常可售
        show.setStatus(1);

        //设置创建和更新时间
        show.setCreateTime(now);
        show.setUpdateTime(now);

        //插入数据库
        int result = showMapper.insert(show);

        //返回结果
        return result == 1;
    }

    /**
     * 实现查询所有已发布的演出列表的逻辑
     */
    @Override
    public List<Show> findAllShows() {
        //调用 Mapper 层的方法
        return showMapper.findAll();
    }

    /**
     * 实现根据ID查询演出详情的逻辑
     */
    @Override
    public Show getShowById(Long id) {
        return showMapper.getById(id);
    }

    /**
     * 更新演出信息
     */
    @Override
    public boolean updateShow(Show show) {
        // 实际项目中，这里需要进行权限校验、字段校验等
        // 特别是 totalTickets 的修改逻辑需要确保合理性，但我们这里只做基本更新
        show.setUpdateTime(LocalDateTime.now());
        int updatedRows = showMapper.update(show);
        return updatedRows == 1;
    }

    /**
     * 删除演出信息
     */
    @Override
    public boolean deleteShow(Long id) {
        //实际项目中，删除演出前需要检查是否有未完成的订单关联
        //如果有则通常不允许删除或执行逻辑删除。
        int deletedRows = showMapper.delete(id);
        return deletedRows == 1;
    }
}
