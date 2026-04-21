package com.openmeteo.mcp.config;

import com.openmeteo.mcp.service.StreamingChatService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class MockBeansTestConfiguration {

    @Bean
    @Primary
    public StreamingChatService streamingChatService() {
        return mock(StreamingChatService.class);
    }
}
