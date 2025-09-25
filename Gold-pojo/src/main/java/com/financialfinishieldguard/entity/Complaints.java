package com.financialfinishieldguard.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 投诉信息表
 * @TableName complaints
 */
@Data
@Accessors(chain = true)
@TableName(value ="complaints")
public class Complaints {
    /**
     * id
     */
    private Long id;

    /**
     * 投诉内容
     */
    private String text;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * id
     */
    public Long getId() {
        return id;
    }

    /**
     * id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 投诉内容
     */
    public String getText() {
        return text;
    }

    /**
     * 投诉内容
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}