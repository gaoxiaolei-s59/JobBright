ALTER TABLE job_post
    ADD COLUMN IF NOT EXISTS job_description MEDIUMTEXT NULL COMMENT '职位详情描述' AFTER job_summary,
    ADD COLUMN IF NOT EXISTS role_category VARCHAR(64) NULL COMMENT '岗位角色类别' AFTER job_description,
    ADD COLUMN IF NOT EXISTS role_tags TEXT NULL COMMENT '岗位角色标签(JSON或逗号分隔)' AFTER role_category,
    ADD COLUMN IF NOT EXISTS skill_tags TEXT NULL COMMENT '技能标签(JSON或逗号分隔)' AFTER role_tags,
    ADD COLUMN IF NOT EXISTS industry_tags TEXT NULL COMMENT '行业标签(JSON或逗号分隔)' AFTER skill_tags,
    ADD COLUMN IF NOT EXISTS benefit_tags TEXT NULL COMMENT '福利标签(JSON或逗号分隔)' AFTER industry_tags,
    ADD COLUMN IF NOT EXISTS highlight_tags TEXT NULL COMMENT '岗位亮点标签(JSON或逗号分隔)' AFTER benefit_tags,
    ADD COLUMN IF NOT EXISTS city VARCHAR(64) NULL COMMENT '城市' AFTER location,
    ADD COLUMN IF NOT EXISTS district VARCHAR(64) NULL COMMENT '区域' AFTER city,
    ADD COLUMN IF NOT EXISTS education_requirement VARCHAR(64) NULL COMMENT '学历要求' AFTER employment_type,
    ADD COLUMN IF NOT EXISTS preferred_major VARCHAR(128) NULL COMMENT '优先专业' AFTER education_requirement,
    ADD COLUMN IF NOT EXISTS min_experience_years INT NULL COMMENT '最低经验年限' AFTER experience_level,
    ADD COLUMN IF NOT EXISTS max_experience_years INT NULL COMMENT '最高经验年限' AFTER min_experience_years,
    ADD COLUMN IF NOT EXISTS internship_months INT NULL COMMENT '实习时长要求(月)' AFTER max_experience_years,
    ADD COLUMN IF NOT EXISTS salary_min_monthly INT NULL COMMENT '月薪下限(k)' AFTER internship_months,
    ADD COLUMN IF NOT EXISTS salary_max_monthly INT NULL COMMENT '月薪上限(k)' AFTER salary_min_monthly,
    ADD COLUMN IF NOT EXISTS salary_months INT NULL COMMENT '年薪月数' AFTER salary_max_monthly;

CREATE INDEX idx_job_post_city ON job_post(city);
CREATE INDEX idx_job_post_education_requirement ON job_post(education_requirement);
CREATE INDEX idx_job_post_experience_level ON job_post(experience_level);
