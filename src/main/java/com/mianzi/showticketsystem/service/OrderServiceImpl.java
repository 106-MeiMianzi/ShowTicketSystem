package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.OrderMapper;
import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.model.entity.Order;
import com.mianzi.showticketsystem.model.entity.Show;
import com.mianzi.showticketsystem.service.OrderService;
import com.mianzi.showticketsystem.service.ShowService;
import com.mianzi.showticketsystem.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.mianzi.showticketsystem.model.entity.PageResult;
import java.util.List;

/**
 * OrderService 接口的实现类
 * 
 * 作用：
 * - 实现OrderService接口定义的所有业务逻辑方法
 * - 调用Mapper层进行数据库操作
 * - 处理业务逻辑和异常情况
 * 
 * 职责：
 * - 订单创建和管理逻辑
 * - 订单状态管理逻辑
 * - 库存管理逻辑（下单减库存，取消还库存）
 * - 分页查询逻辑
 */
@Service
/**
 * @Service 注解：
 * - 标识这是一个Service层的Bean
 * - Spring会自动扫描并创建这个类的实例
 */
public class OrderServiceImpl implements OrderService {

    /**
     * 注入Mapper和Service
     */
    
    /**
     * 注入订单Mapper
     * 
     * 用于执行订单相关的数据库操作
     */
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 注入演出Mapper
     * 
     * 用于执行演出库存相关的数据库操作
     */
    @Autowired
    private ShowMapper showMapper;

    /**
     * 注入演出Service
     * 
     * 用于查询演出信息（获取价格等）
     */
    @Autowired
    private ShowService showService;

    /**
     * 注入Redis工具类
     * 
     * 用于分布式锁和限流
     */
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 注入Redis库存服务
     * 
     * 用于Redis库存管理
     */
    @Autowired
    private RedisStockService redisStockService;

