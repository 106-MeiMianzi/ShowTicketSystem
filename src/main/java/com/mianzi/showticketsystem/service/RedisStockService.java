package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Redis库存管理服务
 * 
 * 作用：
 * - 使用Redis原子操作管理库存，防止超卖
 * - 同步Redis库存和数据库库存
 * 
 * 说明：
 * - Redis存储库存用于快速扣减和查询
 * - 数据库存储真实库存，定期同步
 */
@Service
public class RedisStockService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ShowMapper showMapper;

    /**
     * 初始化演出库存到Redis
     * 
     * @param showId 演出ID
     * @param stock 库存数量
     */
    public void initStock(Long showId, Integer stock) {
        String stockKey = "stock:show:" + showId;
        redisUtil.set(stockKey, stock, 86400 * 7); // 7天过期
    }

    /**
     * 从数据库同步库存到Redis
     * 
     * @param showId 演出ID
     */
    public void syncStockFromDatabase(Long showId) {
        Show show = showMapper.getById(showId);
        if (show != null) {
            initStock(showId, show.getAvailableTickets());
        }
    }

    /**
     * 扣减库存（原子操作）
     * 
     * @param showId 演出ID
     * @param quantity 扣减数量
     * @return 扣减后的剩余库存，如果库存不足返回-1
     */
    public Long decreaseStock(Long showId, Integer quantity) {
        String stockKey = "stock:show:" + showId;
        
        // 先检查Redis中是否有库存数据，如果没有则从数据库同步
        if (!redisUtil.hasKey(stockKey)) {
            syncStockFromDatabase(showId);
        }
        
        // 原子递减操作
        Long remaining = redisUtil.decrement(stockKey, quantity);
        
        if (remaining < 0) {
            // 库存不足，回滚
            redisUtil.increment(stockKey, quantity);
            return -1L;
        }
        
        return remaining;
    }

    /**
     * 增加库存（原子操作）
     * 
     * @param showId 演出ID
     * @param quantity 增加数量
     * @return 增加后的库存
     */
    public Long increaseStock(Long showId, Integer quantity) {
        String stockKey = "stock:show:" + showId;
        
        // 先检查Redis中是否有库存数据，如果没有则从数据库同步
        if (!redisUtil.hasKey(stockKey)) {
            syncStockFromDatabase(showId);
        }
        
        return redisUtil.increment(stockKey, quantity);
    }

    /**
     * 获取当前库存
     * 
     * @param showId 演出ID
     * @return 当前库存，如果不存在返回null
     */
    public Integer getStock(Long showId) {
        String stockKey = "stock:show:" + showId;
        Object stock = redisUtil.get(stockKey);
        
        if (stock == null) {
            // Redis中没有，从数据库同步
            syncStockFromDatabase(showId);
            stock = redisUtil.get(stockKey);
        }
        
        if (stock instanceof Integer) {
            return (Integer) stock;
        } else if (stock instanceof Long) {
            return ((Long) stock).intValue();
        } else if (stock instanceof String) {
            return Integer.valueOf((String) stock);
        }
        
        return null;
    }
}
