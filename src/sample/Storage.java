package sample;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import sample.model.Task;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Storage {

    private static final String FILE_NAME = "tasks.json";

    public static void save(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            Gson gson = new Gson();
            gson.toJson(tasks, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Task> load() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            Gson gson = new Gson();
            List<Task> list = gson.fromJson(reader, new TypeToken<List<Task>>() {}.getType());
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
