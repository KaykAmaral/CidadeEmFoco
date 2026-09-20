package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.exception.StorageException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
public class CloudinaryClient {

    private final Cloudinary cloudinary;

    public CloudinaryClient(
            @Value("${app.storage.cloudinary.cloud-name}") String cloudName,
            @Value("${app.storage.cloudinary.api-key}") String apiKey,
            @Value("${app.storage.cloudinary.api-secret}") String apiSecret
    ) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Preencha CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY e CLOUDINARY_API_SECRET"
            );
        }
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public UploadResult upload(byte[] content, String publicId, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "resource_type", "image",
                    "public_id", publicId,
                    "folder", folder,
                    "overwrite", false
            ));
            return new UploadResult(
                    required(result, "public_id"),
                    required(result, "format"),
                    required(result, "secure_url")
            );
        } catch (IOException exception) {
            throw new StorageException("Nao foi possivel enviar a imagem ao Cloudinary", exception);
        }
    }

    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image",
                    "invalidate", true
            ));
        } catch (IOException exception) {
            throw new StorageException("Nao foi possivel excluir a imagem do Cloudinary", exception);
        }
    }

    private String required(Map<?, ?> result, String key) {
        Object value = result.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new StorageException("Resposta invalida do Cloudinary: campo " + key + " ausente");
        }
        return value.toString();
    }

    public record UploadResult(String publicId, String format, String secureUrl) {
    }
}
