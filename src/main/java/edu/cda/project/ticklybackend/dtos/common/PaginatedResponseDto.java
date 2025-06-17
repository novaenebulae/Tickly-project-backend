package edu.cda.project.ticklybackend.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponseDto<T> {
    private List<T> items;
    private long totalItems;
    private int currentPage;
    private int pageSize;
    private int totalPages;

    public PaginatedResponseDto(Page<T> page) {
        this.items = page.getContent();
        this.totalItems = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
    }
}