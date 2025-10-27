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
 * 会话消息表
 * @TableName session_messages
 */
@TableName(value ="session_messages")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessages {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 发送者ID
     */
    private Long senderUserId;

    /**
     * 0-AI 1-用户
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}