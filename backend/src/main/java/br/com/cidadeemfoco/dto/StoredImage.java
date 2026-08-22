package br.com.cidadeemfoco.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record StoredImage(
        String filename,
        MediaType mediaType,
        Resource resource
) {
}
