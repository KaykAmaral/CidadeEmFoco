package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.StorageException;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

final class ImageFileValidator {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private ImageFileValidator() {
    }

    static ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("A imagem e obrigatoria");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessRuleException("A imagem deve ter no maximo 5 MB");
        }

        try {
            byte[] content = file.getBytes();
            ImageFormat format = identifyFormat(content);
            return new ValidatedImage(content, format.extension, format.mediaType);
        } catch (IOException exception) {
            throw new StorageException("Nao foi possivel ler a imagem", exception);
        }
    }

    static MediaType mediaTypeFromFormat(String format) {
        return switch (format.toLowerCase(java.util.Locale.ROOT)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> throw new BusinessRuleException("Formato da imagem armazenada nao reconhecido");
        };
    }

    private static ImageFormat identifyFormat(byte[] content) {
        if (content.length >= 3
                && unsigned(content[0]) == 0xFF
                && unsigned(content[1]) == 0xD8
                && unsigned(content[2]) == 0xFF) {
            return ImageFormat.JPEG;
        }
        if (content.length >= 8
                && unsigned(content[0]) == 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47
                && content[4] == 0x0D
                && content[5] == 0x0A
                && content[6] == 0x1A
                && content[7] == 0x0A) {
            return ImageFormat.PNG;
        }
        if (content.length >= 12
                && content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P') {
            return ImageFormat.WEBP;
        }
        throw new BusinessRuleException("Formato de imagem invalido. Use JPEG, PNG ou WebP");
    }

    private static int unsigned(byte value) {
        return Byte.toUnsignedInt(value);
    }

    record ValidatedImage(byte[] content, String extension, MediaType mediaType) {
    }

    private enum ImageFormat {
        JPEG(".jpg", MediaType.IMAGE_JPEG),
        PNG(".png", MediaType.IMAGE_PNG),
        WEBP(".webp", MediaType.parseMediaType("image/webp"));

        private final String extension;
        private final MediaType mediaType;

        ImageFormat(String extension, MediaType mediaType) {
            this.extension = extension;
            this.mediaType = mediaType;
        }
    }
}
