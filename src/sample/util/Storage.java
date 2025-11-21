package sample.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import sample.model.Task;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Storage {

    // Where tasks are saved
    private static final String FILE_NAME = "tasks.json";

    // Gson instance (pretty printing)
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Loads the tasks from tasks.json
     */
    public static List<Task> loadTasks() {

        File file = new File(FILE_NAME);

        // If file doesn't exist yet → return empty list
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {

            Type listType = new TypeToken<List<Task>>() {}.getType();
            List<Task> tasks = gson.fromJson(reader, listType);

            if (tasks == null) {
                return new ArrayList<>();
            }

            return tasks;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();   // fallback
        }
    }

    /**
     * Saves tasks to tasks.json
     */
    public static void saveTasks(List<Task> tasks) {

        try (FileWriter writer = new FileWriter(FILE_NAME)) {

            gson.toJson(tasks, writer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
