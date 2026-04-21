package com.openmeteo.mcp.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test configuration providing mock beans for integration tests.
 */
@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    @Primary
    public ChatModel chatModel() {
        return mock(ChatModel.class);
    }
}
