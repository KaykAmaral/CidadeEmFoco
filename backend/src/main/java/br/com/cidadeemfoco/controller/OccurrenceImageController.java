package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.service.OccurrenceImageService;
import jakarta.validation.constraints.Positive;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/occurrences")
public class OccurrenceImageController {

    private final OccurrenceImageService occurrenceImageService;

    public OccurrenceImageController(OccurrenceImageService occurrenceImageService) {
        this.occurrenceImageService = occurrenceImageService;
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OccurrenceResponse upload(
            Authentication authentication,
            @PathVariable @Positive Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return occurrenceImageService.upload(authentication.getName(), id, file);
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OccurrenceResponse uploadMany(
            Authentication authentication,
            @PathVariable @Positive Long id,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return occurrenceImageService.uploadMany(authentication.getName(), id, files);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> download(@PathVariable @Positive Long id) {
        StoredImage image = occurrenceImageService.load(id);
        String contentDisposition = ContentDisposition.inline()
                .filename(image.filename())
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(image.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(image.resource());
    }

    @GetMapping("/{id}/images/{imageId}")
    public ResponseEntity<Resource> download(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long imageId
    ) {
        StoredImage image = occurrenceImageService.load(id, imageId);
        return ResponseEntity.ok()
                .contentType(image.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(image.filename()).build().toString())
                .body(image.resource());
    }
}
