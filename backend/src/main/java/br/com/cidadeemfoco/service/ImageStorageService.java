package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.StoredImage;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    StoredImage store(MultipartFile file);

    StoredImage load(String storageKey);

    void deleteQuietly(String storageKey);
}
