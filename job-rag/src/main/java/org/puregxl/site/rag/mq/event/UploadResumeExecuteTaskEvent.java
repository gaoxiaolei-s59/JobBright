package org.puregxl.site.rag.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResumeExecuteTaskEvent {


    /**
     * 文件地址
     */
    private String fileAddress;


    /**
     * 文件id
     */
    private String fileId;
}
