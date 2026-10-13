package com.whatsonmyplate.dto.community;

public class LikeResponse {

    private Boolean liked;
    private Long likeCount;

    public LikeResponse() {
    }

    public LikeResponse(Boolean liked, Long likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
}
