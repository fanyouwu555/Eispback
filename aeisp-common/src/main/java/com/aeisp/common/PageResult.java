package com.aeisp.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类。
 *
 * <p>用于统一封装分页查询的返回结果，支持从 MyBatis-Plus 的 {@link IPage} 快速转换。</p>
 *
 * @param <T> 列表元素类型
 * @author AEISP Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表。
     */
    private List<T> list;

    /**
     * 总记录数。
     */
    private Long total;

    /**
     * 当前页码。
     */
    private Long pageNum;

    /**
     * 每页大小。
     */
    private Long pageSize;

    /**
     * 总页数。
     */
    private Long totalPages;

    /**
     * 从 MyBatis-Plus 的 IPage 构造分页结果。
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>   元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return PageResult.<T>builder()
                .list(page.getRecords())
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .totalPages(page.getPages())
                .build();
    }

    /**
     * 从 MyBatis-Plus 的 IPage 构造分页结果（支持记录转换）。
     *
     * @param page    MyBatis-Plus 分页对象
     * @param records 转换后的记录列表
     * @param <T>     目标元素类型
     * @param <R>     源元素类型
     * @return 分页结果
     */
    public static <T, R> PageResult<T> of(IPage<R> page, List<T> records) {
        return PageResult.<T>builder()
                .list(records)
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .totalPages(page.getPages())
                .build();
    }
}
