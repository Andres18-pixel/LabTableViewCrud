package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tuempresa/labtablecrud/main-view.fxml"));

        if (fxmlLoader.getLocation() == null) {
            throw new RuntimeException("No se encontró el archivo FXML en /com/tuempresa/labtablecrud/main-view.fxml");
        }

        Scene scene = new Scene(fxmlLoader.load(), 780, 560);
        stage.setTitle("Catálogo de Productos - Papelería Campus");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}