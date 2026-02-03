package com.project.library.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.project.library.common.ImageUpLoadResult;
import com.project.library.exception.BusinessException;
import com.project.library.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; //
    private static final int COVER_WIDTH = 600;
    private static final int COVER_HEIGHT = 900;

    @Override
    public ImageUpLoadResult uploadImage(MultipartFile file, String folder) {
        try {
            validateImageFile(file);
            String publicId = generatePublicId(folder);
            // build
            Map<String, Object> uploadParams = buildUploadParams(publicId, folder);
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            ImageUpLoadResult result = parseUploadResult(uploadResult);

            log.info("Upload successful - URL: {}, publicId: {}, size: {}x{}",
                    result.getUrl(), result.getPublicId(), result.getWidth(), result.getHeight());
            return result;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary - file: {}",
                    file.getOriginalFilename(), e);
            throw new BusinessException("Failed to upload image. Please try again.");
        }

    }


    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.error("Failed to delete image: {}", publicId, e);
        }
    }

    @Override
    public void validateImageFile(MultipartFile file) {
        //Check null
        if (file == null || file.isEmpty()) throw new BusinessException("Image file is required");
        //check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Invalid file format. Only JPEG, PNG, JPG, WEBP and images are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(String.format("File size maximum limit of %d MB", MAX_FILE_SIZE / (1024 * 1024)));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("Invalid file name");
        }
        log.debug("Image validation passed - file: {}, type: {}, size: {} bytes", originalFilename, contentType, file.getSize());
    }

    @Override
    public ImageUpLoadResult updateImage(MultipartFile file, String oldPublicId, String folder) {
        return null;
    }

    private String generatePublicId(String folder) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return folder + "/" + timestamp + "_" + randomId;
    }

    private Map<String, Object> buildUploadParams(String publicId, String folder) {
        return ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "overwrite", true,
                "invalidate", true, // xóa cache
//                "transformation", ObjectUtils.asMap(
//                        "width", COVER_WIDTH,
//                        "height", COVER_HEIGHT,
//                        "crop", "limit",
//                        "quality", "auto:good",
//                        "fetch_format", "auto",
//                        "effect", "sharpen:80"),
                "transformation", new Transformation<>()
                        .width(COVER_WIDTH)
                        .height(COVER_HEIGHT)
                        .crop("limit")
                        .quality("auto:good")
                        .fetchFormat("auto")
                        .effect("sharpen:80"),
                // metadata
                "context", ObjectUtils.asMap(
                        "type", "book_cover",
                        "uploaded_at", new Date().toString()
                ),
                "tags", Arrays.asList("book_cover", "library")
        );
    }

    private ImageUpLoadResult parseUploadResult(Map uploadResult) {

        return ImageUpLoadResult.builder()
                .url((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .size(((Number) uploadResult.get("bytes")).longValue())
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .format((String) uploadResult.get("format"))
                .build();
    }
}
