package com.github.mongo.support.utils;

import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页
 *
 * @author max
 */
@ToString
public class Pager<T> implements Serializable {
    private static final long serialVersionUID = 7812799091682959210L;
    /**
     * 对象列表
     */
    private List<T> data = new ArrayList<>();
    /**
     * 下次拉取开始时间
     */
    private Long lastTime;
    /**
     * 页面大小
     */
    private Integer pageSize = 10;
    /**
     * 当前页
     */
    private Integer currentPage = 1;
    /**
     * 页面总数
     */
    private Integer pageTotal = 0;
    /**
     * 记录数
     */
    private Integer recordSize = 0;

    /**
     * 获取offset
     *
     * @return offset
     */
    public int offset() {
        return (currentPage - 1) * pageSize;
    }

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage < 1) {
            currentPage = 1;
        }
        this.currentPage = currentPage;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public Integer getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        if (recordSize >= 0) {
            pageTotal = recordSize / pageSize;
            if (recordSize % pageSize != 0) {
                pageTotal++;
            }
            this.recordSize = recordSize;
        }
    }
}
