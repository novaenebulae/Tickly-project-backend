package edu.cda.project.ticklybackend.dtos.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned after a successful file upload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after a successful file upload.")
public class FileUploadResponseDto {

    @Schema(description = "Original file name.", example = "my_logo.png")
    private String fileName;

    @Schema(description = "Public URL to access the uploaded file.", example = "http://localhost/static/structures/logos/uuid-logo.png")
    private String fileUrl;

    @Schema(description = "Confirmation message.", example = "File uploaded successfully.")
    private String message;
}