package com.whatsonmyplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsonmyplate.dto.community.CommentCreateRequest;
import com.whatsonmyplate.dto.community.CommentResponse;
import com.whatsonmyplate.dto.community.CommunityPostPageResponse;
import com.whatsonmyplate.dto.community.CommunityPostResponse;
import com.whatsonmyplate.dto.community.LikeResponse;
import com.whatsonmyplate.dto.community.RatingRequest;
import com.whatsonmyplate.dto.community.RatingResponse;
import com.whatsonmyplate.service.CommunityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityService communityService;

    @Test
    void testGetCommunityPostsPublic() throws Exception {
        CommunityPostResponse post = new CommunityPostResponse(
                1L, 1L, "chefmaria", "Chef Maria", null, 10L, "Tacos",
                "https://cloudinary.com/post.jpg", "Great remake!",
                LocalDateTime.now(), LocalDateTime.now(), 5L, 2L, 4.5, 2L, false, null
        );

        CommunityPostPageResponse pageRes = new CommunityPostPageResponse(
                List.of(post), 0, 12, 1L, 1, true
        );

        when(communityService.getCommunityPosts(0, 12, null)).thenReturn(pageRes);

        mockMvc.perform(get("/api/community/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].username").value("chefmaria"));
    }

    @Test
    void testCreateCommunityPostUnauthenticatedReturns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "dish.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/community/posts")
                .file(file)
                .param("caption", "My dish"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"USER"})
    void testCreateCommunityPostAuthenticatedSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "dish.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        CommunityPostResponse postRes = new CommunityPostResponse(
                1L, 1L, "chefmaria", "Chef Maria", null, 10L, "Tacos",
                "https://cloudinary.com/post.jpg", "My dish",
                LocalDateTime.now(), LocalDateTime.now(), 0L, 0L, 5.0, 1L, false, 5
        );

        when(communityService.createCommunityPost(eq("maria@example.com"), eq(10L), eq("My dish"), eq(5), any()))
                .thenReturn(postRes);

        mockMvc.perform(multipart("/api/community/posts")
                .file(file)
                .param("recipeId", "10")
                .param("caption", "My dish")
                .param("rating", "5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.caption").value("My dish"));
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"USER"})
    void testLikePostSuccess() throws Exception {
        LikeResponse res = new LikeResponse(true, 10L);
        when(communityService.likePost("maria@example.com", 1L)).thenReturn(res);

        mockMvc.perform(post("/api/community/posts/1/like")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(10L));
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"USER"})
    void testCommentCreationSuccess() throws Exception {
        CommentCreateRequest req = new CommentCreateRequest("Loved this!");
        CommentResponse res = new CommentResponse(
                5L, 1L, "chefmaria", "Chef Maria", null, "Loved this!",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(communityService.createComment(eq("maria@example.com"), eq(1L), any(CommentCreateRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/community/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.content").value("Loved this!"));
    }

    @Test
    @WithMockUser(username = "maria@example.com", roles = {"USER"})
    void testRatePostSuccess() throws Exception {
        RatingRequest req = new RatingRequest(5, "Awesome");
        RatingResponse res = new RatingResponse(4.8, 10L, 5);

        when(communityService.ratePost(eq("maria@example.com"), eq(1L), any(RatingRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/community/posts/1/rating")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.8))
                .andExpect(jsonPath("$.currentUserRating").value(5));
    }
}
