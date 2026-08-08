package com.codebrain.scanner;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DiffResult {
    private List<String> newFileList = new ArrayList<>();
    private List<String> changedFileList = new ArrayList<>();
    private List<Long> deletedFileIdList = new ArrayList<>();
}