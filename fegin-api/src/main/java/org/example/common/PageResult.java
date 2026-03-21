package org.example.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 */
@Data
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 每页记录数
     */
    private int pageSize;
    
    /**
     * 当前页码
     */
    private int currentPage;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 构造方法
     */
    public PageResult() {
    }
    
    /**
     * 构造方法
     */
    public PageResult(long total, int pageSize, int currentPage, List<T> list) {
        this.total = total;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.list = list;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
    
    /**
     * 静态方法
     */
    public static <T> PageResult<T> of(long total, int pageSize, int currentPage, List<T> list) {
        return new PageResult<>(total, pageSize, currentPage, list);
    }
}