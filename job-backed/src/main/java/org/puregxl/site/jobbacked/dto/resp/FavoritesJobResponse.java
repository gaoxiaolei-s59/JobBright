package org.puregxl.site.jobbacked.dto.resp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoritesJobResponse {
    private Integer total;

    private Boolean hasMore;

    private List<RecommendJobResponse> records;
}
