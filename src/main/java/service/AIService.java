package service;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import model.DailySummary;
import model.Project;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIService {

    private static final String API_URL   = "http://localhost:11434/api/generate";
    private static final String MODEL     = "llama3.2:latest";
    private final OkHttpClient client;
    private final Gson gson;

    public AIService() {
        this.client = new OkHttpClient.Builder()
                .callTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    //主入口 

    public String getSuggestions(List<DailySummary> recentDays, List<Project> sortedProjects) {
        String prompt = buildPrompt(recentDays, sortedProjects);
        System.out.println("=== Prompt sent to Ollama ===\n" + prompt); // 调试用
        try {
            return callOllama(prompt);
        } catch (IOException e) {
            return "Failed to get suggestions: " + e.getMessage();
        }
    }

    // Prompt 构建

    private String buildPrompt(List<DailySummary> recentDays, List<Project> sortedProjects) {
        StringBuilder sb = new StringBuilder();

        sb.append("Here is my time tracking data for the past ")
          .append(recentDays.size()).append(" day(s):\n\n");

        for (DailySummary day : recentDays) {
            sb.append("Date: ").append(day.getDate()).append("\n");
            sb.append("  Total: ").append(formatDuration(day.getTotalDuration())).append("\n");
            for (String project : day.getProjectNames()) {
                long dur = day.getDurationForProject(project);
                sb.append("  - ").append(project)
                  .append(": ").append(formatDuration(dur)).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Overall ranking by total time spent:\n");
        int rank = 1;
        for (Project p : sortedProjects) {
            if (p.getTotalDuration() == 0) continue;
            sb.append(rank++).append(". ")
              .append(p.getName()).append(": ")
              .append(formatDuration(p.getTotalDuration())).append("\n");
        }

        sb.append("\nNote: some entries may be manually added and are approximations.\n\n");
        sb.append("Based on this data, give me exactly 3 concise, actionable suggestions ");
        sb.append("to improve my time habits. Number each suggestion.");

        return sb.toString();
    }

    // Ollama API 调用

    private String callOllama(String prompt) throws IOException {
        String json = "{\"model\":\"" + MODEL + "\",\"prompt\":"
                + gson.toJson(prompt)
                + ",\"stream\":false}";

        RequestBody reqBody = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(reqBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "";
                return "Ollama error: " + response.code() + " " + errBody;
            }
            String responseBody = response.body().string();
            JsonObject json2 = gson.fromJson(responseBody, JsonObject.class);
            return json2.get("response").getAsString();
        }
    }

    //工具方法 

    private String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }
}