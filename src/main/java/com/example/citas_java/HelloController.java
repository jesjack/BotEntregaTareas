package com.example.citas_java;

import com.example.bot.TestDiscordBot;
import com.example.study.Estudiante;
import com.example.study.Tarea;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloController {
    @FXML
    public TableView<Tarea> tableTasks;
    @FXML
    public TableView<Estudiante> tableAlumnos;
    @FXML
    public TableView<Thread> hilosActivos;
    Stage primaryStage;

    TestDiscordBot bot;

    public void setPrimaryStageAndSetup(Stage primaryStage, String discordToken) {
        this.primaryStage = primaryStage;
        try {
            showProgressBar();
            updateProgressBar(0.05);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try {
                bot = new TestDiscordBot(discordToken, this::updateProgressBar);
            } catch (Exception e) {
                e.printStackTrace();
                String message = e.getMessage();
                if (message == null) {
                    message = e.getClass().getName();
                }
                String finalMessage = message;
                Platform.runLater(() -> showError("Quizás tu institución actual tiene bloqueado discord."));
            }
        }).start();
    }

    @FXML
    public void initialize() {
        ObservableList<Tarea> tareas = FXCollections.observableArrayList();
        Tarea.loadAll(tareasArrayList -> {
            tareas.addAll(tareasArrayList);
            tableTasks.setItems(tareas);
            return null;
        });

        ObservableList<Estudiante> alumnos = FXCollections.observableArrayList();
        Estudiante.loadAll(alumnosArrayList -> {
            alumnos.addAll(alumnosArrayList);
            tableAlumnos.setItems(alumnos);
            return null;
        });

        hilosActivos.setItems(FXCollections.observableArrayList());
        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        scheduled.scheduleAtFixedRate(this::actualizarTablaHilos, 0, 1, TimeUnit.SECONDS);

        final int DELAY = 200; // milliseconds
        Timeline clickTimer = new Timeline(new KeyFrame(Duration.millis(DELAY), event -> {
//            expira temporizador
        }));
        clickTimer.setCycleCount(1);

        tableAlumnos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Si es un clic simple
                clickTimer.playFromStart(); // Reinicia el temporizador
            } else if (event.getClickCount() == 2) { // Si es un doble clic
                clickTimer.stop(); // Detiene el temporizador
                // Obtiene los datos de la fila seleccionada
                Estudiante alumnoSeleccionado = tableAlumnos.getSelectionModel().getSelectedItem();

                // Haz lo que quieras con los datos, por ejemplo, mostrarlos en un cuadro de diálogo
                try {
                    showEntregasModal(alumnoSeleccionado);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        tableTasks.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Si es un clic simple
                clickTimer.playFromStart(); // Reinicia el temporizador
            } else if (event.getClickCount() == 2) { // Si es un doble clic
                clickTimer.stop(); // Detiene el temporizador
                // Obtiene los datos de la fila seleccionada
                Tarea tareaSeleccionada = tableTasks.getSelectionModel().getSelectedItem();

                // Haz lo que quieras con los datos, por ejemplo, mostrarlos en un cuadro de diálogo
                try {
                    showEntregasModal(tareaSeleccionada);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // Método para actualizar la tabla con los hilos activos
    private void actualizarTablaHilos() {
        Platform.runLater(() -> {
            hilosActivos.getItems().removeIf(thread -> thread.getState() == Thread.State.TERMINATED);
            Map<Thread, StackTraceElement[]> threadsMap = Thread.getAllStackTraces();
            for (Thread thread : threadsMap.keySet()) {
                if (thread.getState() != Thread.State.TERMINATED && !hilosActivos.getItems().contains(thread)) {
                    hilosActivos.getItems().add(thread);
                }
            }
        });
    }

    LoadingModalController controller;
    @FXML
    public void showProgressBar() throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Progress Bar");
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loading-modal.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 330, 150);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL); //Establecer modalidad
        stage.initOwner(primaryStage); //Establecer ventana padre
        stage.setOnCloseRequest(Event::consume); //Interceptar evento de cierre
        stage.show();

        controller = fxmlLoader.getController();
    }

    public void showEntregasModal(Tarea tarea) throws IOException {
        EntregasModalController controller = getEntregasModalController();
        controller.loadEntregas(tarea);
    }

    public void showEntregasModal(Estudiante estudiante) throws IOException {
        EntregasModalController controller = getEntregasModalController();
        controller.loadEntregas(estudiante);
    }

    private EntregasModalController getEntregasModalController() throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Entregas");
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("entregas-modal.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 330, 150);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL); //Establecer modalidad
        stage.initOwner(primaryStage); //Establecer ventana padre
//        stage.setOnCloseRequest(Event::consume); //Interceptar evento de cierre
        stage.show();

        EntregasModalController controller = fxmlLoader.getController();
        return controller;
    }

    public Void updateProgressBar(double progress) {
        controller.setProgressT(progress);
        return null;
    }

    public void showError(String message) {
        controller.setError(message);
    }

    public void addTask(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Agregar tarea");
        dialog.setHeaderText(null);
        dialog.setContentText("Por favor ingrese el nombre de la tarea:");

// Obtener la respuesta del usuario
        Optional<String> resultTarea = dialog.showAndWait();

// Comprobar si el usuario ingresó un nombre de tarea
        if (!resultTarea.isPresent()) {
            return;
        }

        String tarea = resultTarea.get();

        // Crear un cuadro de texto multilínea
        TextArea textArea = new TextArea();
        textArea.setWrapText(true); // Ajustar el texto para que se ajuste a la ventana
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

// Crear la alerta que contiene el cuadro de texto multilínea
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Agregar tarea");
        alert.setHeaderText("Ingrese la descripción de la tarea:");
        alert.getDialogPane().setContent(textArea);

// Mostrar la alerta y esperar a que el usuario haga clic en OK o Cancelar
        ButtonType buttonTypeOK = new ButtonType("Agregar");
        ButtonType buttonTypeCancel = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

// Comprobar si el usuario hizo clic en OK
        if (result != buttonTypeOK) {
            return;
        }

        String descripcion = textArea.getText();

        // fecha, una semana de plazo
        LocalDateTime fecha = LocalDateTime.now().plusWeeks(1);

        Tarea tareaNueva = new Tarea(tarea, descripcion, fecha);

        tableTasks.getItems().add(tareaNueva);
        tareaNueva.save();

        bot.notifyTarea(tareaNueva);
    }
}