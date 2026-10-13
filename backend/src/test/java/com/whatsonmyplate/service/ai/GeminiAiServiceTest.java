package com.whatsonmyplate.service.ai;

import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiAiServiceTest {

    @Mock
    private ObjectProvider<ChatModel> chatModelProvider;

    @Mock
    private ChatModel chatModel;

    private AiResponseValidator responseValidator;
    private GeminiAiService aiService;

    private static final String VALID_RECIPE_JSON = """
            {
              "dishName": "Paneer Butter Masala",
              "description": "Rich Indian paneer curry in a spiced tomato-butter gravy.",
              "ingredients": [
                {
                  "name": "Paneer",
                  "quantity": 250.0,
                  "unit": "g"
                },
                {
                  "name": "Butter",
                  "quantity": 2.0,
                  "unit": "tbsp"
                }
              ],
              "instructions": [
                {
                  "step": 1,
                  "text": "Melt butter in a pan and saute onions with ginger-garlic paste."
                },
                {
                  "step": 2,
                  "text": "Add tomato puree and simmer with paneer cubes."
                }
              ],
              "prepTimeMinutes": 10,
              "cookTimeMinutes": 20,
              "servings": 3,
              "spiceLevel": "MEDIUM",
              "confidence": 0.92
            }
            """;

    @BeforeEach
    void setUp() {
        responseValidator = new AiResponseValidator();
        aiService = new GeminiAiService(
                chatModelProvider,
                responseValidator,
                "valid-test-gemini-api-key-12345",
                "gemini-2.5-flash"
        );
    }

    @Test
    void testGenerateRecipeFromTextSuccess() {
        when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);

        Generation generation = new Generation(new AssistantMessage(VALID_RECIPE_JSON));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        AiRecipeResult result = aiService.generateRecipeFromText("How to make paneer butter masala?");

        assertNotNull(result);
        assertEquals("Paneer Butter Masala", result.getDishName());
        assertEquals(SpiceLevel.MEDIUM, result.getSpiceLevel());
        assertEquals(2, result.getIngredients().size());
        assertEquals(2, result.getInstructions().size());
        assertEquals(0.92, result.getConfidence());
    }

    @Test
    void testGenerateRecipeFromImageSuccess() {
        when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);

        Generation generation = new Generation(new AssistantMessage(VALID_RECIPE_JSON));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        byte[] sampleImageBytes = new byte[]{1, 2, 3, 4, 5};
        AiRecipeResult result = aiService.generateRecipeFromImage(sampleImageBytes, "image/jpeg", "Identify dish");

        assertNotNull(result);
        assertEquals("Paneer Butter Masala", result.getDishName());
        assertEquals(2, result.getIngredients().size());
    }

    @Test
    void testMissingApiKeyThrowsException() {
        GeminiAiService unconfiguredService = new GeminiAiService(
                chatModelProvider,
                responseValidator,
                "replace-with-your-gemini-api-key",
                "gemini-2.5-flash"
        );

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> unconfiguredService.generateRecipeFromText("Some prompt")
        );

        assertEquals(
                "Google Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable.",
                ex.getMessage()
        );
    }

    @Test
    void testBlankPromptThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> aiService.generateRecipeFromText("   ")
        );
    }

    @Test
    void testEmptyImageDataThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> aiService.generateRecipeFromImage(new byte[0], "image/jpeg", null)
        );
    }

    @Test
    void testMalformedAiResponseThrowsException() {
        when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);

        Generation generation = new Generation(new AssistantMessage("Invalid non-json response from model"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        assertThrows(
                AiServiceException.class,
                () -> aiService.generateRecipeFromText("Make pasta")
        );
    }
}