    /**
     * 预订票务的核心方法，确保减库存和创建订单在同一个事务中
     * 
     * 业务逻辑：
     * 1. 查询演出信息并验证库存
     * 2. 计算订单总金额
     * 3. 原子性减少库存
     * 4. 创建订单记录
     * 
     * @param userId 购票用户ID
     * @param showId 演出ID
     * @param quantity 购买数量
     * @return 成功返回订单对象，失败返回null
     */
    @Override
    /**
     * @Override 注解说明：
     * 
     * 可读性：这是一个重写的方法
     * 如果拼写错误，会立即报错
     */
    @Transactional
    /**
     * @Transactional 注解说明：
     * 
     * 声明式事务管理
     * 
     * 作用：
     * - 确保方法内的所有数据库操作在同一个事务中执行
     * - 如果任何操作失败，所有操作都会回滚
     * - 防止出现订单创建成功但库存未扣减的数据不一致问题
     * 
     * 使用场景：
     * - 例如，在创建订单时，需要执行插入订单和扣减库存两个数据库操作
     * - 如果其中任何一个失败，@Transactional 就能确保 Spring 框架自动将所有已执行的操作回滚
     * - 防止出现订单创建成功但库存未扣减的数据不一致问题
     */
    public Order createOrder(Long userId, Long showId, Integer quantity) {

        /**
         * 步骤0：分布式锁防止重复下单
         */
        String lockKey = "lock:order:" + userId + ":" + showId;
        String lockValue = UUID.randomUUID().toString();
        boolean lockAcquired = redisUtil.tryLock(lockKey, lockValue, 10); // 10秒超时
        
        if (!lockAcquired) {
            // 获取锁失败，可能是重复提交
            throw new RuntimeException("请勿重复提交订单，请稍后再试");
        }

        try {
            /**
             * 步骤1：业务校验和获取价格
             */

            /**
             * 步骤1.1：获取演出信息，用于价格计算
             * 
             * 查询演出信息，获取价格和库存
             */
            Show show = showService.getShowById(showId);

            if (show == null) {
                /**
                 * 演出不存在或状态不正常
                 * getShowById 已在 Mapper 中通过 status = 1 过滤
                 */
                return null;
            }

            /**
             * 步骤1.2：校验购买数量
             * 
             * 购买数量必须大于0
             */
            if (quantity == null || quantity <= 0) {
                return null;
            }

            /**
             * 步骤1.3：使用Redis检查库存（快速检查）
             */
            Integer redisStock = redisStockService.getStock(showId);
            if (redisStock == null || quantity > redisStock) {
                /**
                 * Redis库存不足，返回null
                 */
                // TODO：该报错的地方没报错
                return null;
            }

            /**
             * 步骤1.4：计算总金额
             * 
             * 从数据库获取实时价格
             */
            BigDecimal price = show.getPrice();
            
            /**
             * BigDecimal 说明：
             * 
             * BigDecimal 可以精确地表示和计算任何大小和精度的小数
             * 专门用于避免浮点数计算误差
             * 
             * 计算总金额（价格 * 数量）
             * 
             * multiply()：乘法运算，返回新的BigDecimal对象
             */
            BigDecimal totalPrice = price.multiply(new BigDecimal(quantity));

            /**
             * 步骤2：使用Redis原子操作减库存
             */

            /**
             * 步骤2.1：使用Redis原子操作扣减库存
             * 
             * Redis的decrement是原子操作，可以防止超卖
             */
            Long remaining = redisStockService.decreaseStock(showId, quantity);

            if (remaining < 0) {
                /**
                 * Redis库存扣减失败，库存不足
                 * 直接返回 null
                 */
                return null;
            }

            /**
             * 步骤2.2：同步扣减数据库库存（保证数据一致性）
             */
            int updatedRows = showMapper.updateStock(showId, quantity);

            if (updatedRows == 0) {
                /**
                 * 数据库库存扣减失败，回滚Redis库存
                 */
                redisStockService.increaseStock(showId, quantity);
                return null;
            }

        /**
         * 步骤3：创建订单记录
         */

        /**
         * 步骤3.1：生成商户订单号
         * 
         * 格式：订单前缀 + 时间戳 + 用户ID
         * 
         * System.currentTimeMillis()：获取当前时间戳（毫秒）
         * 确保订单号唯一
         */
        String outTradeNo = "ORD" + System.currentTimeMillis() + userId;

        /**
         * 步骤3.2：构建订单对象
         * 
         * 使用链式调用设置属性
         */
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order()
                .setUserId(userId)              // 用户ID
                .setShowId(showId)              // 演出ID
                .setOutTradeNo(outTradeNo)      // 商户订单号
                .setQuantity(quantity)          // 购买数量
                .setTotalPrice(totalPrice)      // 订单总金额
                .setStatus(1)                   // 1: 待支付
                .setOrderTime(now)              // 下单时间
                .setCreateTime(now)             // 创建时间
                .setUpdateTime(now);            // 更新时间

        /**
         * 步骤3.3：插入订单记录
         */
        int result = orderMapper.insert(order);

            if (result == 1) {
                /**
                 * 订单创建成功，返回订单对象
                 * 
                 * order.getId()会返回数据库生成的主键ID
                 */
                return order;
            } else {
                /**
                 * 订单插入失败，回滚Redis库存并抛出异常触发事务回滚
                 */
                redisStockService.increaseStock(showId, quantity);
                throw new RuntimeException("创建订单失败，事务回滚。");
            }
        } finally {
            // 释放分布式锁
            redisUtil.releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 实现根据订单ID和用户ID查询订单详情的逻辑
     * 
     * 功能说明：
     * - 用户端查询订单详情（需要验证用户ID）
     * - 管理端查询订单详情（userId可为null）
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（可为null，用于管理端查询）
     * @return 订单对象，如果不存在或不属于该用户则返回null
     */
    @Override
    public Order getOrderDetails(Long orderId, Long userId) {
        if (userId != null) {
            /**
             * 用户端查询，需要校验用户ID
             */
            Order order = orderMapper.getByIdAndUserId(orderId, userId);
            
            /**
             * 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
             */
            if (order != null && !order.getUserId().equals(userId)) {
                /**
                 * 订单不属于当前用户，返回null
                 */
                return null;
            }
            return order;
        } else {
            /**
             * 管理端查询，只需要订单ID
             * 
             * 管理端可以查询所有订单，不需要验证用户ID
             */
            return orderMapper.getById(orderId);
        }
    }

    /**
     * 实现用户取消订单并释放库存的逻辑
     * 
     * 业务逻辑：
     * 1. 查询订单并验证权限
     * 2. 验证订单状态（只有待支付才能取消）
     * 3. 恢复演出库存
     * 4. 更新订单状态为已取消
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 成功返回true，失败返回false
     */
    @Override
    @Transactional
    /**
     * @Transactional 注解：
     * - 确保方法中的数据库操作是原子性的
     * - 恢复库存和更新订单状态必须同时成功或同时失败
     */
    public boolean cancelOrder(Long orderId, Long userId) {

        /**
         * 步骤1：获取订单信息
         * 
         * 查询订单并验证订单属于该用户
         */
        Order order = orderMapper.getByIdAndUserId(orderId, userId);

        if (order == null) {
            /**
             * 订单不存在或不属于该用户
             */
            return false;
        }

        /**
         * 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
         */
        if (!order.getUserId().equals(userId)) {
            /**
             * 订单不属于当前用户
             */
            return false;
        }

        /**
         * 定义订单状态常量
         * 
         * 使用常量而不是魔法数字，提高代码可读性
         */
        final int STATUS_PENDING_PAYMENT = 1; // 待支付
        final int STATUS_CANCELED = 3;        // 已取消（注意：这里应该是3，不是4）

        /**
         * 步骤2：业务校验：只有"待支付"状态的订单才能取消
         */
        if (order.getStatus() != STATUS_PENDING_PAYMENT) {
            /**
             * 订单状态不正确（可能已支付、已完成或已取消）
             */
            return false;
        }

        /**
         * 步骤3：返还库存操作（Redis + 数据库）
         */

        /**
         * 步骤3.1：先返还Redis库存
         */
        redisStockService.increaseStock(order.getShowId(), order.getQuantity());

        /**
         * 步骤3.2：返还数据库库存
         * 
         * ShowMapper.updateStock 是减库存，我们需要一个对应的增库存方法
         * addStock()方法会增加指定数量的库存
         */
        int stockUpdatedRows = showMapper.addStock(order.getShowId(), order.getQuantity());

        if (stockUpdatedRows == 0) {
            /**
             * 数据库库存返还失败，回滚Redis库存
             */
            redisStockService.decreaseStock(order.getShowId(), order.getQuantity());
            throw new RuntimeException("返还库存失败，事务回滚");
        }

        /**
         * 步骤4：更新订单状态
         */

        /**
         * 调用 OrderMapper 更新状态
         * 
         * updateStatus()方法会：
         * 1. 验证订单ID和用户ID匹配
         * 2. 验证旧状态匹配（必须是待支付）
         * 3. 更新订单状态
         */
        int orderUpdatedRows = orderMapper.updateStatus(
                orderId,
                userId,
                STATUS_CANCELED,           // 新状态：已取消
                STATUS_PENDING_PAYMENT      // 旧状态必须是待支付
        );

        if (orderUpdatedRows == 1) {
            /**
             * 订单状态更新成功，事务提交
             */
            return true;
        } else {
            /**
             * 订单状态更新失败，抛出异常回滚库存返还
             * 
             * 抛出异常后，@Transactional 会自动回滚之前的所有操作
             * 包括库存返还操作
             */
            throw new RuntimeException("更新订单状态失败，事务回滚。");
        }
    }

    /**
     * 模拟支付成功逻辑：更新订单状态和支付时间
     * 
     * 功能说明：
     * - 用户支付订单（简化版，直接更新状态）
     * - 实际项目中应该调用支付接口
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 成功返回true，失败返回false
     */
    @Override
    public boolean payOrder(Long orderId, Long userId) {

        /**
         * 定义订单状态常量
         */
        final int STATUS_PENDING_PAYMENT = 1; // 待支付
        final int STATUS_PAID = 2;            // 已支付

        /**
         * 步骤1：先查询订单，验证订单是否存在且属于当前用户
         */
        Order order = orderMapper.getByIdAndUserId(orderId, userId);
        if (order == null) {
            /**
             * 订单不存在或不属于该用户
             */
            return false;
        }
        
        /**
         * 双重验证：确保返回的订单确实属于当前用户（防止SQL注入或其他安全问题）
         */
        if (!order.getUserId().equals(userId)) {
            /**
             * 订单不属于当前用户
             */
            return false;
        }
        
        /**
         * 步骤2：验证订单状态是否为待支付
         */
        if (order.getStatus() != STATUS_PENDING_PAYMENT) {
            /**
             * 订单状态不正确（可能已支付或已取消）
             */
            return false;
        }

        /**
         * 步骤3：调用 Mapper 更新订单状态和支付时间
         * 
         * updateStatusAndPayTime 方法会同时确保：
         * 1. 只有 status = 1 (待支付) 的订单才会被更新
         * 2. 只有 user_id = userId 的订单才会被更新（权限校验）
         * 3. 成功时更新 status, pay_time, update_time 三个字段
         */
        int updatedRows = orderMapper.updateStatusAndPayTime(
                orderId,
                userId,
                STATUS_PAID,            // 新状态：已支付
                STATUS_PENDING_PAYMENT  // 旧状态：待支付
        );

        /**
         * 返回结果
         * 
         * updatedRows = 1 表示更新成功（影响1行）
         * updatedRows = 0 表示更新失败（状态不匹配或订单不存在）
         */
        return updatedRows == 1;
    }

    /**
     * 实现分页查询指定用户的订单列表的逻辑
     * 
     * 功能说明：
     * - 用户端查询自己的订单列表
     * - 支持分页查询
     * 
     * @param userId 用户ID
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    @Override
    public PageResult<Order> getUserOrderList(Long userId, int pageNum, int pageSize) {
        // TODO：分页查询推荐使用Mybatis的分页插件

        /**
         * 步骤1：参数校验，确保 pageNum 和 pageSize 有效
         */
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        
        /**
         * 页大小上限限制，防止恶意请求导致数据库压力过大
         */
        if (pageSize > 100) pageSize = 100;

        /**
         * 步骤2：计算偏移量 offset（就是跳过用户查询的页数的前几页）
         * 
         * offset = (pageNum - 1) * pageSize
         * 例如：第2页，每页10条 -> offset = 10（跳过前10条）
         */
        int offset = (pageNum - 1) * pageSize;

        /**
         * 步骤3：查询总记录数
         * 
         * 用于计算总页数
         */
        long total = orderMapper.countOrdersByUserId(userId);

        /**
         * 步骤4：如果总记录数为0，直接返回空结果
         * 
         * 避免不必要的数据库查询
         */
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of()); // 使用 List.of() 返回空列表
        }

