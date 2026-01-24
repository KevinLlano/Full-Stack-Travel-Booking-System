package com.assessment.demo.controllers;

import com.assessment.demo.services.VacationEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for AI-powered vacation search and chat.
 * 
 * Endpoints:
 * - POST /api/ai/chat - Ask questions about vacations
 * - POST /api/ai/ingest - Ingest vacation data into vector store (admin)
 * - GET /api/ai/search - Semantic search for vacations
 */
@RestController
@RequestMapping("/api/ai")
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = false)
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final VacationEmbeddingService embeddingService;

    // System prompt that defines the AI's behavior
    private static final String SYSTEM_PROMPT = """
        You are a helpful travel assistant for our vacation booking website.
        Your role is to help customers find the perfect vacation based on their preferences.
        
        Guidelines:
        - Only recommend vacations from the provided context data
        - Be friendly, concise, and helpful
        - If the context doesn't have relevant vacations, say so politely
        - Include prices when mentioning vacations
        - Suggest excursions if relevant to the user's interests
        """;

    public AiController(ChatClient.Builder chatClientBuilder, 
                        VectorStore vectorStore,
                        VacationEmbeddingService embeddingService) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
    }


    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        logger.info("AI Chat request: {}", userMessage);

        try {
            // 1. Search vector store for relevant vacations (top 5)
            List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.query(userMessage).withTopK(5)
            );

            // 2. Build context from relevant documents
            String context = relevantDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));

            // 3. Build the prompt with context
            String promptWithContext = String.format("""
                Based on these available vacations:
                
                %s
                
                Customer question: %s
                """, context, userMessage);

            // 4. Call the LLM
            String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(promptWithContext)
                .call()
                .content();

            // 5. Return response with metadata
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("matchedVacations", relevantDocs.stream()
                .map(doc -> doc.getMetadata().get("title"))
                .toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("AI Chat error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to process request: " + e.getMessage()));
        }
    }

    /**
     * Semantic search endpoint - Find vacations matching a query.
     * Returns raw search results without AI interpretation.
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> search(@RequestParam String query,
                                                       @RequestParam(defaultValue = "5") int limit) {
        logger.info("AI Search request: {}", query);

        try {
            List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(limit)
            );

            List<Map<String, Object>> vacations = results.stream()
                .map(doc -> {
                    Map<String, Object> vacation = new HashMap<>(doc.getMetadata());
                    vacation.put("content", doc.getContent());
                    return vacation;
                })
                .toList();

            return ResponseEntity.ok(Map.of(
                "query", query,
                "results", vacations,
                "count", vacations.size()
            ));

        } catch (Exception e) {
            logger.error("AI Search error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }

    
    @PostMapping(value = "/ingest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ingestVacations() {
        logger.info("Starting vacation data ingestion...");

        try {
            int count = embeddingService.ingestAllVacations();
            return ResponseEntity.ok(Map.of(
                "message", "Successfully ingested vacation data",
                "count", count
            ));
        } catch (Exception e) {
            logger.error("Ingestion error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ingestion failed: " + e.getMessage()));
        }
    }

    /**
     * Health check for AI features.
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "AI features are enabled",
            "vectorStore", "pgvector",
            "chatModel", "gpt-4o-mini"
        ));
    }
}
