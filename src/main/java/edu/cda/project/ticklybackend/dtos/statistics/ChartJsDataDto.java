package edu.cda.project.ticklybackend.dtos.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A generic DTO for chart data compatible with Chart.js library.
 * This DTO can be used for various chart types like doughnut, line, bar, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chart.js compatible data structure for visualizations")
public class ChartJsDataDto {
    
    @Schema(description = "The type of chart (e.g., 'doughnut', 'line', 'bar')")
    private String chartType;
    
    @Schema(description = "Labels for the chart data points")
    private List<String> labels;
    
    @Schema(description = "Datasets containing the actual data and styling information")
    private List<ChartJsDataset> datasets;
}