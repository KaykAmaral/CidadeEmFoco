package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.OccurrenceImage;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OccurrenceImageService {
    private static final int MAX_IMAGES = 4;
    private final OccurrenceRepository occurrenceRepository;
    private final ImageStorageService imageStorageService;

    public OccurrenceImageService(OccurrenceRepository occurrenceRepository,
            ImageStorageService imageStorageService) {
        this.occurrenceRepository = occurrenceRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public OccurrenceResponse upload(String userEmail, Long occurrenceId, MultipartFile file) {
        return uploadMany(userEmail, occurrenceId, List.of(file));
    }

    @Transactional
    public OccurrenceResponse uploadMany(String userEmail, Long occurrenceId, List<MultipartFile> files) {
        Occurrence occurrence = findOccurrence(occurrenceId);
        if (!occurrence.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Somente o autor pode adicionar fotos");
        }
        if (files == null || files.isEmpty()) throw new BusinessRuleException("Selecione ao menos uma imagem");
        if (occurrence.getImages().size() + files.size() > MAX_IMAGES) {
            throw new BusinessRuleException("A ocorrencia pode ter no maximo 4 imagens");
        }
        List<String> storedPaths = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                StoredImage stored = imageStorageService.store(file);
                storedPaths.add(stored.storageKey());
                occurrence.addImage(stored.storageKey());
            }
            return OccurrenceResponse.from(occurrenceRepository.saveAndFlush(occurrence));
        } catch (RuntimeException exception) {
            storedPaths.forEach(imageStorageService::deleteQuietly);
            throw exception;
        }
    }

    public StoredImage load(Long occurrenceId) {
        Occurrence occurrence = findOccurrence(occurrenceId);
        if (!occurrence.getImages().isEmpty()) return imageStorageService.load(occurrence.getImages().getFirst().getImagePath());
        if (occurrence.getImagePath() != null) return imageStorageService.load(occurrence.getImagePath());
        throw new ResourceNotFoundException("A ocorrencia nao possui imagem");
    }

    public StoredImage load(Long occurrenceId, Long imageId) {
        OccurrenceImage image = findOccurrence(occurrenceId).getImages().stream()
                .filter(value -> value.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Imagem da ocorrencia nao encontrada"));
        return imageStorageService.load(image.getImagePath());
    }

    private Occurrence findOccurrence(Long id) {
        return occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrencia nao encontrada"));
    }
}
