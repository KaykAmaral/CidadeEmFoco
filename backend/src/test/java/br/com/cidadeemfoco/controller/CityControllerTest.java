package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.CityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CityControllerTest {

    @Mock
    private CityService cityService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CityController(cityService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnGeoJsonBoundary() throws Exception {
        String geoJson = "{\"type\":\"FeatureCollection\",\"features\":[]}";
        when(cityService.findBoundary("3541000")).thenReturn(geoJson);

        mockMvc.perform(get("/api/cities/3541000/boundary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/geo+json"))
                .andExpect(jsonPath("$.type").value("FeatureCollection"));
    }

    @Test
    void shouldRejectInvalidIbgeCode() throws Exception {
        mockMvc.perform(get("/api/cities/123/boundary"))
                .andExpect(status().isBadRequest());
    }
}
