package com.mianzi.showticketsystem.model.entity; // 假设你放在 model.vo 包下

import lombok.Data; // 导入 Lombok 的 @Data
import java.util.List;

/**
 * 通用分页结果封装类
 * @param <T> 列表数据的类型 (例如 Order)
 */
@Data // Lombok 注解，自动生成 Getter, Setter, toString, equals, hashCode
public class PageResult<T> {

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 当前页码
     */
    private int pageNum;

    /**
     * 每页记录数
     */
    private int pageSize;

    /**
     * 当前页的列表数据
     */
    private List<T> records;

    // 静态方法，方便构建分页结果对象
    public static <T> PageResult<T> build(long total, int pageNum, int pageSize, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        // 计算总页数：(总记录数 + 每页大小 - 1) / 每页大小
        long pages = (total + pageSize - 1) / pageSize;
        result.setPages(pages);

        result.setRecords(records);
        return result;
    }
}
