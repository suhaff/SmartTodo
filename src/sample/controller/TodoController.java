package sample.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.model.Task;
import sample.util.Storage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

public class TodoController {

    @FXML private TableView<Task> taskTable;

    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> categoryColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, String> completedColumn;  // NEW COLUMN

    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> priorityBox;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryBox;

    // track the currently opened file (null = default tasks.json)
    private File currentFile = null;

    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    // Gson for reading/writing arbitrary files
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonSerializer<LocalDate>) (src, type, context) ->
                            new com.google.gson.JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonDeserializer<LocalDate>) (json, type, context) ->
                            LocalDate.parse(json.getAsString()))
            .setPrettyPrinting()
            .create();

    @FXML
    public void initialize() {

        // LOAD DATA (from Storage default tasks.json)
        taskList.addAll(Storage.loadTasks());
        taskTable.setItems(taskList);

        // Bind columns
        titleColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        categoryColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        priorityColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPriority()));
        dueDateColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDueDate()));
        descriptionColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));

        // Completed column binding: Completed / Incomplete
        completedColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().isCompleted() ? "Completed" : "Incomplete"
                )
        );

        // Populate combo boxes
        categoryBox.setItems(FXCollections.observableArrayList(
                "School", "Work", "Personal", "Shopping", "Health"
        ));

        priorityBox.setItems(FXCollections.observableArrayList(
                "High", "Medium", "Low"
        ));

        filterCategoryBox.setItems(FXCollections.observableArrayList(
                "School", "Work", "Personal", "Shopping", "Health"
        ));

        // Load selection into form
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                titleField.setText(selected.getTitle());
                descField.setText(selected.getDescription());
                datePicker.setValue(selected.getDueDate());
                categoryBox.setValue(selected.getCategory());
                priorityBox.setValue(selected.getPriority());
            }
        });
    }

    // ----------------- CRUD -----------------

    @FXML
    private void addTask() {
        Task task = new Task(
                titleField.getText(),
                descField.getText(),
                datePicker.getValue(),
                categoryBox.getValue(),
                priorityBox.getValue(),
                false  // Always start incomplete
        );

        taskList.add(task);
        saveToCurrentOrDefault(); // persist
        clearForm();
    }

    @FXML
    private void updateTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setTitle(titleField.getText());
        selected.setDescription(descField.getText());
        selected.setDueDate(datePicker.getValue());
        selected.setCategory(categoryBox.getValue());
        selected.setPriority(priorityBox.getValue());

        // Do NOT change completed status here
        taskTable.refresh();
        saveToCurrentOrDefault();
    }

    @FXML
    private void deleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        taskList.remove(selected);
        saveToCurrentOrDefault();
        clearForm();
    }

    @FXML
    private void markCompleted() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setCompleted(true);
        taskTable.refresh();
        saveToCurrentOrDefault();
    }

    // ----------------- Search & Filter -----------------

    @FXML
    private void searchTasks() {
        String keyword = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase();

        if (keyword.isEmpty()) {
            taskTable.setItems(taskList);
            return;
        }

        ObservableList<Task> filtered = taskList.filtered(
                t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(keyword)) ||
                     (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword))
        );

        taskTable.setItems(filtered);
    }

    @FXML
    private void filterCategory() {
        String category = filterCategoryBox.getValue();
        if (category == null || category.isEmpty()) {
            taskTable.setItems(taskList);
            return;
        }

        ObservableList<Task> filtered = taskList.filtered(
                t -> category.equals(t.getCategory())
        );

        taskTable.setItems(filtered);
    }

    // ----------------- File operations -----------------

    @FXML
    private void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open tasks file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files", "*.json"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        // optional: start at project folder or last known path
        File selected = chooser.showOpenDialog(getStage());
        if (selected == null) return;

        try (FileReader reader = new FileReader(selected)) {
            Type listType = new TypeToken<List<Task>>(){}.getType();
            List<Task> loaded = gson.fromJson(reader, listType);
            taskList.clear();
            if (loaded != null) taskList.addAll(loaded);
            taskTable.setItems(taskList);
            currentFile = selected;
        } catch (Exception e) {
            showError("Open failed", "Could not open file:\n" + e.getMessage());
        }
    }

    @FXML
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }
        writeTasksToFile(currentFile);
    }

    @FXML
    private void saveFileAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save tasks as");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files", "*.json"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        File selected = chooser.showSaveDialog(getStage());
        if (selected == null) return;

        // ensure extension if user didn't type .json
        if (!selected.getName().toLowerCase().endsWith(".json")) {
            selected = new File(selected.getAbsolutePath() + ".json");
        }

        currentFile = selected;
        writeTasksToFile(currentFile);
    }

    private void writeTasksToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(taskList, writer);
        } catch (Exception e) {
            showError("Save failed", "Could not save file:\n" + e.getMessage());
        }
    }

    // Save to currently selected file, or default to Storage.saveTasks()
    private void saveToCurrentOrDefault() {
        if (currentFile == null) {
            // default local storage
            Storage.saveTasks(taskList);
        } else {
            writeTasksToFile(currentFile);
        }
    }

    // ----------------- Export / Print -----------------

    @FXML
    private void exportPDF() {
        // Using JavaFX PrinterJob. Many systems allow "Save as PDF" in the dialog.
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showError("Export failed", "No printer job available.");
            return;
        }
        Stage stage = getStage();
        boolean proceed = job.showPrintDialog(stage);
        if (!proceed) return;

        // Print the TableView node (scaled if necessary)
        boolean success = job.printPage(taskTable);
        if (success) {
            job.endJob();
        } else {
            showError("Export failed", "Printing failed.");
        }
    }

    // ----------------- About & Details windows -----------------

    @FXML
    private void showAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/about.fxml"));
            Scene scene = new Scene(loader.load());
            Stage st = new Stage();
            st.setTitle("About Us");
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(scene);
            st.showAndWait();
        } catch (Exception e) {
            showError("Error", "Could not open About window:\n" + e.getMessage());
        }
    }

    @FXML
    private void showDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/details.fxml"));
            Scene scene = new Scene(loader.load());
            Stage st = new Stage();
            st.setTitle("Details / User Guide");
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(scene);
            st.showAndWait();
        } catch (Exception e) {
            showError("Error", "Could not open Details window:\n" + e.getMessage());
        }
    }

    @FXML
    private void closeApp() {
        Stage stage = getStage();
        if (stage != null) stage.close();
    }

    // ----------------- Helpers -----------------

    private Stage getStage() {
        if (taskTable == null) return null;
        Scene s = taskTable.getScene();
        if (s == null) return null;
        return (Stage) s.getWindow();
    }

    private void clearForm() {
        titleField.clear();
        descField.clear();
        datePicker.setValue(null);
        categoryBox.setValue(null);
        priorityBox.setValue(null);
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.initOwner(getStage());
        a.showAndWait();
    }
}
