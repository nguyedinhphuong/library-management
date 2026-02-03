package com.project.library.service;

import com.project.library.common.ImageUpLoadResult;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    /**
     *
     * @param file : file send to client -  form -data
     * @param folder: Folder name in Cloudinary
     * @return ImageUpLoadResult includes information after that uploaded
     */
    ImageUpLoadResult uploadImage(MultipartFile file, String folder);

    /**
     *
     * @param publicId : Cloudinary use public_id that is id.
     */
    void deleteImage(String publicId);

    /**
     *
     * @param file : need validate picture file before upload it
     * @throws com.project.library.exception.BusinessException if file invalid
     */
    void validateImageFile(MultipartFile file);

    /**
     * delete old image , use new image
     * @param file file new image
     * @param oldPublicId : old public_id image
     * @param folder : Folder name in Cloudinary
     * @return  includes image new information
     */
    ImageUpLoadResult updateImage(MultipartFile file, String oldPublicId, String folder);
}
