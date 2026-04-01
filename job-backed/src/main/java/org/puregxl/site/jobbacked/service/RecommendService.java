package org.puregxl.site.jobbacked.service;

import org.puregxl.site.jobbacked.dto.req.JobPageRequest;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.puregxl.site.jobbacked.dto.resp.RecommendJobListResponse;

public interface RecommendService {

    RecommendJobListResponse getRecommendJobs(JobPageRequest requestParam);

    RecommendJobListResponse getRecommendJobsV2(JobPageRequestV2 requestParam);
}
