package org.puregxl.site.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmbeddingClient {

    private static final String API_URL = "https://api.siliconflow.cn/v1/embeddings";
    private static final String MODEL = "Qwen/Qwen3-Embedding-8B";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EmbeddingClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }


    public List<double[]> embed(List<String> texts) throws Exception {
        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("input", texts);
        requestBody.put("encoding_format", "float");

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 发送 HTTP 请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API 调用失败，状态码：" + response.statusCode()
                    + "，响应：" + response.body());
        }

        // 解析响应，提取向量
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode dataArray = root.get("data");

        List<double[]> embeddings = new ArrayList<>();
        for (JsonNode item : dataArray) {
            JsonNode embeddingNode = item.get("embedding");
            double[] vector = new double[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = embeddingNode.get(i).asDouble();
            }
            embeddings.add(vector);
        }

        return embeddings;
    }


    /**
     * 分批向量化
     *
     * @param texts     所有待向量化的文本
     * @param batchSize 每批的大小（建议 20~50）
     * @return 所有文本的向量
     */
    public List<double[]> embedInBatches(List<String> texts, int batchSize) throws Exception {
        List<double[]> allEmbeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);

            System.out.printf("向量化进度：%d/%d%n", end, texts.size());
            List<double[]> batchEmbeddings = embed(batch);
            allEmbeddings.addAll(batchEmbeddings);

            // 简单的限流：每批之间等一下，避免触发 API 的速率限制
            if (end < texts.size()) {
                Thread.sleep(200);
            }
        }

        return allEmbeddings;
    }


    /**
     * 将单段文本转成向量（便捷方法）
     */
    public double[] embed(String text) throws Exception {
        return embed(List.of(text)).get(0);
    }


    public List<double[]> embedInBatchesV2(List<String> texts, int batchSize) throws Exception {
        List<double[]> allEmbeddings = new ArrayList<>();

        for (int i = 0 ; i < texts.size(); i += batchSize) {
            int end = Math.min(texts.size(), i + batchSize);
            List<String> batch = texts.subList(i, end);
            System.out.printf("向量化进度：%d/%d%n", end, texts.size());
            List<double[]> batchEmbeddings = embed(batch);
            allEmbeddings.addAll(batchEmbeddings);

            if (end < texts.size()) {
                Thread.sleep(200);
            }
        }

        return allEmbeddings;
    }
}