        /**
         * 步骤5：分页查询列表数据
         */
        List<Order> records = orderMapper.findOrdersByUserId(userId, offset, pageSize);

        /**
         * 步骤6：封装为 PageResult 并返回
         */
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 管理端 - 实现分页查询所有订单列表的逻辑
     * 
     * 功能说明：
     * - 管理端查询所有订单
     * - 支持分页查询
     * 
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含所有订单列表的分页结果对象
     */
    @Override
    public PageResult<Order> getAllOrderList(int pageNum, int pageSize) {

        /**
         * 步骤1：参数校验，确保 pageNum 和 pageSize 有效
         */
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;
        
        /**
         * 页大小上限限制，防止恶意请求导致数据库压力过大
         */
        if (pageSize > 100) pageSize = 100;

        /**
         * 步骤2：计算偏移量 offset
         */
        int offset = (pageNum - 1) * pageSize;

        /**
         * 步骤3：查询总记录数（调用 countAllOrders）
         * 
         * 管理端可以查询所有订单
         */
        long total = orderMapper.countAllOrders();

        /**
         * 步骤4：如果总记录数为0，直接返回空结果
         */
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        /**
         * 步骤5：分页查询列表数据（调用 findAllOrders）
         */
        List<Order> records = orderMapper.findAllOrders(offset, pageSize);

        /**
         * 步骤6：封装为 PageResult 并返回
         */
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 管理端 - 手动更新订单状态的逻辑
     * 
     * 功能说明：
     * - 管理员手动修改订单状态
     * - 用于处理异常订单（如手动退款、强制取消等）
     * 
     * @param orderId 订单ID
     * @param newStatus 新状态（例如：1=待支付, 2=已支付, 3=已取消, 4=已退款）
     * @return 成功返回true，失败返回false
     */
    @Override
    @Transactional
    /**
     * @Transactional 注解：
     * - 虽然这个方法只更新订单状态，但使用事务可以保证操作的原子性
     */
    public boolean updateOrderStatus(Long orderId, Integer newStatus) {
        /**
         * 步骤1：参数校验
         */
        if (orderId == null || newStatus == null) {
            return false;
        }

        /**
         * 步骤2：调用管理员专用的 Mapper 方法
         * 
         * adminUpdateStatus()方法：
         * - 不检查旧状态
         * - 不检查用户ID
         * - 管理员有更高权限，可以强制更新订单状态
         */
        int updatedRows = orderMapper.adminUpdateStatus(orderId, newStatus);

        /**
         * 说明：
         * - 提供一个灵活的、强制性的工具，让管理员能够纠正错误或处理异常情况
         * - 管理员操作涉及人工介入也就是手动操作
         * - 我们暂时只实现状态更新，不自动触发库存逻辑
         * - 如果需要恢复库存，管理员需要手动操作
         */
        return updatedRows > 0;
    }

    /**
     * 用户端 - 条件查询订单列表（分页）
     * 
     * 功能说明：
     * - 用户端查询自己的订单列表，支持按状态筛选
     * - 支持分页查询
     * 
     * @param userId 用户ID
     * @param status 订单状态（可选），1=待支付，2=已支付，3=已取消，4=已退款
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    @Override
    public PageResult<Order> getUserOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize) {
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
         */
        long total = orderMapper.countOrdersByUserIdWithConditions(userId, status);

        /**
         * 步骤4：如果总记录数为0，直接返回空结果
         */
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        /**
         * 步骤5：分页查询列表数据
         */
        List<Order> records = orderMapper.findOrdersByUserIdWithConditions(userId, status, offset, pageSize);

        /**
         * 步骤6：封装为 PageResult 并返回
         */
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 管理端 - 条件查询订单列表（分页）
     * 
     * 功能说明：
     * - 管理端查询订单，支持多条件筛选
     * - 支持分页查询
     * 
     * @param userId 用户ID（可选），如果为null则查询所有用户
     * @param status 订单状态（可选），1=待支付，2=已支付，3=已取消，4=已退款
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含订单列表的分页结果对象
     */
    @Override
    public PageResult<Order> getAllOrderListWithConditions(Long userId, Integer status, int pageNum, int pageSize) {
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
         * 管理端可以查询所有用户的订单
         */
        long total = orderMapper.countOrdersForAdmin(userId, status);

        /**
         * 步骤4：如果总记录数为0，直接返回空结果
         */
        if (total == 0) {
            return PageResult.build(0, pageNum, pageSize, List.of());
        }

        /**
         * 步骤5：分页查询列表数据
         */
        List<Order> records = orderMapper.findOrdersForAdmin(userId, status, offset, pageSize);

        /**
         * 步骤6：封装为 PageResult 并返回
         */
        return PageResult.build(total, pageNum, pageSize, records);
    }

    /**
     * 根据商户订单号查询订单
     * 
     * 功能说明：
     * - 支付回调时根据商户订单号查找订单
     * - 用于支付处理
     * 
     * @param outTradeNo 商户订单号（系统生成的唯一订单号）
     * @return 订单对象，如果不存在则返回null
     */
    @Override
    public Order getOrderByOutTradeNo(String outTradeNo) {
        /**
         * 直接调用Mapper层查询订单
         */
        return orderMapper.getByOutTradeNo(outTradeNo);
    }
}
