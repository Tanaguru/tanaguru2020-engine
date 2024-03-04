package com.tanaguru.domain.dto;


import org.springframework.data.domain.Pageable;

import java.util.Map;

public class AuditSynthesisDTO{
    private Map<String, Map<Long, TestHierarchyResultDTO>> content;
    private long totalElements = 0;
    private long totalPages = 0;
    private Pageable pageable;

    public AuditSynthesisDTO(Map<String, Map<Long, TestHierarchyResultDTO>> content, long totalElements, int totalPages, Pageable pageable) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.pageable = pageable;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public Map<String, Map<Long, TestHierarchyResultDTO>> getContent() {
        return content;
    }

    public void setContent(Map<String, Map<Long, TestHierarchyResultDTO>> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }
}
