package com.codebrain.scanner;

public class ScanReport {
    private Integer inserted;
    private Integer updated;
    private Integer deleted;
    private Integer failed;

    public ScanReport() {}

    public Integer getInserted() {
        return inserted;
    }

    public void setInserted(Integer inserted) {
        this.inserted = inserted;
    }

    public Integer getUpdated() {
        return updated;
    }

    public void setUpdated(Integer updated) {
        this.updated = updated;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }
}