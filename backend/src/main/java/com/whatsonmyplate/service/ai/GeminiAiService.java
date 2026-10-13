package com.whatsonmyplate.service.ai;

import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionResult;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentResult;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiAiService implements AiRecipeService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAiService.class);

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final AiResponseValidator responseValidator;
    private final String apiKey;
    private final String modelName;
    private final BeanOutputConverter<AiRecipeResult> outputConverter;
    private final BeanOutputConverter<IngredientSubstitutionResult> substitutionConverter;
    private final BeanOutputConverter<SpiceAdjustmentResult> spiceConverter;

    public GeminiAiService(
            ObjectProvider<ChatModel> chatModelProvider,
            AiResponseValidator responseValidator,
            @Value("${spring.ai.openai.api-key:${GEMINI_API_KEY:}}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model:${GEMINI_MODEL:gemini-3.6-flash}}")String modelName
    ) {
        this.chatModelProvider = chatModelProvider;
        this.responseValidator = responseValidator;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.outputConverter = new BeanOutputConverter<>(AiRecipeResult.class);
        this.substitutionConverter = new BeanOutputConverter<>(IngredientSubstitutionResult.class);
        this.spiceConverter = new BeanOutputConverter<>(SpiceAdjustmentResult.class);
    }

    @Override
    public AiRecipeResult generateRecipeFromText(String promptText) {
        ensureConfigured();

        if (promptText == null || promptText.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt text cannot be blank.");
        }

        logger.info("Starting text recipe generation with model [{}]", modelName);
        long startTime = System.currentTimeMillis();

        try {
            SystemMessage systemMessage =
                    new SystemMessage(GeminiAiPrompts.RECIPE_ANALYSIS_SYSTEM_PROMPT);

            String userPromptWithFormat =
                    promptText + "\n\n" + outputConverter.getFormat();

            UserMessage userMessage =
                    new UserMessage(userPromptWithFormat);

            Prompt prompt =
                    new Prompt(List.of(systemMessage, userMessage));

            ChatModel chatModel = getChatModel();
            ChatResponse response = chatModel.call(prompt);

            if (response == null
                    || response.getResult() == null
                    || response.getResult().getOutput() == null) {

                throw new AiServiceException(
                        "Gemini returned an empty chat response."
                );
            }

            String responseContent =
                    response.getResult().getOutput().getText();

            AiRecipeResult result =
                    outputConverter.convert(responseContent);

            responseValidator.validate(result);

            long duration =
                    System.currentTimeMillis() - startTime;

            logger.info(
                    "Completed text recipe generation for [{}] in {}ms",
                    result.getDishName(),
                    duration
            );

            return result;

        } catch (AiServiceException e) {
            throw e;

        } catch (Exception e) {
            logger.error(
                    "AI text recipe generation failed: {}",
                    e.getMessage()
            );

            throw new AiServiceException(
                    "Failed to generate recipe from AI: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public AiRecipeResult generateRecipeFromImage(
            byte[] imageBytes,
            String mimeType,
            String additionalPrompt
    ) {
        ensureConfigured();

        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Image data cannot be empty."
            );
        }

        String safeMime =
                (mimeType != null && !mimeType.trim().isEmpty())
                        ? mimeType
                        : "image/jpeg";

        logger.info(
                "Starting multimodal image recipe generation (MIME: [{}], Size: {} bytes) with model [{}]",
                safeMime,
                imageBytes.length,
                modelName
        );

        long startTime = System.currentTimeMillis();

        try {
            SystemMessage systemMessage =
                    new SystemMessage(
                            GeminiAiPrompts.RECIPE_ANALYSIS_SYSTEM_PROMPT
                    );

            String userText =
                    ((additionalPrompt != null
                            && !additionalPrompt.trim().isEmpty())
                            ? additionalPrompt
                            : "Analyze this dish image and provide the complete recipe.")
                            + "\n\n"
                            + outputConverter.getFormat();

            Media media =
                    new Media(
                            MimeType.valueOf(safeMime),
                            new ByteArrayResource(imageBytes)
                    );

            // Spring AI 1.0 multimodal UserMessage
            UserMessage userMessage =
                    UserMessage.builder()
                            .text(userText)
                            .media(media)
                            .build();

            Prompt prompt =
                    new Prompt(List.of(systemMessage, userMessage));

            ChatModel chatModel = getChatModel();
            ChatResponse response = chatModel.call(prompt);

            if (response == null
                    || response.getResult() == null
                    || response.getResult().getOutput() == null) {

                throw new AiServiceException(
                        "Gemini returned an empty multimodal response."
                );
            }

            String responseContent =
                    response.getResult().getOutput().getText();

            AiRecipeResult result =
                    outputConverter.convert(responseContent);

            responseValidator.validate(result);

            long duration =
                    System.currentTimeMillis() - startTime;

            logger.info(
                    "Completed multimodal recipe generation for [{}] in {}ms",
                    result.getDishName(),
                    duration
            );

            return result;

        } catch (AiServiceException e) {
            throw e;

        } catch (Exception e) {
            logger.error(
                    "AI multimodal recipe generation failed: {}",
                    e.getMessage()
            );

            throw new AiServiceException(
                    "Failed to analyze image with AI: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public IngredientSubstitutionResult generateIngredientSubstitution(
            Recipe recipe,
            String ingredientName,
            String reason
    ) {
        ensureConfigured();

        if (recipe == null) {
            throw new IllegalArgumentException(
                    "Recipe cannot be null."
            );
        }

        if (ingredientName == null
                || ingredientName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Ingredient name cannot be blank."
            );
        }

        logger.info(
                "Starting ingredient substitution for [{}] in recipe [{}]",
                ingredientName,
                recipe.getTitle()
        );

        long startTime = System.currentTimeMillis();

        try {
            SystemMessage systemMessage =
                    new SystemMessage(
                            GeminiAiPrompts.SUBSTITUTION_SYSTEM_PROMPT
                    );

            String ingredientsList =
                    (recipe.getIngredients() != null)
                            ? recipe.getIngredients()
                            .stream()
                            .map(i ->
                                 "- "
                                 + (i.getQuantity() != null
                                    ? i.getQuantity() + " "
                                    : "")
                                 + (i.getUnit() != null
                                    ? i.getUnit() + " "
                                    : "")
                                 + i.getName()
                            )
                            .collect(Collectors.joining("\n"))
                            : "";

            String userText = String.format("""
                    Recipe: %s
                    Description: %s
                    Ingredients:
                    %s

                    Target ingredient to replace: %s
                    Reason/Preference: %s

                    Provide the best culinary substitute with quantity ratio and instructions.

                    %s
                    """,
                    recipe.getTitle(),
                    recipe.getDescription() != null
                            ? recipe.getDescription()
                            : "",
                    ingredientsList,
                    ingredientName.trim(),
                    (reason != null && !reason.isBlank())
                            ? reason.trim()
                            : "None provided",
                    substitutionConverter.getFormat()
            );

            UserMessage userMessage =
                    new UserMessage(userText);

            Prompt prompt =
                    new Prompt(List.of(systemMessage, userMessage));

            ChatModel chatModel = getChatModel();
            ChatResponse response = chatModel.call(prompt);

            if (response == null
                    || response.getResult() == null
                    || response.getResult().getOutput() == null) {

                throw new AiServiceException(
                        "Gemini returned an empty substitution response."
                );
            }

            String responseContent =
                    response.getResult().getOutput().getText();

            IngredientSubstitutionResult result =
                    substitutionConverter.convert(responseContent);

            responseValidator.validateSubstitution(
                    result,
                    ingredientName
            );

            long duration =
                    System.currentTimeMillis() - startTime;

            logger.info(
                    "Completed ingredient substitution for [{}] in {}ms",
                    ingredientName,
                    duration
            );

            return result;

        } catch (AiServiceException e) {
            throw e;

        } catch (Exception e) {
            logger.error(
                    "AI ingredient substitution failed: {}",
                    e.getMessage()
            );

            throw new AiServiceException(
                    "Failed to generate ingredient substitution: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public SpiceAdjustmentResult generateSpiceAdjustment(
            Recipe recipe,
            SpiceLevel targetSpiceLevel
    ) {
        ensureConfigured();

        if (recipe == null) {
            throw new IllegalArgumentException(
                    "Recipe cannot be null."
            );
        }

        if (targetSpiceLevel == null) {
            throw new IllegalArgumentException(
                    "Target spice level cannot be null."
            );
        }

        logger.info(
                "Starting spice adjustment to [{}] for recipe [{}]",
                targetSpiceLevel,
                recipe.getTitle()
        );

        long startTime = System.currentTimeMillis();

        try {
            SystemMessage systemMessage =
                    new SystemMessage(
                            GeminiAiPrompts.SPICE_ADJUSTMENT_SYSTEM_PROMPT
                    );

            String ingredientsList =
                    (recipe.getIngredients() != null)
                            ? recipe.getIngredients()
                            .stream()
                            .map(i ->
                                 "- "
                                 + (i.getQuantity() != null
                                    ? i.getQuantity() + " "
                                    : "")
                                 + (i.getUnit() != null
                                    ? i.getUnit() + " "
                                    : "")
                                 + i.getName()
                            )
                            .collect(Collectors.joining("\n"))
                            : "";

            String userText = String.format("""
                    Recipe: %s
                    Description: %s
                    Original Spice Level: %s
                    Target Spice Level: %s

                    Ingredients:
                    %s

                    Adjust only the heat/spice contributing ingredients to match the target spice level.

                    %s
                    """,
                    recipe.getTitle(),
                    recipe.getDescription() != null
                            ? recipe.getDescription()
                            : "",
                    recipe.getSpiceLevel() != null
                            ? recipe.getSpiceLevel().name()
                            : "MEDIUM",
                    targetSpiceLevel.name(),
                    ingredientsList,
                    spiceConverter.getFormat()
            );

            UserMessage userMessage =
                    new UserMessage(userText);

            Prompt prompt =
                    new Prompt(List.of(systemMessage, userMessage));

            ChatModel chatModel = getChatModel();
            ChatResponse response = chatModel.call(prompt);

            if (response == null
                    || response.getResult() == null
                    || response.getResult().getOutput() == null) {

                throw new AiServiceException(
                        "Gemini returned an empty spice adjustment response."
                );
            }

            String responseContent =
                    response.getResult().getOutput().getText();

            SpiceAdjustmentResult result =
                    spiceConverter.convert(responseContent);

            if (result != null) {
                result.setRecipeId(recipe.getId());

                result.setOriginalSpiceLevel(
                        recipe.getSpiceLevel() != null
                                ? recipe.getSpiceLevel().name()
                                : "MEDIUM"
                );

                result.setTargetSpiceLevel(
                        targetSpiceLevel.name()
                );
            }

            responseValidator.validateSpiceAdjustment(
                    result,
                    recipe.getIngredients()
            );

            long duration =
                    System.currentTimeMillis() - startTime;

            logger.info(
                    "Completed spice adjustment to [{}] in {}ms",
                    targetSpiceLevel,
                    duration
            );

            return result;

        } catch (AiServiceException e) {
            throw e;

        } catch (Exception e) {
            logger.error(
                    "AI spice adjustment failed: {}",
                    e.getMessage()
            );

            throw new AiServiceException(
                    "Failed to generate spice adjustment: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private void ensureConfigured() {
        if (apiKey == null
                || apiKey.trim().isEmpty()
                || apiKey.contains("replace-with")) {

            throw new AiServiceException(
                    "Google Gemini API key is not configured. "
                            + "Please set the GEMINI_API_KEY environment variable."
            );
        }
    }

    private ChatModel getChatModel() {
        ChatModel chatModel =
                chatModelProvider.getIfAvailable();

        if (chatModel == null) {
            throw new AiServiceException(
                    "Spring AI ChatModel is not available. "
                            + "Please verify your GEMINI_API_KEY "
                            + "and Spring AI configuration."
            );
        }

        return chatModel;
    }
}