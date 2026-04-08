package org.puregxl.site.clawler.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.puregxl.site.clawler.dto.ZhaopinFetchedJob;
import org.puregxl.site.clawler.dto.ZhaopinJobRawTagItem;
import org.puregxl.site.clawler.entity.ZhaopinJobRaw;
import org.puregxl.site.clawler.entity.ZhaopinJobRawTag;
import org.puregxl.site.clawler.mapper.ZhaopinJobRawMapper;
import org.puregxl.site.clawler.mapper.ZhaopinJobRawTagMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ZhaopinRawPersistenceService {

    private static final DateTimeFormatter PUBLISH_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ZhaopinJobRawMapper zhaopinJobRawMapper;
    private final ZhaopinJobRawTagMapper zhaopinJobRawTagMapper;

    public ZhaopinRawPersistenceService(
            ZhaopinJobRawMapper zhaopinJobRawMapper,
            ZhaopinJobRawTagMapper zhaopinJobRawTagMapper
    ) {
        this.zhaopinJobRawMapper = zhaopinJobRawMapper;
        this.zhaopinJobRawTagMapper = zhaopinJobRawTagMapper;
    }

    public void saveOrUpdate(ZhaopinFetchedJob fetchedJob) {
        ZhaopinJobRaw raw = zhaopinJobRawMapper.selectOne(Wrappers.lambdaQuery(ZhaopinJobRaw.class)
                .eq(ZhaopinJobRaw::getSourceKey, fetchedJob.posting().getSourceKey())
                .last("limit 1"));
        if (raw == null) {
            raw = new ZhaopinJobRaw();
        }

        raw.setSourceSite("zhaopin");
        raw.setSourceKey(fetchedJob.posting().getSourceKey());
        raw.setJobId(fetchedJob.jobId());
        raw.setPositionNumber(fetchedJob.positionNumber());
        raw.setTitle(fetchedJob.posting().getTitle());
        raw.setCompanyName(fetchedJob.posting().getCompany());
        raw.setCompanyId(fetchedJob.companyId());
        raw.setCityName(fetchedJob.cityName());
        raw.setCityDistrict(fetchedJob.cityDistrict());
        raw.setSalaryText(fetchedJob.salaryText());
        raw.setWorkExperience(fetchedJob.workExperience());
        raw.setEducation(fetchedJob.education());
        raw.setCompanySize(fetchedJob.companySize());
        raw.setCompanyProperty(fetchedJob.companyProperty());
        raw.setIndustryName(fetchedJob.industryName());
        raw.setJobSummary(fetchedJob.posting().getSummary());
        raw.setSourceUrl(fetchedJob.posting().getSourceUrl());
        raw.setPublishTime(parsePublishTime(fetchedJob.publishTimeText()));
        raw.setCrawledAt(fetchedJob.posting().getCrawledAt());
        raw.setRawJson(fetchedJob.rawJson());

        LocalDateTime now = LocalDateTime.now();
        if (raw.getCrawledAt() == null) {
            raw.setCrawledAt(now);
        }
        if (raw.getId() == null) {
            raw.setCreatedAt(now);
            raw.setUpdatedAt(now);
            zhaopinJobRawMapper.insert(raw);
        } else {
            raw.setUpdatedAt(now);
            zhaopinJobRawMapper.updateById(raw);
        }
        replaceTags(fetchedJob.posting().getSourceKey(), fetchedJob.tags());
    }

    private void replaceTags(String sourceKey, List<ZhaopinJobRawTagItem> tags) {
        zhaopinJobRawTagMapper.delete(Wrappers.lambdaQuery(ZhaopinJobRawTag.class)
                .eq(ZhaopinJobRawTag::getSourceKey, sourceKey));
        if (tags == null || tags.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (ZhaopinJobRawTagItem tag : tags) {
            ZhaopinJobRawTag entity = new ZhaopinJobRawTag();
            entity.setSourceKey(sourceKey);
            entity.setTagType(tag.tagType());
            entity.setTagName(tag.tagName());
            entity.setCreatedAt(now);
            zhaopinJobRawTagMapper.insert(entity);
        }
    }

    private LocalDateTime parsePublishTime(String publishTime) {
        if (publishTime == null || publishTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(publishTime, PUBLISH_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
