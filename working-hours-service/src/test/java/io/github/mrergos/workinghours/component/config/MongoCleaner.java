package io.github.mrergos.workinghours.component.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class MongoCleaner {

    private static final Logger log = LoggerFactory.getLogger(MongoCleaner.class);
    private static final String COLLECTION_NAME = "trainer_summaries";

    private final MongoTemplate mongoTemplate;

    public MongoCleaner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void clean() {
        long removed = mongoTemplate.remove(new Query(), COLLECTION_NAME).getDeletedCount();
        log.debug("Cleaned {} collection before scenario, removedDocuments={}", COLLECTION_NAME, removed);
    }
}
