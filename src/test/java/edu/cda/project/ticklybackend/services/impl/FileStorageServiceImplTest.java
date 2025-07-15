package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.exceptions.FileStorageException;
import edu.cda.project.ticklybackend.services.files.FileStorageProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceImplTest {

    private FileStorageServiceImpl fileStorageService;
    private FileStorageProperties fileStorageProperties;
    
    @TempDir
    Path tempDir;
    
    private final String STATIC_BASE_URL = "http://localhost/static/";
    private final String TEST_SUBDIRECTORY = "test-subdir";
    private final String TEST_FILENAME = "test-file.txt";
    private final String TEST_CONTENT = "Test file content";

    @BeforeEach
    void setUp() {
        // Create a mock FileStorageProperties
        fileStorageProperties = mock(FileStorageProperties.class);
        when(fileStorageProperties.getUploadDir()).thenReturn(tempDir.toString());
        when(fileStorageProperties.getStaticBaseUrl()).thenReturn(STATIC_BASE_URL);
        
        // Create the FileStorageServiceImpl instance
        fileStorageService = new FileStorageServiceImpl(fileStorageProperties);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up any files created during tests
        Files.walk(tempDir)
                .filter(path -> !path.equals(tempDir))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
    }

    @Test
    void storeFile_WithValidFile_ShouldStoreAndReturnFilename() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "text/plain",
                TEST_CONTENT.getBytes()
        );

        // Act
        String storedFilename = fileStorageService.storeFile(file, TEST_SUBDIRECTORY);

        // Assert
        assertNotNull(storedFilename);
        assertTrue(storedFilename.endsWith(".txt"));
        
        // Verify the file was stored
        Path storedFilePath = tempDir.resolve(TEST_SUBDIRECTORY).resolve(storedFilename);
        assertTrue(Files.exists(storedFilePath));
    }

    @Test
    void storeFile_WithEmptyFile_ShouldThrowException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "text/plain",
                new byte[0]
        );

        // Act & Assert
        assertThrows(FileStorageException.class, () -> {
            fileStorageService.storeFile(emptyFile, TEST_SUBDIRECTORY);
        });
    }

    @Test
    void storeFile_WithInvalidFilename_ShouldThrowException() {
        // Arrange
        MultipartFile fileWithoutExtension = new MockMultipartFile(
                "file",
                "invalid-filename-without-extension",
                "text/plain",
                TEST_CONTENT.getBytes()
        );

        // Act & Assert
        assertThrows(FileStorageException.class, () -> {
            fileStorageService.storeFile(fileWithoutExtension, TEST_SUBDIRECTORY);
        });
    }

    @Test
    void loadFile_WithExistingFile_ShouldReturnPath() throws IOException {
        // Arrange
        // First store a file
        MultipartFile file = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "text/plain",
                TEST_CONTENT.getBytes()
        );
        String storedFilename = fileStorageService.storeFile(file, TEST_SUBDIRECTORY);

        // Act
        Path loadedPath = fileStorageService.loadFile(storedFilename, TEST_SUBDIRECTORY);

        // Assert
        assertNotNull(loadedPath);
        assertTrue(Files.exists(loadedPath));
        assertEquals(TEST_CONTENT, Files.readString(loadedPath));
    }

    @Test
    void loadFileAsResource_WithExistingFile_ShouldReturnResource() throws IOException {
        // Arrange
        // First store a file
        MultipartFile file = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "text/plain",
                TEST_CONTENT.getBytes()
        );
        String storedFilename = fileStorageService.storeFile(file, TEST_SUBDIRECTORY);

        // Act
        Resource resource = fileStorageService.loadFileAsResource(storedFilename, TEST_SUBDIRECTORY);

        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void loadFileAsResource_WithNonExistingFile_ShouldThrowException() {
        // Act & Assert
        assertThrows(FileStorageException.class, () -> {
            fileStorageService.loadFileAsResource("non-existing-file.txt", TEST_SUBDIRECTORY);
        });
    }

    @Test
    void deleteFile_WithExistingFile_ShouldDeleteFile() throws IOException {
        // Arrange
        // First store a file
        MultipartFile file = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "text/plain",
                TEST_CONTENT.getBytes()
        );
        String storedFilename = fileStorageService.storeFile(file, TEST_SUBDIRECTORY);
        Path storedFilePath = tempDir.resolve(TEST_SUBDIRECTORY).resolve(storedFilename);
        
        // Verify file exists before deletion
        assertTrue(Files.exists(storedFilePath));

        // Act
        fileStorageService.deleteFile(storedFilename, TEST_SUBDIRECTORY);

        // Assert
        assertFalse(Files.exists(storedFilePath));
    }

    @Test
    void deleteFile_WithNonExistingFile_ShouldNotThrowException() {
        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("non-existing-file.txt", TEST_SUBDIRECTORY);
        });
    }

    @Test
    void deleteFile_WithNullFilename_ShouldNotThrowException() {
        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile(null, TEST_SUBDIRECTORY);
        });
    }

    @Test
    void getFileUrl_WithValidFilename_ShouldReturnCorrectUrl() {
        // Act
        String url = fileStorageService.getFileUrl(TEST_FILENAME, TEST_SUBDIRECTORY);

        // Assert
        assertEquals(STATIC_BASE_URL + TEST_SUBDIRECTORY + "/" + TEST_FILENAME, url);
    }

    @Test
    void getFileUrl_WithNullFilename_ShouldReturnNull() {
        // Act
        String url = fileStorageService.getFileUrl(null, TEST_SUBDIRECTORY);

        // Assert
        assertNull(url);
    }

    @Test
    void getFileUrl_WithEmptyFilename_ShouldReturnNull() {
        // Act
        String url = fileStorageService.getFileUrl("", TEST_SUBDIRECTORY);

        // Assert
        assertNull(url);
    }
}