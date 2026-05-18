package ui;

import core.Airport;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DesktopApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Airport airport = Airport.getInstance();

        // главное окно
        MainViewController mainController = new MainViewController(airport);
        Scene scene = new Scene(mainController.getRoot(), 900, 600);

        primaryStage.setTitle("Система управления аэропортом");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
