package org.puregxl.site.jobbacked.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendJobListResponse {

    private Integer total;

    private Boolean hasMore;

    private List<RecommendJobResponse> records;
}
