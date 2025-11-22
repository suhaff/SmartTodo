package sample.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
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

    @FXML private TableColumn<Task, Boolean> checkColumn;
    @FXML private TableColumn<Task, String> statusColumn;

    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> categoryColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;

    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> priorityBox;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryBox;

    private File currentFile = null;
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonSerializer<LocalDate>) (src, type, ctx) ->
                            new com.google.gson.JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonDeserializer<LocalDate>) (json, type, ctx) ->
                            LocalDate.parse(json.getAsString()))
            .setPrettyPrinting()
            .create();

    @FXML
    public void initialize() {
        // load saved tasks into the table
        taskList.addAll(Storage.loadTasks());
        taskTable.setItems(taskList);

        // Bind columns to Task properties
        titleColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        categoryColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        priorityColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPriority()));
        dueDateColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDueDate()));
        descriptionColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));

        // status column: Completed / Incomplete
        statusColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().isCompleted() ? "Completed" : "Incomplete"
                )
        );

        // checkColumn uses default square CheckBox
        setupCheckColumn();

        // populate combo boxes
        categoryBox.setItems(FXCollections.observableArrayList("School", "Work", "Personal", "Shopping", "Health"));
        priorityBox.setItems(FXCollections.observableArrayList("High", "Medium", "Low"));
        filterCategoryBox.setItems(FXCollections.observableArrayList("School", "Work", "Personal", "Shopping", "Health"));

        // populate form when selecting a row
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

    private void setupCheckColumn() {

        checkColumn.setCellValueFactory(param ->
                new javafx.beans.property.SimpleBooleanProperty(param.getValue().isCompleted())
        );

        checkColumn.setCellFactory(col -> new TableCell<Task, Boolean>() {

            private final CheckBox checkBox = new CheckBox();

            {
                // default behavior (square checkbox)
                checkBox.setOnAction(event -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    Task task = getTableView().getItems().get(idx);
                    task.setCompleted(checkBox.isSelected());
                    saveToCurrentOrDefault();
                    taskTable.refresh();
                });
            }

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(value != null && value);
                    setGraphic(checkBox);
                }
            }
        });
    }

    // CRUD
    @FXML
    private void addTask() {
        Task task = new Task(
                titleField.getText(),
                descField.getText(),
                datePicker.getValue(),
                categoryBox.getValue(),
                priorityBox.getValue(),
                false // new tasks start incomplete
        );

        taskList.add(task);
        saveToCurrentOrDefault();
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
        saveToCurrentOrDefault();
        taskTable.refresh();
    }

    // Search / Filter
    @FXML
    private void searchTasks() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        if (keyword.isEmpty()) {
            taskTable.setItems(taskList);
            return;
        }

        taskTable.setItems(taskList.filtered(
                t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(keyword)) ||
                     (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword))
        ));
    }

    @FXML
    private void filterCategory() {
        String category = filterCategoryBox.getValue();

        if (category == null || category.isEmpty()) {
            taskTable.setItems(taskList);
            return;
        }

        taskTable.setItems(taskList.filtered(t -> category.equals(t.getCategory())));
    }

    // File handling
    @FXML
    private void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open tasks file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File selected = chooser.showOpenDialog(getStage());
        if (selected == null) return;

        try (FileReader reader = new FileReader(selected)) {
            Type listType = new TypeToken<List<Task>>(){}.getType();
            List<Task> loaded = gson.fromJson(reader, listType);

            taskList.clear();
            if (loaded != null) taskList.addAll(loaded);

            currentFile = selected;
            taskTable.setItems(taskList);

        } catch (Exception e) {
            showError("Open failed", e.getMessage());
        }
    }

    @FXML
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            writeTasksToFile(currentFile);
        }
    }

    @FXML
    private void saveFileAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save tasks as");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File selected = chooser.showSaveDialog(getStage());
        if (selected == null) return;

        if (!selected.getName().toLowerCase().endsWith(".json")) {
            selected = new File(selected.getAbsolutePath() + ".json");
        }

        currentFile = selected;
        writeTasksToFile(selected);
    }

    private void writeTasksToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(taskList, writer);
        } catch (Exception e) {
            showError("Save failed", e.getMessage());
        }
    }

    private void saveToCurrentOrDefault() {
        if (currentFile == null)
            Storage.saveTasks(taskList);
        else
            writeTasksToFile(currentFile);
    }

    // PDF export
    @FXML
    private void exportPDF() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;

        if (job.showPrintDialog(getStage())) {
            if (job.printPage(taskTable)) job.endJob();
        }
    }

    // Modals
    @FXML
    private void showAbout() {
        openModal("/sample/about.fxml", "About");
    }

    @FXML
    private void showDetails() {
        openModal("/sample/details.fxml", "Details");
    }

    private void openModal(String resource, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Scene scene = new Scene(loader.load());

            Stage st = new Stage();
            st.setTitle(title);
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(scene);
            st.showAndWait();

        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    // Helpers
    private Stage getStage() {
        return (Stage) taskTable.getScene().getWindow();
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
        a.showAndWait();
    }
}
