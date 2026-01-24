package com.assessment.demo.services;

import com.assessment.demo.dao.VacationRepository;
import com.assessment.demo.entities.Vacation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for converting vacation data into vector embeddings.
 * 
 * This service takes vacation descriptions and stores them as vectors
 * in PostgreSQL (using pgvector) for semantic search.
 */
@Service
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = false)
public class VacationEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(VacationEmbeddingService.class);

    private final VacationRepository vacationRepository;
    private final VectorStore vectorStore;

    public VacationEmbeddingService(VacationRepository vacationRepository, VectorStore vectorStore) {
        this.vacationRepository = vacationRepository;
        this.vectorStore = vectorStore;
    }

    /**
     * Ingests all vacations into the vector store.
     * Call this once to populate the vector database, or when vacations are updated.
     * 
     * @return Number of vacations ingested
     */
    public int ingestAllVacations() {
        logger.info("Starting vacation data ingestion into vector store...");
        
        List<Vacation> vacations = vacationRepository.findAll();
        
        if (vacations.isEmpty()) {
            logger.warn("No vacations found to ingest");
            return 0;
        }

        // Convert vacations to Documents for the vector store
        List<Document> documents = vacations.stream()
            .map(this::vacationToDocument)
            .toList();

        vectorStore.add(documents);
        
        logger.info("Successfully ingested {} vacations into vector store", documents.size());
        return documents.size();
    }

    /**
     * Converts a Vacation entity to a Document for vector storage.
     * The content combines title and description for better search results.
     */
    private Document vacationToDocument(Vacation vacation) {
    
        String content = String.format(
            "Vacation: %s. Description: %s. Price: $%.2f",
            vacation.getVacation_title(),
            vacation.getDescription(),
            vacation.getTravel_price()
        );

        Map<String, Object> metadata = Map.of(
            "vacationId", vacation.getId(),
            "title", vacation.getVacation_title(),
            "price", vacation.getTravel_price(),
            "imageUrl", vacation.getImage_URL() != null ? vacation.getImage_URL() : ""
        );

        return new Document(content, metadata);
    }

    
    public void ingestVacation(Vacation vacation) {
        Document document = vacationToDocument(vacation);
        vectorStore.add(List.of(document));
        logger.info("Ingested vacation: {}", vacation.getVacation_title());
    }
}
