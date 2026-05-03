package com.example.music.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(60000);
        RestTemplate restTemplate = new RestTemplate(factory);
        // QQ Music API returns text/plain instead of application/json
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(
                org.springframework.http.MediaType.APPLICATION_JSON,
                org.springframework.http.MediaType.TEXT_PLAIN,
                org.springframework.http.MediaType.TEXT_HTML
        ));
        restTemplate.getMessageConverters().add(0, converter);
        return restTemplate;
    }
}
