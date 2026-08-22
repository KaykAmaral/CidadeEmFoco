package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.OccurrenceImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OccurrenceImageControllerTest {

    @Mock
    private OccurrenceImageService occurrenceImageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OccurrenceImageController(occurrenceImageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );
        when(occurrenceImageService.upload(eq("citizen@example.com"), eq(10L), any()))
                .thenReturn(response());

        mockMvc.perform(multipart("/api/occurrences/10/image")
                        .file(file)
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/api/occurrences/10/image"));
    }

    @Test
    void shouldDownloadImageInline() throws Exception {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        when(occurrenceImageService.load(10L)).thenReturn(new StoredImage(
                "imagem.jpg",
                MediaType.IMAGE_JPEG,
                new ByteArrayResource(content)
        ));

        mockMvc.perform(get("/api/occurrences/10/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(content))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"imagem.jpg\""));
    }

    private OccurrenceResponse response() {
        return new OccurrenceResponse(
                10L,
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                "Via alagada",
                PerceivedRisk.ALTO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                null,
                "/api/occurrences/10/image",
                OccurrenceStatus.REGISTRADA,
                Instant.parse("2026-08-21T18:00:00Z"),
                Instant.parse("2026-08-21T18:00:00Z")
        );
    }

    private Authentication authentication() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "citizen@example.com",
                null,
                List.of()
        );
    }
}
