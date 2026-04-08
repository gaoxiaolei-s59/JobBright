package org.puregxl.site.rag.service;

import org.puregxl.site.rag.dao.entity.JobPost;

public interface JobPostCleanPersistenceService {

    void persistAndDelete(JobPost jobPost, Long rawJobId);
}
