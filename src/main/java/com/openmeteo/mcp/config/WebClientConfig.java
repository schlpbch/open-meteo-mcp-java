package com.openmeteo.mcp.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for Open-Meteo API clients.
 * <p>
 * Configures separate WebClient beans for each Open-Meteo API endpoint
 * with gzip compression, timeouts, and proper headers.
 * </p>
 */
@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${openmeteo.api.weather-url}")
    private String weatherUrl;

    @Value("${openmeteo.api.air-quality-url}")
    private String airQualityUrl;

    @Value("${openmeteo.api.geocoding-url}")
    private String geocodingUrl;

    @Value("${openmeteo.api.marine-url}")
    private String marineUrl;

    @Value("${openmeteo.api.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${openmeteo.api.gzip-enabled:true}")
    private boolean gzipEnabled;

    /**
     * Creates a base WebClient.Builder with common configuration.
     *
     * @return configured WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS)));

        // Enable gzip compression if configured
        if (gzipEnabled) {
            httpClient = httpClient.compress(true);
            log.debug("Gzip compression enabled for HTTP client");
        }

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.USER_AGENT, "open-meteo-mcp-java/1.0.0-alpha")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
    }

    /**
     * WebClient for weather forecast API.
     *
     * @param builder WebClient.Builder
     * @return configured WebClient for weather API
     */
    @Bean(name = "weatherWebClient")
    public WebClient weatherWebClient(WebClient.Builder builder) {
        log.info("Configuring weatherWebClient for URL: {}", weatherUrl);
        return builder
                .baseUrl(weatherUrl)
                .build();
    }

    /**
     * WebClient for air quality API.
     *
     * @param builder WebClient.Builder
     * @return configured WebClient for air quality API
     */
    @Bean(name = "airQualityWebClient")
    public WebClient airQualityWebClient(WebClient.Builder builder) {
        log.info("Configuring airQualityWebClient for URL: {}", airQualityUrl);
        return builder
                .baseUrl(airQualityUrl)
                .build();
    }

    /**
     * WebClient for geocoding API.
     *
     * @param builder WebClient.Builder
     * @return configured WebClient for geocoding API
     */
    @Bean(name = "geocodingWebClient")
    public WebClient geocodingWebClient(WebClient.Builder builder) {
        log.info("Configuring geocodingWebClient for URL: {}", geocodingUrl);
        return builder
                .baseUrl(geocodingUrl)
                .build();
    }

    /**
     * WebClient for marine API (future use).
     *
     * @param builder WebClient.Builder
     * @return configured WebClient for marine API
     */
    @Bean(name = "marineWebClient")
    public WebClient marineWebClient(WebClient.Builder builder) {
        log.info("Configuring marineWebClient for URL: {}", marineUrl);
        return builder
                .baseUrl(marineUrl)
                .build();
    }
}
