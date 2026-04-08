package org.puregxl.site.clawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("company")
public class Company {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("company_id")
    private String companyId;

    @TableField("company_name")
    private String companyName;

    @TableField("company_logo")
    private String companyLogo;

    @TableField("industry_name")
    private String industryName;

    @TableField("company_stage")
    private String companyStage;

    @TableField("company_size")
    private String companySize;

    @TableField("company_intro")
    private String companyIntro;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("del_flag")
    private Integer delFlag = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }
    public String getIndustryName() { return industryName; }
    public void setIndustryName(String industryName) { this.industryName = industryName; }
    public String getCompanyStage() { return companyStage; }
    public void setCompanyStage(String companyStage) { this.companyStage = companyStage; }
    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }
    public String getCompanyIntro() { return companyIntro; }
    public void setCompanyIntro(String companyIntro) { this.companyIntro = companyIntro; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDelFlag() { return delFlag; }
    public void setDelFlag(Integer delFlag) { this.delFlag = delFlag; }
}
