package com.financialfinishieldguard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户会话表
 * @TableName user_sessions
 */
@TableName(value ="user_sessions")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserSessions {
    /**
     * 主键sessionID
     */
    @TableId(type = IdType.AUTO)
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}