package org.puregxl.site.jobbacked.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("company")
public class Company {

    /** 主键ID */
    private Long id;

    /** 公司业务ID */
    private String companyId;

    /** 公司名称 */
    private String companyName;

    /** 公司Logo地址 */
    private String companyLogo;

    /** 行业名称 */
    private String industryName;

    /** 公司阶段 */
    private String companyStage;

    /** 公司规模 */
    private String companySize;

    /** 公司简介 */
    private String companyIntro;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 删除标识 0-未删除 1-已删除 */
    private Integer delFlag;
}
