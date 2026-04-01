package org.puregxl.site.jobbacked.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("user_job_action")
public class UserJobAction {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 职位业务ID */
    private String jobId;

    /** 是否收藏 0-否 1-是 */
    private Integer liked;

    /** 是否投递 0-否 1-是 */
    private Integer applied;

    /** 投递时间 */
    private Date appliedTime;

    /** 最近查看时间 */
    private Date lastViewTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 删除标识 0-未删除 1-已删除 */
    private Integer delFlag;
}
