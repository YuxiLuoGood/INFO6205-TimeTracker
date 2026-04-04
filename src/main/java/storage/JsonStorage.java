package storage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import model.Project;
import model.TimeEntry;

public class JsonStorage {

    private static final String FILE_PATH = "data.json";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    // ----------------------------------------------------------------
    // Save
    // ----------------------------------------------------------------

    /**
     * 把所有 Project 序列化成 JSON 写入 data.json
     * 在 Main.java 的 windowClosing() 里调用
     */
    public static void save(List<Project> projects) {
        // 把每个 Project 转成可序列化的 DTO
        List<ProjectDTO> dtos = new ArrayList<>();
        for (Project p : projects) {
            dtos.add(toDTO(p));
        }

        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(dtos, writer);
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Load
    // ----------------------------------------------------------------

    /**
     * 从 data.json 读取数据，重建 Project 列表
     * 在 Main.java 启动时调用
     * 如果文件不存在则返回空列表（第一次启动）
     */
    public static List<Project> load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<ProjectDTO>>() {}.getType();
            List<ProjectDTO> dtos = gson.fromJson(reader, listType);
            if (dtos == null) return new ArrayList<>();

            List<Project> projects = new ArrayList<>();
            for (ProjectDTO dto : dtos) {
                projects.add(fromDTO(dto));
            }
            return projects;

        } catch (IOException e) {
            System.err.println("Failed to load data: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ----------------------------------------------------------------
    // DTO conversion
    // Gson 不能直接序列化自定义链表，先转成普通 List 再存
    // ----------------------------------------------------------------

    private static ProjectDTO toDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.name = project.getName();
        dto.totalDuration = project.getTotalDuration();
        dto.entries = project.getEntries().toList(); // MyLinkedList -> List
        return dto;
    }

    private static Project fromDTO(ProjectDTO dto) {
        Project project = new Project(dto.name);
        if (dto.entries != null) {
            for (TimeEntry entry : dto.entries) {
                project.addEntry(entry);
            }
        }
        return project;
    }

    // ----------------------------------------------------------------
    // DTO classes (内部用，不需要 public)
    // ----------------------------------------------------------------

    private static class ProjectDTO {
        String name;
        long totalDuration;
        List<TimeEntry> entries;
    }

    // ----------------------------------------------------------------
    // LocalDateTime / LocalDate adapters for Gson
    // Gson 默认不支持 java.time，需要手动注册
    // ----------------------------------------------------------------

    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime src, Type type,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type,
                                         JsonDeserializationContext context) {
            String s = json.getAsString();
            return s.equals("null") ? null : LocalDateTime.parse(s);
        }
    }

    private static class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate src, Type type,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type type,
                                     JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString());
        }
    }
}