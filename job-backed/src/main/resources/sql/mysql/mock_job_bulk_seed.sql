-- MySQL 8.0+
-- 用法：
-- 1. 先执行本文件，批量生成测试公司和 1000 条测试职位
-- 2. 如需重复生成，可先执行清理语句或修改前缀

DELETE FROM job_post
WHERE job_id LIKE 'MOCKJOB%';

DELETE FROM company
WHERE company_id LIKE 'MOCKCOM%';

INSERT INTO company (
    company_id,
    company_name,
    company_logo,
    industry_name,
    company_stage,
    company_size,
    company_intro,
    create_time,
    update_time,
    del_flag
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT
    CONCAT('MOCKCOM', LPAD(n, 4, '0')) AS company_id,
    CONCAT(
        ELT(((n - 1) % 8) + 1, '星帆', '木舟', '极川', '海岚', '曜石', '澄明', '远岸', '青禾'),
        ELT(((n - 1) % 6) + 1, '科技', '数据', '网络', '软件', '云服', '智能')
    ) AS company_name,
    CONCAT('https://demo.example.com/logo/mock-', n, '.png') AS company_logo,
    ELT(((n - 1) % 8) + 1, '互联网', '云计算', '企业服务', '大数据', '金融科技', '教育科技', '医疗信息化', 'AI应用') AS industry_name,
    ELT(((n - 1) % 6) + 1, '天使轮', 'A轮', 'B轮', 'C轮', '成熟期', '已上市') AS company_stage,
    ELT(((n - 1) % 6) + 1, '20-99人', '100-499人', '500-999人', '1000-9999人', '10000人以上', '50-99人') AS company_size,
    CONCAT('这是第 ', n, ' 家用于推荐、搜索和画像匹配联调的测试公司。') AS company_intro,
    NOW(),
    NOW(),
    0
FROM seq;

INSERT INTO job_post (
    job_id,
    company_id,
    title,
    job_summary,
    job_description,
    role_category,
    role_tags,
    skill_tags,
    industry_tags,
    benefit_tags,
    highlight_tags,
    location,
    city,
    district,
    work_mode,
    employment_type,
    education_requirement,
    preferred_major,
    experience_level,
    min_experience_years,
    max_experience_years,
    internship_months,
    salary_min_monthly,
    salary_max_monthly,
    salary_months,
    applicant_count,
    apply_url,
    posted_at,
    status,
    country,
    create_time,
    update_time,
    del_flag
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    CONCAT('MOCKJOB', LPAD(n, 5, '0')) AS job_id,
    CONCAT('MOCKCOM', LPAD(((n - 1) % 50) + 1, 4, '0')) AS company_id,
    CONCAT(
        ELT(((n - 1) % 10) + 1,
            'Java 后端开发工程师',
            '高级 Java 平台工程师',
            'Java 开发实习生',
            '校招 Java 工程师',
            '中级后端开发工程师',
            '推荐系统后端工程师',
            '画像平台 Java 工程师',
            '分布式中间件开发工程师',
            '搜索平台后端工程师',
            '支付平台 Java 工程师'
        ),
        CASE
            WHEN MOD(n, 7) = 0 THEN '(急招)'
            WHEN MOD(n, 11) = 0 THEN '(重点)'
            ELSE ''
        END
    ) AS title,
    CONCAT(
        '负责 ',
        ELT(((n - 1) % 8) + 1,
            'Spring Boot 微服务',
            '用户画像服务',
            '职位推荐链路',
            '搜索与召回模块',
            '支付与账务服务',
            '数据中台接口',
            '风控平台后端',
            '招聘平台核心服务'
        ),
        ' 开发，要求熟悉 ',
        ELT(((n - 1) % 6) + 1,
            'Java、Spring Boot、MySQL',
            'Java、Redis、MQ',
            'Java、MySQL、Elasticsearch',
            'Java、Spring Cloud、Redis',
            'Java、Kafka、MySQL',
            'Java、SQL、缓存优化'
        ),
        '。'
    ) AS job_summary,
    CONCAT(
        '这个岗位面向 ',
        ELT(((n - 1) % 7) + 1, '后端开发', '平台研发', '推荐系统', '画像平台', '支付服务', '搜索平台', '数据平台'),
        ' 方向候选人，强调 ',
        ELT(((n - 1) % 6) + 1, 'Java 基础', '工程能力', '业务理解', '分布式经验', '画像标签理解', '推荐链路经验'),
        '。'
    ) AS job_description,
    ELT(((n - 1) % 7) + 1, '后端开发', '平台研发', '推荐系统', '画像平台', '支付平台', '搜索平台', '数据平台') AS role_category,
    ELT(((n - 1) % 7) + 1,
        '["Java后端","微服务"]',
        '["平台研发","分布式"]',
        '["推荐系统","召回排序"]',
        '["画像平台","标签体系"]',
        '["支付系统","高可用"]',
        '["搜索平台","检索服务"]',
        '["数据平台","实时计算"]'
    ) AS role_tags,
    ELT(((n - 1) % 7) + 1,
        '["Java","Spring Boot","MySQL"]',
        '["Java","Redis","MQ"]',
        '["Java","推荐系统","Redis"]',
        '["Java","画像","MySQL"]',
        '["Java","SQL","MQ"]',
        '["Java","Elasticsearch","MySQL"]',
        '["Java","Flink","Redis"]'
    ) AS skill_tags,
    CONCAT('["', ELT(((n - 1) % 8) + 1, '互联网', '云计算', '企业服务', '大数据', '金融科技', '教育科技', '医疗信息化', 'AI应用'), '"]') AS industry_tags,
    ELT(((n - 1) % 5) + 1,
        '["双休","年度体检"]',
        '["弹性办公","下午茶"]',
        '["绩效奖金","年度旅游"]',
        '["免费三餐","节日礼包"]',
        '["补充医疗","远程补贴"]'
    ) AS benefit_tags,
    ELT(((n - 1) % 6) + 1,
        '["画像匹配","核心项目"]',
        '["校招友好","成长快"]',
        '["平台型业务","高并发"]',
        '["推荐联动","画像服务"]',
        '["业务核心","技术驱动"]',
        '["远程友好","效率优先"]'
    ) AS highlight_tags,
    ELT(((n - 1) % 10) + 1, '上海', '杭州', '北京', '深圳', '广州', '苏州', '南京', '成都', '武汉', '西安') AS location,
    ELT(((n - 1) % 10) + 1, '上海', '杭州', '北京', '深圳', '广州', '苏州', '南京', '成都', '武汉', '西安') AS city,
    ELT(((n - 1) % 8) + 1, '浦东新区', '西湖区', '海淀区', '南山区', '天河区', '工业园区', '建邺区', '高新区') AS district,
    ELT(((n - 1) % 4) + 1, '现场办公', '混合办公', '远程办公', '混合办公') AS work_mode,
    ELT(((n - 1) % 4) + 1, '全职', '全职', '实习', '校招') AS employment_type,
    ELT(((n - 1) % 4) + 1, '大专及以上', '本科', '本科及以上', '硕士及以上') AS education_requirement,
    ELT(((n - 1) % 5) + 1, '计算机相关专业', '软件工程', '数据科学', '信息安全', '数学/统计') AS preferred_major,
    ELT(((n - 1) % 6) + 1, 'STUDENT', 'NEW_GRAD', 'JUNIOR', '1-3年', '3-5年', '5-10年') AS experience_level,
    ELT(((n - 1) % 6) + 1, 0, 0, 1, 1, 3, 5) AS min_experience_years,
    ELT(((n - 1) % 6) + 1, 0, 1, 3, 3, 5, 10) AS max_experience_years,
    CASE
        WHEN ELT(((n - 1) % 4) + 1, '全职', '全职', '实习', '校招') = '实习' THEN 3 + MOD(n, 4)
        ELSE NULL
    END AS internship_months,
    8 + MOD(n * 3, 28) AS salary_min_monthly,
    15 + MOD(n * 5, 35) AS salary_max_monthly,
    ELT(((n - 1) % 4) + 1, 13, 14, NULL, 15) AS salary_months,
    5 + MOD(n * 7, 120) AS applicant_count,
    CONCAT('https://demo.example.com/jobs/MOCKJOB', LPAD(n, 5, '0')) AS apply_url,
    DATE_SUB(NOW(), INTERVAL MOD(n * 3, 720) HOUR) AS posted_at,
    'ONLINE' AS status,
    '中国大陆' AS country,
    NOW(),
    NOW(),
    0
FROM seq;

-- 可选检查
SELECT COUNT(*) AS company_count
FROM company
WHERE company_id LIKE 'MOCKCOM%';

SELECT COUNT(*) AS job_count
FROM job_post
WHERE job_id LIKE 'MOCKJOB%';
