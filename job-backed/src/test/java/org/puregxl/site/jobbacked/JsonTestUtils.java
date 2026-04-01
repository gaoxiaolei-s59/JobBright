package org.puregxl.site.jobbacked;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.puregxl.site.jobbacked.dao.entity.JobPost;
import org.puregxl.site.jobbacked.dao.mapper.JobPostMapper;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@SpringBootTest
public final class JsonTestUtils {

    @Autowired
    public JobPostMapper jobPostMapper;

    @Test
    void test() {



    }


    private String formatPostedAt(Date postedAt) {
        if (postedAt == null) {
            return "";
        }
        Duration duration = Duration.between(postedAt.toInstant(), Instant.now());
        long minutes = Math.max(duration.toMinutes(), 0);
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }
        long days = duration.toDays();
        if (days < 7) {
            return days + " 天前";
        }
        return postedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }


    private boolean matchesDatePosted(String postedAt, String datePosted) {
        if (!StringUtils.hasText(datePosted)) {
            return true;
        }
        if (!StringUtils.hasText(postedAt)) {
            return false;
        }
        if ("24h".equals(datePosted)) {
            return postedAt.contains("小时前") || postedAt.contains("分钟前") || postedAt.contains("刚刚");
        }
        if ("3d".equals(datePosted)) {
            return !postedAt.contains("天前") || postedAt.startsWith("1 天前") || postedAt.startsWith("2 天前");
        }
        if ("7d".equals(datePosted)) {
            return true;
        }
        return true;
    }

}
