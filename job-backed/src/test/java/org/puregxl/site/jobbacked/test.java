package org.puregxl.site.jobbacked;

import org.junit.jupiter.api.Test;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class test {

    @Autowired
    public UserResumeFileMapper userResumeFileMapper;

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
}
