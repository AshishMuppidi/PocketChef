package com.whatsonmyplate.controller;

import com.whatsonmyplate.dto.community.CommentCreateRequest;
import com.whatsonmyplate.dto.community.CommentResponse;
import com.whatsonmyplate.dto.community.CommunityPostPageResponse;
import com.whatsonmyplate.dto.community.CommunityPostResponse;
import com.whatsonmyplate.dto.community.CommunityPostUpdateRequest;
import com.whatsonmyplate.dto.community.LikeResponse;
import com.whatsonmyplate.dto.community.RatingRequest;
import com.whatsonmyplate.dto.community.RatingResponse;
import com.whatsonmyplate.service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ResponseEntity<CommunityPostPageResponse> getCommunityPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        CommunityPostPageResponse response = communityService.getCommunityPosts(page, size, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<CommunityPostResponse> getCommunityPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        CommunityPostResponse response = communityService.getCommunityPostById(id, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommunityPostResponse> createCommunityPost(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "recipeId", required = false) Long recipeId,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "rating", required = false) Integer rating,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommunityPostResponse response = communityService.createCommunityPost(
                userDetails.getUsername(),
                recipeId,
                caption,
                rating,
                image
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<CommunityPostResponse> updateCommunityPost(
            @PathVariable Long id,
            @Valid @RequestBody CommunityPostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommunityPostResponse response = communityService.updateCommunityPost(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deleteCommunityPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        communityService.deleteCommunityPost(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<LikeResponse> likePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LikeResponse response = communityService.likePost(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<LikeResponse> unlikePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LikeResponse response = communityService.unlikePost(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        List<CommentResponse> comments = communityService.getComments(id);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse response = communityService.createComment(userDetails.getUsername(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        communityService.deleteComment(userDetails.getUsername(), commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{id}/rating")
    public ResponseEntity<RatingResponse> ratePost(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RatingResponse response = communityService.ratePost(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{id}/rating")
    public ResponseEntity<RatingResponse> getPostRating(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        RatingResponse response = communityService.getPostRating(id, username);
        return ResponseEntity.ok(response);
    }
}
