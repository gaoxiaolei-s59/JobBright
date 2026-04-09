package org.puregxl.site.rag.dto.profile;

import lombok.Data;

import java.util.List;

@Data
public class ResumeProfileDTO {

    private String candidate_name;

    private List<String> target_roles;

    private String seniority;

    private String education_level;

    private String school_name;

    private String major_name;

    private List<String> school_tags;

    private List<String> core_skills;

    private List<String> project_tags;

    private List<String> industry_tags;

    private List<String> strengths;

    private List<String> preferred_cities;

    private String preferred_job_type;

    private String internship_months;

    private String salary_expectation;

    private String work_years;

    private String resume_summary;

    private String model;
}
