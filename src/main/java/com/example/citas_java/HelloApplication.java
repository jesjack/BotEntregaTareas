package com.example.citas_java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static String token;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 500);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        HelloController controller = fxmlLoader.getController();
        stage.show();
        controller.setPrimaryStageAndSetup(stage, token);
    }

    public static void main(String[] args) {
        token = args[0];
        launch();
    }

}