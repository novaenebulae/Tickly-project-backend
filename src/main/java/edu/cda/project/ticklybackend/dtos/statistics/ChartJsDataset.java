package edu.cda.project.ticklybackend.dtos.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a dataset for Chart.js visualizations.
 * Each chart can have multiple datasets, each with its own data and styling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dataset for Chart.js visualizations")
public class ChartJsDataset {
    
    @Schema(description = "Label for the dataset")
    private String label;
    
    @Schema(description = "The actual data points")
    private List<Number> data;
    
    @Schema(description = "Background colors for elements (for bar/doughnut charts)")
    private List<String> backgroundColor;
    
    @Schema(description = "Border color for elements (for line charts)")
    private String borderColor;
    
    @Schema(description = "Whether to fill the area under a line (for line charts)")
    private boolean fill;
}