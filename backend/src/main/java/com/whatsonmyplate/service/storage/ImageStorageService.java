package com.whatsonmyplate.service.storage;

import com.whatsonmyplate.dto.storage.ImageUploadResult;

public interface ImageStorageService {

    /**
     * Uploads image byte data to the storage provider.
     *
     * @param imageBytes The raw binary image payload.
     * @param contentType The MIME content type (e.g. image/jpeg, image/png, image/webp).
     * @param folder The storage folder destination (e.g. whatsonmyplate/recipes).
     * @return Validated ImageUploadResult containing the HTTPS secureUrl and publicId.
     */
    ImageUploadResult uploadImage(byte[] imageBytes, String contentType, String folder);

    /**
     * Deletes an uploaded asset from the storage provider using its public ID.
     *
     * @param publicId The unique public identifier of the asset.
     */
    void deleteImage(String publicId);
}
