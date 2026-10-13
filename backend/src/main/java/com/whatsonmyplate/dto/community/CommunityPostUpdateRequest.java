package com.whatsonmyplate.dto.community;

import jakarta.validation.constraints.Size;

public class CommunityPostUpdateRequest {

    @Size(max = 2000, message = "Caption must not exceed 2000 characters")
    private String caption;

    public CommunityPostUpdateRequest() {
    }

    public CommunityPostUpdateRequest(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
