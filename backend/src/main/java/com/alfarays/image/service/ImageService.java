package com.alfarays.image.service;

import com.alfarays.image.entity.Image;
import com.alfarays.image.mapper.ImageMapper;
import com.alfarays.image.model.ImageResponse;
import com.alfarays.image.repository.ImageRepository;
import com.alfarays.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    @Value("${file.upload.directory}")
    private String uploadDir;
    private final ImageRepository imageRepository;

    public Image upload(MultipartFile file, User user) {
        try {
            Path filePath = saveFileToDisk(file, user);
            String fileDownloadUri = generateFileUrl(filePath);
            Image image = buildImageEntity(file, user, fileDownloadUri, filePath.getFileName().toString());
            return imageRepository.save(image);
        } catch(IOException e) {
            log.error("Error uploading image: ", e);
            throw new RuntimeException("Error uploading image", e);
        }
    }

    public List<ImageResponse> get(Long userId) {
        return imageRepository.findByUserId(userId)
                .stream()
                .map(ImageMapper::toResponse)
                .collect(Collectors.toList());
    }

    public boolean delete(Image existingImage) throws IOException {
        Path existingFilePath = Paths.get(existingImage.getPath());
        Files.deleteIfExists(existingFilePath);
        imageRepository.delete(existingImage);
        return true;
    }

    public Image update(MultipartFile newProfile, Image existingImage) throws IOException {
        if(newProfile == null || newProfile.isEmpty()) return existingImage;
        Path existingFilePath = Paths.get(existingImage.getPath());
        Files.deleteIfExists(existingFilePath);

        Path newFilePath = saveFileToDisk(newProfile, existingImage.getUser());
        String newFileDownloadUri = generateFileUrl(newFilePath);

        existingImage.setPath(newFileDownloadUri);
        existingImage.setFilename(newFilePath.getFileName().toString());
        existingImage.setType(newProfile.getContentType());
        existingImage.setSize(newProfile.getSize());

        return imageRepository.save(existingImage);
    }


    private Path saveFileToDisk(MultipartFile file, User user) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Ensure upload directory exists
        if(!Files.exists(uploadPath)) {
            log.info("Creating upload directory: {}", uploadPath);
            Files.createDirectories(uploadPath);
        }

        // Directory must be writable
        if(!Files.isWritable(uploadPath)) throw new IOException("Upload directory is not writable: " + uploadPath);

        // Extract file name & extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if(originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            originalFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }

        // Build final file name
        long timestamp = System.currentTimeMillis();
        String finalFileName = String.format("%s_%s_%s%s", user.getId(), originalFilename, timestamp, fileExtension);

        // Resolve final path
        Path filePath = uploadPath.resolve(finalFileName);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File saved at: {}", filePath);
        return filePath;
    }

    private String generateFileUrl(Path filePath) {
        String dateSubdirectory = filePath.getParent().getFileName().toString();
        String fileName = filePath.getFileName().toString();

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(dateSubdirectory + "/")
                .path(fileName)
                .toUriString();
    }

    private Image buildImageEntity(MultipartFile file, User user, String fileDownloadUri, String filename) {
        Image image = new Image();
        image.setPath(fileDownloadUri);
        image.setFilename(filename);
        image.setType(file.getContentType());
        image.setSize(file.getSize());
        image.setUser(user);
        return image;
    }

}
