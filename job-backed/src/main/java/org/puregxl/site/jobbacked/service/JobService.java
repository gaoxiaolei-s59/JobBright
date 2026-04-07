package org.puregxl.site.jobbacked.service;

import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.puregxl.site.jobbacked.dto.resp.AppliedJobResponse;
import org.puregxl.site.jobbacked.dto.resp.FavoritesJobResponse;

public interface JobService {
    FavoritesJobResponse getFavoritesJob(JobPageRequestV2 requestParam);

    void favoritesJob(String jobId);

    void cancelFavoritesJob(String jobId);

    void applyJob(String jobId);

    void cancelApplyJob(String jobId);

    AppliedJobResponse getAppliedJob(JobPageRequestV2 requestParam);
}
