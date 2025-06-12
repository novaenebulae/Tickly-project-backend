package edu.cda.project.ticklybackend.files;

import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Service
public interface FileStorageService {
    String storeFile(MultipartFile file, String subDirectory);

    Path loadFile(String filename, String subDirectory);

    Resource loadFileAsResource(String filename, String subDirectory);

    void deleteFile(String filename, String subDirectory) throws FileStorageException;

    String getFileUrl(String filename, String subDirectory);
}