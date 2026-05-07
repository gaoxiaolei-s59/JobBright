package org.puregxl.site.infra.embedding;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class EmbeddingServiceImpl implements EmbeddingService{
    @Override
    public List<Float> embed(String text) {
        return List.of();
    }

    @Override
    public List<Float> embed(String text, String modelId) {
        return List.of();
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        return List.of();
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts, String modelId) {
        return List.of();
    }
}
