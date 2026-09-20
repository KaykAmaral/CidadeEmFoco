package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
public class CloudinaryImageStorageService implements ImageStorageService {

    private static final String KEY_PREFIX = "cloudinary|";

    private final CloudinaryClient cloudinaryClient;
    private final String cloudName;
    private final String folder;

    public CloudinaryImageStorageService(
            CloudinaryClient cloudinaryClient,
            @Value("${app.storage.cloudinary.cloud-name}") String cloudName,
            @Value("${app.storage.cloudinary.folder}") String folder
    ) {
        this.cloudinaryClient = cloudinaryClient;
        this.cloudName = cloudName;
        this.folder = folder;
    }

    @Override
    public StoredImage store(MultipartFile file) {
        ImageFileValidator.ValidatedImage image = ImageFileValidator.validate(file);
        CloudinaryClient.UploadResult uploaded = cloudinaryClient.upload(
                image.content(), UUID.randomUUID().toString(), folder
        );
        String storageKey = KEY_PREFIX + uploaded.publicId() + "|" + uploaded.format()
                + "|" + uploaded.secureUrl();
        if (storageKey.length() > 500) {
            cloudinaryClient.delete(uploaded.publicId());
            throw new StorageException("A referencia da imagem excedeu o limite permitido");
        }
        return storedImage(storageKey);
    }

    @Override
    public StoredImage load(String storageKey) {
        return storedImage(storageKey);
    }

    @Override
    public void deleteQuietly(String storageKey) {
        try {
            cloudinaryClient.delete(parse(storageKey).publicId());
        } catch (RuntimeException ignored) {
            // Uma falha de limpeza nao deve desfazer o cadastro das demais imagens.
        }
    }

    private StoredImage storedImage(String storageKey) {
        CloudinaryReference reference = parse(storageKey);
        validateUrl(reference.secureUrl());
        try {
            return new StoredImage(
                    storageKey,
                    filename(reference),
                    ImageFileValidator.mediaTypeFromFormat(reference.format()),
                    new UrlResource(reference.secureUrl())
            );
        } catch (MalformedURLException exception) {
            throw new StorageException("URL de imagem invalida no Cloudinary", exception);
        }
    }

    private CloudinaryReference parse(String storageKey) {
        if (storageKey == null || !storageKey.startsWith(KEY_PREFIX)) {
            throw new BusinessRuleException("Referencia de imagem do Cloudinary invalida");
        }
        String[] parts = storageKey.split("\\|", 4);
        if (parts.length != 4 || parts[1].isBlank() || parts[2].isBlank() || parts[3].isBlank()) {
            throw new BusinessRuleException("Referencia de imagem do Cloudinary invalida");
        }
        return new CloudinaryReference(parts[1], parts[2], parts[3]);
    }

    private void validateUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("URL de imagem do Cloudinary invalida");
        }
        String expectedPathPrefix = "/" + cloudName + "/";
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !"res.cloudinary.com".equalsIgnoreCase(uri.getHost())
                || uri.getPath() == null
                || !uri.getPath().startsWith(expectedPathPrefix)) {
            throw new BusinessRuleException("URL de imagem do Cloudinary invalida");
        }
    }

    private String filename(CloudinaryReference reference) {
        int slash = reference.publicId().lastIndexOf('/');
        String name = slash >= 0 ? reference.publicId().substring(slash + 1) : reference.publicId();
        return name + "." + reference.format();
    }

    private record CloudinaryReference(String publicId, String format, String secureUrl) {
    }
}
