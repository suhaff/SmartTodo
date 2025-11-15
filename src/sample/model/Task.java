package sample.model;

import java.time.LocalDate;

public class Task {
    private String title;
    private String description;
    private LocalDate dueDate;
    private String category;
    private String priority;
    private boolean completed;

    public Task(String title, String description, LocalDate dueDate,
                String category, String priority, boolean completed) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.priority = priority;
        this.completed = completed;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getCategory() { return category; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return completed; }

    public void setTitle(String s) { title = s; }
    public void setDescription(String s) { description = s; }
    public void setDueDate(LocalDate d) { dueDate = d; }
    public void setCategory(String s) { category = s; }
    public void setPriority(String s) { priority = s; }
    public void setCompleted(boolean c) { completed = c; }
}
