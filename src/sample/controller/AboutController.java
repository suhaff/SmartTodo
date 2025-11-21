package sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class AboutController {

    @FXML private TextArea aboutText;

    @FXML
    public void initialize() {
        // Detailed description (Version B — more professional)
        String text = "SmartTodo\n\n" +
                "Team:\n" +
                " - Numaan Suhaff — Lead Developer (UI & Core Logic)\n" +
                " - Luqman Hyder — Feature Developer (Filtering, Search, & UX improvements)\n" +
                " - Raza Momnin — Storage & Data Handling (JSON persistence, file operations)\n\n" +
                "Contributions:\n" +
                "Numaan led the project architecture, FXML layout updates, and core controller logic.\n" +
                "Luqman implemented the search and filtering UI and improved the table UX.\n" +
                "Raza implemented storage, file I/O, and helped wire up export/save functionality.\n\n" +
                "Thanks for using SmartTodo.\n";
        aboutText.setText(text);

        // disable caret / selection
        aboutText.setEditable(false);
    }

    @FXML
    private void close() {
        Stage st = (Stage) aboutText.getScene().getWindow();
        st.close();
    }
}
