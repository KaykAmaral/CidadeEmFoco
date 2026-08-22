package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class OccurrenceImageService {

    private final OccurrenceRepository occurrenceRepository;
    private final LocalImageStorageService imageStorageService;

    public OccurrenceImageService(
            OccurrenceRepository occurrenceRepository,
            LocalImageStorageService imageStorageService
    ) {
        this.occurrenceRepository = occurrenceRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public OccurrenceResponse upload(String userEmail, Long occurrenceId, MultipartFile file) {
        Occurrence occurrence = findOccurrence(occurrenceId);
        if (!occurrence.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Somente o autor pode adicionar a foto");
        }

        StoredImage storedImage = imageStorageService.store(file);
        String previousImage = occurrence.replaceImage(storedImage.filename());
        scheduleFileCleanup(storedImage.filename(), previousImage);
        return OccurrenceResponse.from(occurrenceRepository.saveAndFlush(occurrence));
    }

    public StoredImage load(Long occurrenceId) {
        Occurrence occurrence = findOccurrence(occurrenceId);
        if (occurrence.getImagePath() == null) {
            throw new ResourceNotFoundException("A ocorrencia nao possui imagem");
        }
        return imageStorageService.load(occurrence.getImagePath());
    }

    private Occurrence findOccurrence(Long id) {
        return occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrencia nao encontrada"));
    }

    private void scheduleFileCleanup(String newImage, String previousImage) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            imageStorageService.deleteQuietly(previousImage);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    imageStorageService.deleteQuietly(previousImage);
                } else {
                    imageStorageService.deleteQuietly(newImage);
                }
            }
        });
    }
}
