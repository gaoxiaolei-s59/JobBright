package org.puregxl.site.jobbacked.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_resume_manual_content")
public class UserResumeManualContent {

    private Long id;

    private Long userId;

    private String resumeId;

    private String contentJson;

    private String status;

    private Date createTime;

    private Date updateTime;

    private Integer delFlag;
}
