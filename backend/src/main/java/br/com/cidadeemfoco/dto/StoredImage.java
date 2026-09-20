package br.com.cidadeemfoco.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record StoredImage(
        String storageKey,
        String filename,
        MediaType mediaType,
        Resource resource
) {

    public StoredImage(String filename, MediaType mediaType, Resource resource) {
        this(filename, filename, mediaType, resource);
    }
}
