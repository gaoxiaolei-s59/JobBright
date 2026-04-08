package org.puregxl.site.clawler.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.puregxl.site.clawler.entity.Company;
import org.puregxl.site.clawler.mapper.CompanyMapper;
import org.puregxl.site.clawler.util.BusinessIdGenerator;
import org.puregxl.site.clawler.util.TextCleaner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CompanyPersistenceService {

    private final CompanyMapper companyMapper;

    public CompanyPersistenceService(CompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
    }

    @Transactional
    public Company saveOrUpdate(
            String sourceSite,
            String sourceCompanyId,
            String companyName,
            String companyLogoUrl,
            String companySize,
            String industryName,
            String companyStage,
            String companyIntro,
            String rawJson
    ) {
        String cleanedSourceSite = TextCleaner.clean(sourceSite);
        String cleanedCompanyName = TextCleaner.clean(companyName);
        if (TextCleaner.isBlank(cleanedSourceSite) || TextCleaner.isBlank(cleanedCompanyName)) {
            return null;
        }

        String cleanedSourceCompanyId = TextCleaner.clean(sourceCompanyId);
        String companyId = BusinessIdGenerator.generateCompanyId(cleanedSourceSite, cleanedSourceCompanyId, cleanedCompanyName);
        Company company = companyMapper.selectOne(Wrappers.lambdaQuery(Company.class)
                .eq(Company::getCompanyId, companyId)
                .last("limit 1"));
        if (company == null) {
            company = new Company();
        }

        company.setCompanyId(companyId);
        company.setCompanyName(cleanedCompanyName);
        company.setCompanyLogo(TextCleaner.clean(companyLogoUrl));
        company.setCompanySize(TextCleaner.clean(companySize));
        company.setIndustryName(TextCleaner.clean(industryName));
        company.setCompanyStage(TextCleaner.clean(companyStage));
        company.setCompanyIntro(TextCleaner.clean(companyIntro));
        if (company.getDelFlag() == null) {
            company.setDelFlag(0);
        }
        LocalDateTime now = LocalDateTime.now();
        if (company.getId() == null) {
            company.setCreateTime(now);
            company.setUpdateTime(now);
            companyMapper.insert(company);
        } else {
            company.setUpdateTime(now);
            companyMapper.updateById(company);
        }
        return company;
    }
}
