package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sample.model.Task;
import sample.Storage;

import java.time.LocalDate;

public class TodoController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task,String> titleCol;
    @FXML private TableColumn<Task,String> categoryCol;
    @FXML private TableColumn<Task,String> priorityCol;
    @FXML private TableColumn<Task, LocalDate> dateCol;

    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> priorityBox;
    @FXML private CheckBox completedCheck;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryBox;

    private ObservableList<Task> tasks;

    @FXML
    public void initialize() {

        tasks = FXCollections.observableArrayList(Storage.load());

        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        categoryCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        priorityCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPriority()));
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDueDate()));

        taskTable.setItems(tasks);

        categoryBox.setItems(FXCollections.observableArrayList("Work", "School", "Personal"));
        priorityBox.setItems(FXCollections.observableArrayList("Low", "Medium", "High"));

        filterCategoryBox.setItems(FXCollections.observableArrayList("All", "Work", "School", "Personal"));
        filterCategoryBox.getSelectionModel().select("All");
    }

    @FXML
    public void addTask() {
        Task t = new Task(
                titleField.getText(),
                descField.getText(),
                datePicker.getValue(),
                categoryBox.getValue(),
                priorityBox.getValue(),
                completedCheck.isSelected()
        );

        tasks.add(t);
        Storage.save(tasks);
    }

    @FXML
    public void updateTask() {
        Task t = taskTable.getSelectionModel().getSelectedItem();
        if (t == null) return;

        t.setTitle(titleField.getText());
        t.setDescription(descField.getText());
        t.setDueDate(datePicker.getValue());
        t.setCategory(categoryBox.getValue());
        t.setPriority(priorityBox.getValue());
        t.setCompleted(completedCheck.isSelected());

        taskTable.refresh();
        Storage.save(tasks);
    }

    @FXML
    public void deleteTask() {
        Task t = taskTable.getSelectionModel().getSelectedItem();
        if (t == null) return;

        tasks.remove(t);
        Storage.save(tasks);
    }

    @FXML
    public void markCompleted() {
        Task t = taskTable.getSelectionModel().getSelectedItem();
        if (t == null) return;
        t.setCompleted(true);
        taskTable.refresh();
        Storage.save(tasks);
    }

    @FXML
    public void searchTasks() {
        String keyword = searchField.getText().toLowerCase();

        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task t : tasks) {
            if (t.getTitle().toLowerCase().contains(keyword) ||
                t.getDescription().toLowerCase().contains(keyword)) {
                filtered.add(t);
            }
        }

        taskTable.setItems(filtered);
    }

    @FXML
    public void filterCategory() {
        String selected = filterCategoryBox.getValue();

        if (selected.equals("All")) {
            taskTable.setItems(tasks);
            return;
        }

        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task t : tasks) {
            if (t.getCategory().equals(selected)) {
                filtered.add(t);
            }
        }

        taskTable.setItems(filtered);
    }
}
