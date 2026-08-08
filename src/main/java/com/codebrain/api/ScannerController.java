package com.codebrain.api;

import com.codebrain.common.Result;
import com.codebrain.scanner.ScanReport;
import com.codebrain.scanner.ScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scanner")
public class ScannerController {
    @Autowired
    private ScannerService scannerService;

    @PostMapping("/repo/{repoId}")
    public Result<ScanReport> scanRepo(@PathVariable Long repoId) {
        ScanReport report = scannerService.scanRepository(repoId);
        return Result.success(report);
    }
}