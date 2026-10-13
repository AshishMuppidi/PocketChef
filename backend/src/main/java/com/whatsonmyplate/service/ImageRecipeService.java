package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.dto.storage.ImageUploadResult;
import com.whatsonmyplate.service.ai.AiRecipeService;
import com.whatsonmyplate.service.storage.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
public class ImageRecipeService {

    private static final Logger logger = LoggerFactory.getLogger(ImageRecipeService.class);

    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    public static final int MAX_PROMPT_NOTES_LENGTH = 1000;
    public static final String CLOUDINARY_RECIPE_FOLDER = "whatsonmyplate/recipes";

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ImageStorageService imageStorageService;
    private final AiRecipeService aiRecipeService;
    private final RecipePersistenceService recipePersistenceService;

    public ImageRecipeService(
            ImageStorageService imageStorageService,
            AiRecipeService aiRecipeService,
            RecipePersistenceService recipePersistenceService
    ) {
        this.imageStorageService = imageStorageService;
        this.aiRecipeService = aiRecipeService;
        this.recipePersistenceService = recipePersistenceService;
    }

    /**
     * Orchestrates image validation, Cloudinary upload, external Gemini multimodal analysis,
     * fail-safe orphan cleanup, and atomic recipe persistence via Spring transactional proxy.
     *
     * @param image The uploaded food photo.
     * @param promptNotes Optional additional culinary context or dietary notes.
     * @return Created RecipeResponse with HTTP 201 semantics.
     */
    public RecipeResponse analyzeDishImage(MultipartFile image, String promptNotes) {
        byte[] imageBytes = validateUploadAndExtractBytes(image, promptNotes);
        String mimeType = image.getContentType();

        // Step 1: Upload image to Cloudinary (outside DB transaction)
        ImageUploadResult uploadResult = imageStorageService.uploadImage(imageBytes, mimeType, CLOUDINARY_RECIPE_FOLDER);
        String publicId = uploadResult.getPublicId();

        try {
            logger.info("Executing Gemini multimodal analysis (size: {} bytes, mime: [{}])", imageBytes.length, mimeType);

            // Step 2: Execute AI analysis outside of any database transaction
            AiRecipeResult aiResult = aiRecipeService.generateRecipeFromImage(imageBytes, mimeType, promptNotes);

            // Step 3: Persist validated recipe with Cloudinary secureUrl in an isolated DB transaction via Spring proxy
            return recipePersistenceService.saveAiRecipe(aiResult, uploadResult.getSecureUrl());
        } catch (Exception e) {
            // Fail-safe cleanup: If Gemini fails, AI validation fails, or DB persistence fails, delete uploaded Cloudinary asset
            logger.warn("Pipeline failure after Cloudinary upload. Triggering asset cleanup for public ID [{}]", publicId);
            imageStorageService.deleteImage(publicId);
            throw e;
        }
    }

    private byte[] validateUploadAndExtractBytes(MultipartFile image, String promptNotes) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Uploaded image file is required and cannot be empty.");
        }

        if (image.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Uploaded file exceeds the maximum allowed size of 10MB.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported image type: '" + contentType + "'. Allowed types are: image/jpeg, image/png, image/webp."
            );
        }

        if (promptNotes != null && promptNotes.length() > MAX_PROMPT_NOTES_LENGTH) {
            throw new IllegalArgumentException(
                    "Prompt notes cannot exceed " + MAX_PROMPT_NOTES_LENGTH + " characters."
            );
        }

        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read uploaded image data: " + e.getMessage(), e);
        }

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Uploaded image file is required and cannot be empty.");
        }

        if (!hasValidImageHeader(bytes, contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Uploaded file does not match the signature of the declared image type (" + contentType + ")."
            );
        }

        return bytes;
    }

    /**
     * Lightweight magic byte validation for JPEG, PNG, and WebP signatures without heavy external dependencies.
     */
    private boolean hasValidImageHeader(byte[] bytes, String declaredMimeType) {
        if (bytes.length < 12) {
            if ("image/jpeg".equals(declaredMimeType) && bytes.length >= 3) {
                return (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF);
            }
            if ("image/png".equals(declaredMimeType) && bytes.length >= 4) {
                return (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47);
            }
            if ("image/webp".equals(declaredMimeType) && bytes.length >= 4) {
                return (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F');
            }
            return bytes.length > 0;
        }

        if ("image/jpeg".equals(declaredMimeType)) {
            return (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF);
        } else if ("image/png".equals(declaredMimeType)) {
            return (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47 &&
                    bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A);
        } else if ("image/webp".equals(declaredMimeType)) {
            return (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                    bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P');
        }

        return false;
    }
}
