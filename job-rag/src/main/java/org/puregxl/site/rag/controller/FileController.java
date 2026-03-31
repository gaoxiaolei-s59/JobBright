package org.puregxl.site.rag.controller;

import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @PostMapping("/upload")
    public Result<Void> uploadFile() {
        return Results.success();
    }
}
