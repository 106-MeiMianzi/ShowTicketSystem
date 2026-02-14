package com.mianzi.showticketsystem.model.entity;

import lombok.Data;
import java.util.List;

/**
 * 通用分页结果封装类
 * 
 * 作用：
 * - 封装分页查询的结果数据
 * - 统一分页响应格式，包含分页信息和数据列表
 * - 用于 Controller 返回分页数据给前端
 * 
 * 泛型说明：
 * @param <T> 列表数据的类型，例如 Order、Show、User 等
 * 
 * 使用示例：
 * PageResult<Order> pageResult = orderService.getOrderList(...);
 * // 返回包含订单列表和分页信息的结果
 */
@Data
/**
 * @Data 注解：自动生成 getter、setter、toString、equals、hashCode 等方法
 */
public class PageResult<T> {

    /**
     * 总记录数
     * 
     * 说明：
     * - 数据库中符合条件的总记录数（不是当前页的记录数）
     * - 用于前端显示"共 X 条记录"
     * 
     * 类型：long（而不是 int），因为记录数可能很大
     */
    private long total;

    /**
     * 总页数
     * 
     * 说明：
     * - 根据总记录数和每页记录数计算得出
     * - 计算公式：pages = (total + pageSize - 1) / pageSize
     * - 用于前端显示"共 X 页"
     * 
     * 计算示例：
     * - total = 100, pageSize = 10 -> pages = 10
     * - total = 101, pageSize = 10 -> pages = 11（向上取整）
     */
    private long pages;

    /**
     * 当前页码
     * 
     * 说明：
     * - 用户请求的页码（从 1 开始）
     * - 用于前端显示"第 X 页"
     */
    private int pageNum;

    /**
     * 每页记录数
     * 
     * 说明：
     * - 每页显示的记录数量
     * - 通常由前端传递，如 10、20、50 等
     */
    private int pageSize;

    /**
     * 当前页的列表数据
     * 
     * 说明：
     * - 当前页的实际数据列表
     * - 类型为 List<T>，T 是泛型参数
     * - 例如：PageResult<Order> 的 records 是 List<Order>
     */
    private List<T> records;

    /**
     * 静态方法：构建分页结果对象
     * 
     * 作用：提供一个便捷的方法来创建 PageResult 对象
     * 
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @param records 当前页的数据列表
     * @param <T> 数据类型
     * @return PageResult 对象
     * 
     * 使用示例：
     * List<Order> orders = ...; // 查询当前页的订单
     * long total = ...; // 查询总记录数
     * PageResult<Order> result = PageResult.build(total, 1, 10, orders);
     */
    public static <T> PageResult<T> build(long total, int pageNum, int pageSize, List<T> records) {
        // 创建 PageResult 对象
        PageResult<T> result = new PageResult<>();
        
        // 设置总记录数
        result.setTotal(total);
        
        // 设置当前页码
        result.setPageNum(pageNum);
        
        // 设置每页记录数
        result.setPageSize(pageSize);

        /**
         * 计算总页数
         * 
         * 公式：(总记录数 + 每页大小 - 1) / 每页大小
         * 
         * 原理：向上取整
         * - 例如：total = 100, pageSize = 10 -> (100 + 10 - 1) / 10 = 10
         * - 例如：total = 101, pageSize = 10 -> (101 + 10 - 1) / 10 = 11
         * 
         * 为什么是 (total + pageSize - 1)？
         * - 这是整数除法的向上取整技巧
         * - 等价于 Math.ceil((double)total / pageSize)，但避免了浮点数运算
         */
        long pages = (total + pageSize - 1) / pageSize;
        result.setPages(pages);

        // 设置当前页的数据列表
        result.setRecords(records);
        
        // 返回构建好的分页结果对象
        return result;
    }
}
