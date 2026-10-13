package com.whatsonmyplate.dto.storage;

public class ImageUploadResult {

    private final String publicId;
    private final String secureUrl;
    private final String format;
    private final Long bytes;

    public ImageUploadResult(String publicId, String secureUrl, String format, Long bytes) {
        this.publicId = publicId;
        this.secureUrl = secureUrl;
        this.format = format;
        this.bytes = bytes;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public String getFormat() {
        return format;
    }

    public Long getBytes() {
        return bytes;
    }
}
