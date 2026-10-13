package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.AiIngredient;
import com.whatsonmyplate.dto.ai.AiInstruction;
import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.recipe.IngredientItemDto;
import com.whatsonmyplate.dto.recipe.InstructionStepDto;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.dto.storage.ImageUploadResult;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.exception.AiServiceException;
import com.whatsonmyplate.exception.ImageStorageException;
import com.whatsonmyplate.service.ai.AiRecipeService;
import com.whatsonmyplate.service.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageRecipeServiceTest {

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private AiRecipeService aiRecipeService;

    @Mock
    private RecipePersistenceService recipePersistenceService;

    private ImageRecipeService imageRecipeService;

    private static final byte[] VALID_JPEG_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] VALID_PNG_BYTES = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] VALID_WEBP_BYTES = new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    @BeforeEach
    void setUp() {
        imageRecipeService = new ImageRecipeService(
                imageStorageService,
                aiRecipeService,
                recipePersistenceService
        );
    }

    @Test
    void testAnalyzeDishImageSuccessJpeg() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "food.jpg",
                "image/jpeg",
                VALID_JPEG_BYTES
        );

        ImageUploadResult uploadResult = new ImageUploadResult(
                "whatsonmyplate/recipes/shakshuka-uuid",
                "https://res.cloudinary.com/demo/image/upload/v1/whatsonmyplate/recipes/shakshuka-uuid.jpg",
                "jpg",
                1024L
        );

        AiRecipeResult aiResult = new AiRecipeResult(
                "Shakshuka",
                "Poached eggs in spiced tomato sauce",
                List.of(new AiIngredient("Eggs", 3.0, "large")),
                List.of(new AiInstruction(1, "Simmer sauce and poach eggs")),
                10,
                20,
                2,
                SpiceLevel.MEDIUM,
                0.94
        );

        RecipeResponse persistenceResponse = new RecipeResponse(
                101L,
                "Shakshuka",
                "Poached eggs in spiced tomato sauce",
                "https://res.cloudinary.com/demo/image/upload/v1/whatsonmyplate/recipes/shakshuka-uuid.jpg",
                RecipeSource.AI_GENERATED,
                null,
                true,
                10,
                20,
                2,
                SpiceLevel.MEDIUM,
                List.of(new IngredientItemDto("Eggs", 3.0, "large")),
                List.of(new InstructionStepDto(1, "Simmer sauce and poach eggs")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(imageStorageService.uploadImage(any(byte[].class), any(String.class), any(String.class)))
                .thenReturn(uploadResult);
        when(aiRecipeService.generateRecipeFromImage(any(byte[].class), any(String.class), any()))
                .thenReturn(aiResult);
        when(recipePersistenceService.saveAiRecipe(any(AiRecipeResult.class), eq(uploadResult.getSecureUrl())))
                .thenReturn(persistenceResponse);

        RecipeResponse response = imageRecipeService.analyzeDishImage(file, "No cilantro please");

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Shakshuka", response.getTitle());
        assertEquals(RecipeSource.AI_GENERATED, response.getSource());
        assertTrue(response.getIsAiGenerated());
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/whatsonmyplate/recipes/shakshuka-uuid.jpg", response.getImageUrl());

        verify(imageStorageService).uploadImage(any(byte[].class), eq("image/jpeg"), eq(ImageRecipeService.CLOUDINARY_RECIPE_FOLDER));
        verify(aiRecipeService).generateRecipeFromImage(any(byte[].class), eq("image/jpeg"), eq("No cilantro please"));
        verify(recipePersistenceService).saveAiRecipe(aiResult, uploadResult.getSecureUrl());
        verify(imageStorageService, never()).deleteImage(any());
    }

    @Test
    void testAnalyzeDishImageCloudinaryFailureDoesNotCallGemini() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "food.jpg",
                "image/jpeg",
                VALID_JPEG_BYTES
        );

        when(imageStorageService.uploadImage(any(byte[].class), any(String.class), any(String.class)))
                .thenThrow(new ImageStorageException("Cloudinary upload timed out"));

        ImageStorageException ex = assertThrows(
                ImageStorageException.class,
                () -> imageRecipeService.analyzeDishImage(file, null)
        );

        assertEquals("Cloudinary upload timed out", ex.getMessage());

        verify(aiRecipeService, never()).generateRecipeFromImage(any(), any(), any());
        verify(recipePersistenceService, never()).saveAiRecipe(any(), any());
    }

    @Test
    void testAnalyzeDishImageGeminiFailureCleansUpCloudinaryAsset() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "food.jpg",
                "image/jpeg",
                VALID_JPEG_BYTES
        );

        ImageUploadResult uploadResult = new ImageUploadResult(
                "whatsonmyplate/recipes/orphan-uuid",
                "https://res.cloudinary.com/demo/image/upload/v1/whatsonmyplate/recipes/orphan-uuid.jpg",
                "jpg",
                1024L
        );

        when(imageStorageService.uploadImage(any(byte[].class), any(String.class), any(String.class)))
                .thenReturn(uploadResult);
        when(aiRecipeService.generateRecipeFromImage(any(byte[].class), any(String.class), any()))
                .thenThrow(new AiServiceException("Gemini service unavailable"));

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> imageRecipeService.analyzeDishImage(file, null)
        );

        assertEquals("Gemini service unavailable", ex.getMessage());

        // Verify cleanup was triggered for the uploaded asset
        verify(imageStorageService).deleteImage("whatsonmyplate/recipes/orphan-uuid");
        verify(recipePersistenceService, never()).saveAiRecipe(any(), any());
    }

    @Test
    void testAnalyzeDishImageDbPersistenceFailureCleansUpCloudinaryAsset() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "curry.png",
                "image/png",
                VALID_PNG_BYTES
        );

        ImageUploadResult uploadResult = new ImageUploadResult(
                "whatsonmyplate/recipes/db-fail-uuid",
                "https://res.cloudinary.com/demo/image/upload/v1/whatsonmyplate/recipes/db-fail-uuid.png",
                "png",
                2048L
        );

        AiRecipeResult aiResult = new AiRecipeResult(
                "Curry",
                "Curry description",
                List.of(new AiIngredient("Spice", 1.0, "tsp")),
                List.of(new AiInstruction(1, "Cook")),
                10,
                20,
                2,
                SpiceLevel.MEDIUM,
                0.9
        );

        when(imageStorageService.uploadImage(any(byte[].class), any(String.class), any(String.class)))
                .thenReturn(uploadResult);
        when(aiRecipeService.generateRecipeFromImage(any(byte[].class), any(String.class), any()))
                .thenReturn(aiResult);
        when(recipePersistenceService.saveAiRecipe(any(AiRecipeResult.class), any(String.class)))
                .thenThrow(new DataIntegrityViolationException("DB error"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> imageRecipeService.analyzeDishImage(file, null)
        );

        // Verify cleanup was triggered for the uploaded asset
        verify(imageStorageService).deleteImage("whatsonmyplate/recipes/db-fail-uuid");
    }

    @Test
    void testAnalyzeDishImageMismatchedHeaderThrowsException() {
        MockMultipartFile fakeFile = new MockMultipartFile(
                "image",
                "fake.jpg",
                "image/jpeg",
                "This is not a real JPEG image".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageRecipeService.analyzeDishImage(fakeFile, null)
        );
        assertTrue(ex.getMessage().contains("does not match the signature"));

        verify(imageStorageService, never()).uploadImage(any(), any(), any());
        verify(aiRecipeService, never()).generateRecipeFromImage(any(), any(), any());
        verify(recipePersistenceService, never()).saveAiRecipe(any(), any());
    }

    @Test
    void testAnalyzeDishImageOversizedFileThrowsException() {
        byte[] oversizedBytes = new byte[(int) (ImageRecipeService.MAX_FILE_SIZE_BYTES + 1024)];
        oversizedBytes[0] = (byte) 0xFF;
        oversizedBytes[1] = (byte) 0xD8;
        oversizedBytes[2] = (byte) 0xFF;

        MockMultipartFile oversizedFile = new MockMultipartFile(
                "image",
                "huge.jpg",
                "image/jpeg",
                oversizedBytes
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageRecipeService.analyzeDishImage(oversizedFile, null)
        );
        assertEquals("Uploaded file exceeds the maximum allowed size of 10MB.", ex.getMessage());

        verify(imageStorageService, never()).uploadImage(any(), any(), any());
        verify(aiRecipeService, never()).generateRecipeFromImage(any(), any(), any());
        verify(recipePersistenceService, never()).saveAiRecipe(any(), any());
    }

    @Test
    void testAnalyzeDishImageNullOrEmptyThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageRecipeService.analyzeDishImage(emptyFile, null)
        );
        assertEquals("Uploaded image file is required and cannot be empty.", ex.getMessage());

        verify(imageStorageService, never()).uploadImage(any(), any(), any());
        verify(aiRecipeService, never()).generateRecipeFromImage(any(), any(), any());
        verify(recipePersistenceService, never()).saveAiRecipe(any(), any());
    }

    @Test
    void testAnalyzeDishImageUnsupportedMimeTypeThrowsException() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "image",
                "menu.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageRecipeService.analyzeDishImage(pdfFile, null)
        );
        assertTrue(ex.getMessage().contains("Unsupported image type"));

        verify(imageStorageService, never()).uploadImage(any(), any(), any());
        verify(aiRecipeService, never()).generateRecipeFromImage(any(), any(), any());
        verify(recipePersistenceService, never()).saveAiRecipe(any(), any());
    }

    @Test
    void testAnalyzeDishImagePromptNotesExceedsLimitThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "dish.jpg",
                "image/jpeg",
                VALID_JPEG_BYTES
        );

        String longPrompt = "A".repeat(1001);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> imageRecipeService.analyzeDishImage(file, longPrompt)
        );
        assertEquals("Prompt notes cannot exceed 1000 characters.", ex.getMessage());

        verify(imageStorageService, never()).uploadImage(any(), any(), any());
        verify(aiRecipeService, never()).generateRecipeFromImage(any(), any(), any());
        verify(recipePersistenceService, never()).saveAiRecipe(any(), any());
    }
}
