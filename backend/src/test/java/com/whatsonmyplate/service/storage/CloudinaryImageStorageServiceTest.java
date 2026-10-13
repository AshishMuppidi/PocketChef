package com.whatsonmyplate.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.whatsonmyplate.dto.storage.ImageUploadResult;
import com.whatsonmyplate.exception.ImageStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryImageStorageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryImageStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new CloudinaryImageStorageService(
                cloudinary,
                "test-cloud-name",
                "test-api-key",
                "test-api-secret"
        );
    }

    @Test
    void testUploadImageSuccess() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> cloudinaryResponse = Map.of(
                "public_id", "whatsonmyplate/recipes/sample-uuid",
                "secure_url", "https://res.cloudinary.com/test-cloud/image/upload/v12345/whatsonmyplate/recipes/sample-uuid.jpg",
                "format", "jpg",
                "bytes", 1024L
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(cloudinaryResponse);

        byte[] sampleBytes = new byte[]{1, 2, 3, 4};
        ImageUploadResult result = storageService.uploadImage(sampleBytes, "image/jpeg", "whatsonmyplate/recipes");

        assertNotNull(result);
        assertEquals("whatsonmyplate/recipes/sample-uuid", result.getPublicId());
        assertEquals("https://res.cloudinary.com/test-cloud/image/upload/v12345/whatsonmyplate/recipes/sample-uuid.jpg", result.getSecureUrl());
        assertEquals("jpg", result.getFormat());
        assertEquals(1024L, result.getBytes());
    }

    @Test
    void testUploadImageMissingCredentialsThrowsException() {
        CloudinaryImageStorageService unconfiguredService = new CloudinaryImageStorageService(
                cloudinary,
                "replace-with-cloud-name",
                "replace-with-api-key",
                "replace-with-api-secret"
        );

        ImageStorageException ex = assertThrows(
                ImageStorageException.class,
                () -> unconfiguredService.uploadImage(new byte[]{1, 2}, "image/jpeg", "recipes")
        );

        assertTrue(ex.getMessage().contains("Cloudinary credentials are not configured"));
    }

    @Test
    void testUploadImageMissingSecureUrlThrowsException() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> invalidResponse = Map.of(
                "public_id", "whatsonmyplate/recipes/sample-uuid",
                "url", "http://insecure.cloudinary.com/test.jpg"
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(invalidResponse);

        ImageStorageException ex = assertThrows(
                ImageStorageException.class,
                () -> storageService.uploadImage(new byte[]{1, 2}, "image/jpeg", "recipes")
        );

        assertTrue(ex.getMessage().contains("valid HTTPS secure URL"));
    }

    @Test
    void testUploadImageSdkExceptionThrowsImageStorageException() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Network error"));

        ImageStorageException ex = assertThrows(
                ImageStorageException.class,
                () -> storageService.uploadImage(new byte[]{1, 2}, "image/jpeg", "recipes")
        );

        assertTrue(ex.getMessage().contains("Cloudinary communication failure"));
    }

    @Test
    void testDeleteImageSuccess() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(String.class), anyMap())).thenReturn(Map.of("result", "ok"));

        storageService.deleteImage("whatsonmyplate/recipes/sample-uuid");

        verify(uploader).destroy(any(String.class), anyMap());
    }
}
