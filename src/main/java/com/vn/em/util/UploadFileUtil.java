package com.vn.em.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vn.em.exception.UploadFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class UploadFileUtil {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            String resourceType = getResourceType(file);
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type",
                    resourceType));
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new UploadFileException("Upload file failed!");
        }
    }

    public String uploadImage(byte[] bytes) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap("resource_type", "image"));
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new UploadFileException("Upload image failed!");
        }
    }

    public void destroyFileWithUrl(String url) {
        int startIndex = url.lastIndexOf("/") + 1;
        int endIndex = url.lastIndexOf(".");
        String publicId = url.substring(startIndex, endIndex);
        try {
            String resourceType = getResourceTypeFromUrl(url);
            Map<String, String> params = new HashMap<>();
            params.put("resource_type", resourceType);

            Map<?, ?> result = cloudinary.uploader().destroy(publicId, params);
            log.info(String.format("Destroy image public id %s %s", publicId, result.toString()));
        } catch (IOException e) {
            throw new UploadFileException("Remove file failed!");
        }
    }

    private static String getResourceType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return "image";
            } else if (contentType.startsWith("video/")) {
                return "video";
            } else if (contentType.startsWith("audio/")) {
                return "audio";
            } else {
                return "auto";
            }
        } else {
            throw new UploadFileException("Invalid file!");
        }
    }

    private static String getResourceTypeFromUrl(String url) {
        if (url.contains("/video/")) {
            return "video";
        } else if (url.contains("/image/")) {
            return "image";
        } else if (url.contains("/audio/")) {
            return "audio";
        } else if (url.contains("/raw/")) {
            return "raw";
        } else if (url.contains("/3d/")) {
            return "3d";
        } else {
            return "auto";
        }
    }

}
