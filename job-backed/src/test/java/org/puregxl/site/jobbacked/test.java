package org.puregxl.site.jobbacked;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.puregxl.site.jobbacked.dao.entity.JobPost;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.mapper.JobPostMapper;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class test {

    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    private static final String CONTEXT_KEY = "job_backed:context:%s";


    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String API_KEY = "sk-rjtfqcpnhpzonswkebygmaqnqvibqcndgqxqfxghizuguthf";
    private static final String MODEL = Objects.requireNonNullElse(
            System.getenv("SILICONFLOW_CONTEXT_MODEL"),
            "Qwen/Qwen2.5-7B-Instruct"
    );

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .protocols(List.of(Protocol.HTTP_1_1))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    void test() throws IOException {
        withMemoryDemoRedis();
    }


    /**
     * 使用redis 实现上下文窗口热管理
     */
    public void withMemoryDemoRedis() throws IOException {
        String userId = "T00001";
        stringRedisTemplate.delete(String.format(CONTEXT_KEY, userId));
        sendByContext("用户：iPhone 16 Pro 的退货政策是什么？", userId);
        sendByContext("那它的保修期呢？", userId);
        sendByContext("过了保修期维修大概多少钱？", userId);
    }

    public void sendByContext(String userQuestion, String userID) throws IOException {
        List<JsonObject> redisContext = getRedisContext(userID);
        redisContext.add(message("user", userQuestion));
        String answer = loadRedis(redisContext, userID);
        System.out.println(userQuestion);
        System.out.println("助手：" + answer);
    }

    public String loadRedis(List<JsonObject> history, String userId) throws IOException {
        String answer = chat(history);
        history.add(message("assistant", answer));
        stringRedisTemplate.opsForValue().set(String.format(CONTEXT_KEY, userId), gson.toJson(history));
        return answer;
    }

    public List<JsonObject> getRedisContext(String userId) {
        String context = stringRedisTemplate.opsForValue().get(String.format(CONTEXT_KEY, userId));
        List<JsonObject> result = new ArrayList<>();

        if (context == null || context.isEmpty()) {
            return result;
        }

        JsonArray jsonArray = gson.fromJson(context, JsonArray.class);
        if (jsonArray == null) {
            return result;
        }

        for (JsonElement element : jsonArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject jsonObject = element.getAsJsonObject();
            if (isValidMessage(jsonObject)) {
                result.add(jsonObject);
            }
        }

        return result;
    }

    private static boolean isValidMessage(JsonObject message) {
        if (message == null
                || !message.has("role")
                || !message.has("content")
                || message.get("role").isJsonNull()
                || message.get("content").isJsonNull()) {
            return false;
        }

        String role = message.get("role").getAsString();
        return switch (role) {
            case "system", "user", "assistant", "tool" -> true;
            default -> false;
        };
    }


    public static String chat(List<JsonObject> messages) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("temperature", 0.1);
        body.addProperty("max_tokens", 512);
        JsonArray messagesArray = new JsonArray();
        for (JsonObject msg : messages) {
            messagesArray.add(msg);
        }
        body.add("messages", messagesArray);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                throw new IOException("请求失败：" + response + "，响应：" + responseBody);
            }

            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IOException("响应缺少 choices 字段，响应：" + responseBody);
            }

            return choices
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }


    public static JsonObject message(String role, String content) {
        JsonObject msg = new JsonObject();
        msg.addProperty("role", role);
        msg.addProperty("content", content);
        return msg;
    }


    /**
     * 发送 HTTP 请求
     */
    private static JsonObject sendRequest(JsonObject requestBody) throws IOException {
        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "RagTest/1.0 OkHttp")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                throw new IOException("请求失败：" + response + "，响应：" + responseBody);
            }
            return gson.fromJson(responseBody, JsonObject.class);
        }
    }


}
