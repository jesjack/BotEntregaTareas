package com.example.citas_java;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoadingModalController {

    private static final String COLOR_THREAD_NAME = "color";
    private static final String PROGRESS_THREAD_NAME = "progress";

    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label prompt;
    public VBox buttonContainer;

    @FXML
    public void initialize() {
//        var color = new Thread(this::changeColor, COLOR_THREAD_NAME);
//        var progress = new Thread(this::changeProgress, PROGRESS_THREAD_NAME);
//
//        progress.start();
//        color.start();
    }

    private void changeProgress() {
        for (int i = 0; i <= 100; i++) {
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(progressBar.progressProperty(), i / 100.0);
            KeyFrame kf = new KeyFrame(Duration.millis(100), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
            timeline.setOnFinished(event -> {
                synchronized (this) {
                    notify();
                }
            });
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
//        color.interrupt();
        Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().equals(COLOR_THREAD_NAME))
                .forEach(Thread::interrupt);
    }

    private void changeColor() {
        while (true) {
            var r = (int) (Math.random() * 255);
            var g = (int) (Math.random() * 255);
            var b = (int) (Math.random() * 255);
            progressBar.setStyle("-fx-accent: rgb(" + r + "," + g + "," + b + ");");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void setProgress(double progress) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(progressBar.progressProperty(), progress);
        KeyFrame kf = new KeyFrame(Duration.millis(100), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();

        int[] rgb = new int[] {
                (int) (Math.random() * 255),
                (int) (Math.random() * 255),
                (int) (Math.random() * 255)};

        progressBar.setStyle("-fx-accent: rgb(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ");");
    }

    public void closeModal() {
        Stage stage = (Stage) progressBar.getScene().getWindow();
        stage.close();
    }

    public Thread setProgressT(double progress) {
        if (progress < 0 || progress > 1) {
            throw new IllegalArgumentException("Progress must be between 0 and 1");
        }

        Thread thread = new Thread(() -> {

            int[] rgb = new int[] {
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255)};

            progressBar.setStyle("-fx-accent: rgb(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ");");

            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(progressBar.progressProperty(), progress);
            KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
            timeline.getKeyFrames().add(kf);

            new Thread(timeline::play).start();

            timeline.setOnFinished(event -> {
                synchronized (this) {
                    notify();
                }
            });
            if (timeline.getStatus() == Timeline.Status.RUNNING) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (progress == 1)
                Platform.runLater(this::closeModal);
        }, PROGRESS_THREAD_NAME);
        thread.start();
        return thread;
    }

    public void setError(String error) {
        prompt.setText(error);
        progressBar.setStyle("-fx-accent: rgb(255,0,0);");

//        put button to close program
        Button button = new Button("Cerrar");
        button.setOnAction(event -> System.exit(0));
        buttonContainer.getChildren().add(button);
    }
}
