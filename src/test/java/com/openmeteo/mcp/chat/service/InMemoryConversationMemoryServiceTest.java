package com.openmeteo.mcp.chat.service;

import com.openmeteo.mcp.chat.model.ChatSession;
import com.openmeteo.mcp.chat.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryConversationMemoryService.
 */
class InMemoryConversationMemoryServiceTest {
    
    private InMemoryConversationMemoryService memoryService;
    
    @BeforeEach
    void setUp() {
        memoryService = new InMemoryConversationMemoryService();
    }
    
    @Test
    void testSaveAndGetSession() throws ExecutionException, InterruptedException {
        // Given
        var session = ChatSession.create("test-session-1");
        
        // When
        memoryService.saveSession(session).get();
        var retrieved = memoryService.getSession("test-session-1").get();
        
        // Then
        assertTrue(retrieved.isPresent());
        assertEquals("test-session-1", retrieved.get().sessionId());
    }
    
    @Test
    void testGetNonExistentSession() throws ExecutionException, InterruptedException {
        // When
        var retrieved = memoryService.getSession("non-existent").get();
        
        // Then
        assertTrue(retrieved.isEmpty());
    }
    
    @Test
    void testDeleteSession() throws ExecutionException, InterruptedException {
        // Given
        var session = ChatSession.create("test-session-2");
        memoryService.saveSession(session).get();
        
        // When
        memoryService.deleteSession("test-session-2").get();
        var retrieved = memoryService.getSession("test-session-2").get();
        
        // Then
        assertTrue(retrieved.isEmpty());
    }
    
    @Test
    void testSaveAndGetMessages() throws ExecutionException, InterruptedException {
        // Given
        var msg1 = Message.user("session-1", "Hello");
        var msg2 = Message.assistant("session-1", "Hi there!");
        
        // When
        memoryService.saveMessage(msg1).get();
        memoryService.saveMessage(msg2).get();
        var messages = memoryService.getMessages("session-1").get();
        
        // Then
        assertEquals(2, messages.size());
        assertEquals("Hello", messages.get(0).content());
        assertEquals("Hi there!", messages.get(1).content());
    }
    
    @Test
    void testGetRecentMessages() throws ExecutionException, InterruptedException {
        // Given
        for (int i = 0; i < 10; i++) {
            memoryService.saveMessage(Message.user("session-2", "Message " + i)).get();
        }
        
        // When
        var recent = memoryService.getRecentMessages("session-2", 3).get();
        
        // Then
        assertEquals(3, recent.size());
        assertEquals("Message 7", recent.get(0).content());
        assertEquals("Message 8", recent.get(1).content());
        assertEquals("Message 9", recent.get(2).content());
    }
    
    @Test
    void testDeleteMessages() throws ExecutionException, InterruptedException {
        // Given
        memoryService.saveMessage(Message.user("session-3", "Test")).get();
        
        // When
        memoryService.deleteMessages("session-3").get();
        var messages = memoryService.getMessages("session-3").get();
        
        // Then
        assertTrue(messages.isEmpty());
    }
    
    @Test
    void testCleanupExpiredSessions() throws ExecutionException, InterruptedException {
        // Given
        var session1 = ChatSession.create("old-session");
        var session2 = ChatSession.create("new-session");
        memoryService.saveSession(session1).get();
        memoryService.saveSession(session2).get();
        
        // When - cleanup sessions older than 0 minutes (all sessions)
        var cleaned = memoryService.cleanupExpiredSessions(0).get();
        
        // Then
        assertEquals(2, cleaned);
        assertTrue(memoryService.getSession("old-session").get().isEmpty());
        assertTrue(memoryService.getSession("new-session").get().isEmpty());
    }
    
    @Test
    void testSessionIsolation() throws ExecutionException, InterruptedException {
        // Given
        memoryService.saveMessage(Message.user("session-a", "Message A")).get();
        memoryService.saveMessage(Message.user("session-b", "Message B")).get();
        
        // When
        var messagesA = memoryService.getMessages("session-a").get();
        var messagesB = memoryService.getMessages("session-b").get();
        
        // Then
        assertEquals(1, messagesA.size());
        assertEquals(1, messagesB.size());
        assertEquals("Message A", messagesA.get(0).content());
        assertEquals("Message B", messagesB.get(0).content());
    }
}
