package com.odeyalo.analog.netflix.service.image;

import com.odeyalo.analog.netflix.entity.Image;
import com.odeyalo.analog.netflix.entity.ImageStorageType;
import com.odeyalo.analog.netflix.exceptions.UploadException;
import com.odeyalo.analog.netflix.repository.ImageRepository;
import com.odeyalo.analog.netflix.service.storage.LocalFileUploader;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class LocalImageSaverService implements ImageSaverService {
    private final LocalFileUploader fileUploader;
    private final ImageRepository imageRepository;
    private final Logger logger = LoggerFactory.getLogger(LocalImageSaverService.class);

    @Autowired
    public LocalImageSaverService(LocalFileUploader fileUploader, ImageRepository imageRepository) {
        this.fileUploader = fileUploader;
        this.imageRepository = imageRepository;
    }

    @Override
    public Image saveImage(MultipartFile file) throws UploadException, IOException {
        String path = null;
        try {
            path = this.fileUploader.save(file);
            String extension = FilenameUtils.getExtension(path);
            Image imageToSave = Image.builder().path(path).fileCreated(toUnixTimestamp()).storageType(ImageStorageType.LOCAL)
                    .type(extension).size(file.getSize()).build();
            Image image = this.imageRepository.save(imageToSave);
            this.logger.info("Saved image: {}", image);
            return image;
        } catch (Exception ex) {
            this.logger.error("File upload: {} has been failed.", file.getOriginalFilename(), ex);
            Files.deleteIfExists(Paths.get(path));
            throw new UploadException(String.format("Cannot upload file %s, please try again later", file.getName()));
        }
    }


    protected Long toUnixTimestamp() {
        return System.currentTimeMillis() / 1000;
    }
}
