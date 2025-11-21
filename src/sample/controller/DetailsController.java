package sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class DetailsController {

    @FXML private TextArea detailsText;

    @FXML
    public void initialize() {
        String details = ""
                + "SmartTodo — Details / User Guide\n\n"
                + "Buttons and what they do:\n\n"
                + "Add — Adds a new task from the form on the right. New tasks start as Incomplete.\n"
                + "Update — Updates the selected task with the values from the form.\n"
                + "Delete — Deletes the selected task.\n"
                + "Mark Completed — Marks the selected task as Completed (updates the Status column).\n\n"
                + "File menu:\n"
                + "Open... — Open a .json file containing tasks (loads into the table).\n"
                + "Save — Saves tasks to the current file (or default local storage tasks.json if none selected).\n"
                + "Save As... — Save tasks to a chosen file (JSON).\n"
                + "Export to PDF — Open the system print dialog where you can choose 'Save as PDF' if available on your OS.\n"
                + "Close — Closes the application window.\n\n"
                + "Help menu:\n"
                + "About Us — Shows project authors and responsibilities.\n"
                + "Details / User Guide — Shows this screen.\n\n"
                + "Search and Filter:\n"
                + "Search — Searches title and details for the typed keyword.\n"
                + "Filter — Filters the table by the selected category from the dropdown.\n\n"
                + "Notes:\n"
                + "- The application automatically saves to the default 'tasks.json' when you add/update/delete unless you opened a file or used Save As.\n"
                + "- Exporting to PDF uses the system print dialog. Choose a 'Save as PDF' option if your OS offers it.\n"
                + "- The 'Status' column shows 'Incomplete' or 'Completed'. Use the Mark Completed button to set Completed.\n";
        detailsText.setText(details);
        detailsText.setEditable(false);
    }
}
