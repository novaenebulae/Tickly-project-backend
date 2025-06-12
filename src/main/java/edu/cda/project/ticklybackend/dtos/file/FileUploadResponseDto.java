package edu.cda.project.ticklybackend.dtos.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponseDto {
    private String fileName;
    private String fileUrl;
    private String message;
}