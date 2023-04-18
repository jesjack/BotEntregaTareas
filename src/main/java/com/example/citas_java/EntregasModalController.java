package com.example.citas_java;

import com.example.study.Entrega;
import com.example.study.Estudiante;
import com.example.study.Tarea;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.util.Duration;

public class EntregasModalController {
    public Label title;
    public TableView<Entrega> tableEntregas;

    @FXML
    public void initialize() {
        tableEntregas.setItems(FXCollections.observableArrayList());

        final int DELAY = 200; // milliseconds
        Timeline clickTimer = new Timeline(new KeyFrame(Duration.millis(DELAY), event -> {
//            expira temporizador
        }));
        clickTimer.setCycleCount(1);

        tableEntregas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Si es un clic simple
                clickTimer.playFromStart(); // Reinicia el temporizador
            } else if (event.getClickCount() == 2) {
                clickTimer.stop(); // Detiene el temporizador

                Entrega entregaSeleccionada = tableEntregas.getSelectionModel().getSelectedItem();

                entregaSeleccionada.openFile();
            }
        });
    }

    public void loadEntregas(Tarea tarea) {
        title.setText("Entregas de " + tarea.nombre);
        Entrega.loadAll(tarea, entregas -> {
            tableEntregas.getItems().addAll(entregas);
            return null;
        });
    }

    public void loadEntregas(Estudiante estudiante) {
        title.setText("Entregas de " + estudiante.name);
        Entrega.loadAll(estudiante, entregas -> {
            tableEntregas.getItems().addAll(entregas);
            return null;
        });
    }
}
