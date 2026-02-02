package com.openmeteo.mcp.chat.controller;

import com.openmeteo.mcp.chat.model.AiResponse;
import com.openmeteo.mcp.chat.model.ChatSession;
import com.openmeteo.mcp.chat.service.ChatHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatController.
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    
    @Mock
    private ChatHandler chatHandler;
    
    private ChatController chatController;
    
    @BeforeEach
    void setUp() {
        chatController = new ChatController(chatHandler);
    }
    
    @Test
    void testSendMessage() {
        // Given
        var sessionId = "test-session";
        var request = new ChatController.ChatRequest("What's the weather?");
        var response = AiResponse.of("It's sunny!", Map.of("latencyMs", 100L));
        
        when(chatHandler.chat(eq(sessionId), eq("What's the weather?")))
            .thenReturn(CompletableFuture.completedFuture(response));
        
        // When
        var result = chatController.sendMessage(sessionId, request).join();
        
        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("It's sunny!", result.getBody().content());
        assertEquals(100L, result.getBody().metadata().get("latencyMs"));
        
        verify(chatHandler).chat(sessionId, "What's the weather?");
    }
    
    @Test
    void testSendMessageWithError() {
        // Given
        var sessionId = "test-session";
        var request = new ChatController.ChatRequest("Test message");
        
        when(chatHandler.chat(eq(sessionId), any()))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("LLM error")));
        
        // When
        var result = chatController.sendMessage(sessionId, request).join();
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
    
    @Test
    void testGetSession() {
        // Given
        var sessionId = "test-session";
        var session = ChatSession.create(sessionId);
        
        when(chatHandler.getSession(eq(sessionId)))
            .thenReturn(CompletableFuture.completedFuture(session));
        
        // When
        var result = chatController.getSession(sessionId).join();
        
        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(sessionId, result.getBody().sessionId());
        
        verify(chatHandler).getSession(sessionId);
    }
    
    @Test
    void testGetSessionNotFound() {
        // Given
        var sessionId = "non-existent";
        
        when(chatHandler.getSession(eq(sessionId)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Not found")));
        
        // When
        var result = chatController.getSession(sessionId).join();
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
    
    @Test
    void testDeleteSession() {
        // Given
        var sessionId = "test-session";
        
        when(chatHandler.deleteSession(eq(sessionId)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        // When
        var result = chatController.deleteSession(sessionId).join();
        
        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        
        verify(chatHandler).deleteSession(sessionId);
    }
    
    @Test
    void testDeleteSessionWithError() {
        // Given
        var sessionId = "test-session";
        
        when(chatHandler.deleteSession(eq(sessionId)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Delete error")));
        
        // When
        var result = chatController.deleteSession(sessionId).join();
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}
