package br.com.cidadeemfoco.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class PublicApiRateLimitInterceptorTest {

    @Test
    void shouldReturnStandardErrorAfterLimitWithoutEchoingRequestData() throws Exception {
        PublicApiRateLimitInterceptor interceptor = new PublicApiRateLimitInterceptor(
                true, 1, JsonMapper.builder().build()
        );
        MockHttpServletRequest firstRequest = request();
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletRequest secondRequest = request();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(firstRequest, firstResponse, new Object())).isTrue();
        assertThat(interceptor.preHandle(secondRequest, secondResponse, new Object())).isFalse();

        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getHeader("Retry-After")).isNotBlank();
        assertThat(secondResponse.getContentAsString()).contains("Limite de requisicoes");
        assertThat(secondResponse.getContentAsString()).doesNotContain("token", "email", "telefone");
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/alerts/active");
        request.setRemoteAddr("192.0.2.10");
        return request;
    }
}
