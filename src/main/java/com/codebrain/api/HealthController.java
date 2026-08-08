package com.codebrain.api;

import com.codebrain.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, String>> healthCheck() {
        return Result.success(Map.of("status", "UP"));
    }
}