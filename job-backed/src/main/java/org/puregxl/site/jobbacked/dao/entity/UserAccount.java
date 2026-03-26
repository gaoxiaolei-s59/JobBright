package org.puregxl.site.jobbacked.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("user_account")
@Builder
public class UserAccount {

    /** 主键ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 密码 */
    private String password;

    /** 显示名称 */
    private String displayName;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    private Integer delFlag;

}
