package com.codebrain.search.controller;

import com.codebrain.common.Result;
import com.codebrain.search.dto.DualRecallResult;
import com.codebrain.search.dto.HybridSearchResponse;
import com.codebrain.search.dto.SearchRequest;
import com.codebrain.search.service.CodeSearchService;
import com.codebrain.search.service.HybridSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code-search")
@RequiredArgsConstructor
public class CodeSearchController {

    private final CodeSearchService codeSearchService;
    private final HybridSearchService hybridSearchService;

    @PostMapping("/dual-recall")
    public Result<DualRecallResult> dualRecall(@Valid @RequestBody SearchRequest request) {
        DualRecallResult result = codeSearchService.dualRecall(request);
        return Result.success(result);
    }

    @PostMapping("/code")
    public Result<HybridSearchResponse> hybridSearch(@Valid @RequestBody SearchRequest request) {
        HybridSearchResponse resp = hybridSearchService.search(request);
        return Result.success(resp);
    }


}