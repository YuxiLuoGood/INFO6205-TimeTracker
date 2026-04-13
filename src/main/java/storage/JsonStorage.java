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

/**
 * JsonStorage handles reading and writing all application data to a local JSON file.
 *
 * Data is persisted in data.json in the project root directory.
 * On launch, load() restores the previous session's projects and time entries.
 * On exit, save() serializes everything back to disk.
 *
 * Because Gson cannot directly serialize custom ADT classes (MyLinkedList),
 * we convert Project objects to plain DTO (Data Transfer Object) instances
 * that only use standard Java types before serialization.
 */
public class JsonStorage {

    private static final String FILE_PATH = "data.json";

    // Gson instance configured with custom adapters for java.time types,
    // which Gson does not support out of the box.
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    // ── Save ──────────────────────────────────────────────────

    /**
     * Serializes all projects and their time entries to data.json.
     * Each Project is first converted to a ProjectDTO so that Gson can
     * handle the data without needing to understand MyLinkedList.
     * Called from Main.java inside the windowClosing() handler.
     *
     * @param projects the current list of projects to persist
     */
    public static void save(List<Project> projects) {
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

    // ── Load ──────────────────────────────────────────────────

    /**
     * Reads data.json and reconstructs the list of Project objects.
     * Each ProjectDTO is converted back into a Project, with its TimeEntry
     * records re-added to the project's MyLinkedList via addEntry().
     * Called once at startup in Main.java before initializing the services.
     * Returns an empty list if data.json does not exist (first launch).
     *
     * @return the restored list of projects, or an empty list if none exist
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

    // ── DTO conversion ────────────────────────────────────────

    /**
     * Converts a Project to a plain ProjectDTO for serialization.
     * Calls MyLinkedList.toList() to extract entries as a standard List,
     * since Gson cannot serialize the custom linked list structure directly.
     *
     * @param project the Project to convert
     * @return a serializable DTO containing the project's name, total duration, and entries
     */
    private static ProjectDTO toDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.name          = project.getName();
        dto.totalDuration = project.getTotalDuration();
        dto.entries       = project.getEntries().toList(); // MyLinkedList -> plain List
        return dto;
    }

    /**
     * Reconstructs a Project from a ProjectDTO after deserialization.
     * Re-adds each TimeEntry to the project using addEntry(), which appends
     * to the project's MyLinkedList and restores the totalDuration correctly.
     *
     * @param dto the deserialized DTO to convert back into a Project
     * @return a fully reconstructed Project with all time entries restored
     */
    private static Project fromDTO(ProjectDTO dto) {
        Project project = new Project(dto.name);
        if (dto.entries != null) {
            for (TimeEntry entry : dto.entries) {
                project.addEntry(entry);
            }
        }
        return project;
    }

    // ── DTO class ─────────────────────────────────────────────

    /**
     * Internal data transfer object used for JSON serialization only.
     * Uses a plain List instead of MyLinkedList so Gson can process it.
     * Not exposed outside this class.
     */
    private static class ProjectDTO {
        String         name;
        long           totalDuration;
        List<TimeEntry> entries;
    }

    // ── java.time adapters ────────────────────────────────────

    /**
     * Custom Gson adapter for LocalDateTime.
     * Serializes to ISO-8601 string (e.g. "2026-04-05T14:30:00").
     * Handles null startTime values that occur in manually entered records.
     */
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

    /**
     * Custom Gson adapter for LocalDate.
     * Serializes to ISO-8601 date string (e.g. "2026-04-05").
     */
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