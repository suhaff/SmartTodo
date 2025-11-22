package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/todo.fxml"));
        Scene scene = new Scene(loader.load());
        // apply single stylesheet (your modern UI)
        scene.getStylesheets().add(getClass().getResource("/sample/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Smart Todo List");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
