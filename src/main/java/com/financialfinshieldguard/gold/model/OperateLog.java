package com.financialfinshieldguard.gold.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 操作日志表
 * @TableName operate_log
 */
@TableName(value ="operate_log")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class OperateLog {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    private Long operateUser;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 操作的类名
     */
    private String className;

    /**
     * 操作的方法名
     */
    private String methodName;

    /**
     * 方法参数
     */
    private String methodParams;

    /**
     * 返回值
     */
    private String returnValue;

    /**
     * 方法执行耗时, 单位:ms
     */
    private Long costTime;
}