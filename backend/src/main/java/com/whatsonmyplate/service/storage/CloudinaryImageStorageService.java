package com.whatsonmyplate.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.whatsonmyplate.dto.storage.ImageUploadResult;
import com.whatsonmyplate.exception.ImageStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryImageStorageService.class);

    private final Cloudinary cloudinary;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryImageStorageService(
            Cloudinary cloudinary,
            @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}") String cloudName,
            @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}") String apiKey,
            @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}") String apiSecret
    ) {
        this.cloudinary = cloudinary;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public ImageUploadResult uploadImage(byte[] imageBytes, String contentType, String folder) {
        ensureConfigured();

        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image byte payload cannot be empty.");
        }

        String safePublicId = UUID.randomUUID().toString();
        String targetFolder = (folder != null && !folder.trim().isEmpty()) ? folder.trim() : "whatsonmyplate/recipes";

        logger.info("Uploading image (size: {} bytes, mime: [{}]) to Cloudinary folder [{}] with public ID [{}]",
                imageBytes.length, contentType, targetFolder, safePublicId);

        try {
            Map<?, ?> options = ObjectUtils.asMap(
                    "folder", targetFolder,
                    "public_id", safePublicId,
                    "resource_type", "image",
                    "overwrite", false
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(imageBytes, options);

            if (uploadResult == null) {
                throw new ImageStorageException("Cloudinary returned an empty upload response.");
            }

            String secureUrl = (String) uploadResult.get("secure_url");
            String returnedPublicId = (String) uploadResult.get("public_id");
            String format = (String) uploadResult.get("format");
            Number bytesNumber = (Number) uploadResult.get("bytes");
            Long bytes = bytesNumber != null ? bytesNumber.longValue() : (long) imageBytes.length;

            if (secureUrl == null || !secureUrl.startsWith("https://")) {
                throw new ImageStorageException("Cloudinary upload failed: Response did not contain a valid HTTPS secure URL.");
            }

            logger.info("Successfully uploaded image to Cloudinary [Public ID: {}]", returnedPublicId != null ? returnedPublicId : safePublicId);

            return new ImageUploadResult(
                    returnedPublicId != null ? returnedPublicId : targetFolder + "/" + safePublicId,
                    secureUrl,
                    format,
                    bytes
            );
        } catch (ImageStorageException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Cloudinary I/O error during upload: {}", e.getMessage());
            throw new ImageStorageException("Cloudinary communication failure during upload: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Cloudinary upload failed: {}", e.getMessage());
            throw new ImageStorageException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return;
        }

        if (!isConfigured()) {
            logger.warn("Skipping Cloudinary deletion for public ID [{}] because Cloudinary is not configured", publicId);
            return;
        }

        logger.info("Attempting Cloudinary asset cleanup for public ID [{}]", publicId);

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Cloudinary destroy completed for [{}]: {}", publicId, result);
        } catch (Exception e) {
            logger.warn("Failed to delete Cloudinary image [{}] during cleanup: {}", publicId, e.getMessage());
        }
    }

    private boolean isConfigured() {
        return cloudName != null && !cloudName.trim().isEmpty() && !cloudName.contains("replace-with")
                && apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("replace-with")
                && apiSecret != null && !apiSecret.trim().isEmpty() && !apiSecret.contains("replace-with");
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new ImageStorageException(
                    "Cloudinary credentials are not configured. Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET."
            );
        }
    }
}
