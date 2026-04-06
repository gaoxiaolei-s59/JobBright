package org.puregxl.site.jobbacked;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.grpc.internal.JsonUtil;
import org.junit.jupiter.api.Test;
import org.puregxl.site.jobbacked.dao.entity.JobPost;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.mapper.JobPostMapper;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class test {

    @Autowired
    public UserResumeFileMapper userResumeFileMapper;

    @Autowired
    public JobPostMapper jobPostMapper;

    @Test
    void test() {
        UserResumeFile userResumeFile = UserResumeFile.builder().userId(2039240368378200065L).
                resumeId("1c02e038-1feb-4f7e-9f0e-8e4e10280f92").
                fileName("高晓雷-实习.pdf").
                fileExt("pdf").
                contentType("application/pdf").
                fileSize(834566L).
                objectKey("user/resume/1c02e038-1feb-4f7e-9f0e-8e4e10280f98.pdf").
                objectUrl("http://localhost:9000/user-resume/user/resume/1c02e038-1feb-4f7e-9f0e-8e4e10280f98.pdf").
                isCurrent(1).
                score(88d).build();
        userResumeFileMapper.insert(userResumeFile);
        System.out.println(userResumeFile.getId());
    }

    @Test
    void testv2() {
        JobPageRequestV2 jobPageRequestV2 = new JobPageRequestV2();
        jobPageRequestV2.setCurrent(0);
        jobPageRequestV2.setSize(5);

        IPage<JobPost> jobPostIPage = jobPostMapper.selectRecommendJobPageV2(jobPageRequestV2, jobPageRequestV2);

        List<JobPost> records = jobPostIPage.getRecords();

        for (JobPost record : records) {
            System.out.println(JSONUtil.toJsonStr(record));
        }


    }
}
