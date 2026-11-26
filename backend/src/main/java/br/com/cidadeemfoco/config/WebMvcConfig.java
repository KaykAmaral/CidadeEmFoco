package br.com.cidadeemfoco.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PublicApiRateLimitInterceptor publicApiRateLimitInterceptor;

    public WebMvcConfig(PublicApiRateLimitInterceptor publicApiRateLimitInterceptor) {
        this.publicApiRateLimitInterceptor = publicApiRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(publicApiRateLimitInterceptor)
                .addPathPatterns("/api/v1/public/**");
    }
}
