package com.example.study;

import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class Tarea {
    public String nombre;
    public String descripcion;
    public LocalDateTime fechaTermino;

    public Tarea(String nombre, String descripcion, LocalDateTime fechaTermino) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaTermino = fechaTermino;
    }

    public Thread addEntrega(File file, long idEstudiante) {
        Thread thread = new Thread(() -> {
            try {
//                create "data/tareas/" + this.nombre + "/entregas/" + idEstudiante + "/" folder
                Files.createDirectories(Path.of("data/tareas/" + this.nombre + "/entregas/" + idEstudiante));
                Files.move(file.toPath(), Path.of("data/tareas/" + this.nombre + "/entregas/" + idEstudiante + "/" + file.getName()));
                System.out.println("Archivo movido con éxito");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "addEntrega_" + this.nombre + "_" + idEstudiante);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public Thread save() {
        Thread thread = new Thread(() -> {
            try {
                Files.createDirectories(Path.of("data/tareas/" + this.nombre + "/entregas"));
                String data = nombre + "," + descripcion + "," + fechaTermino;
                Files.writeString(Path.of("data/tareas/" + this.nombre + "/tarea.txt"), data);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al guardar la tarea");
            }
        }, "saveTarea_" + this.nombre);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return thread;
    }

    public static Thread load(String nombre, Callback<Tarea, Void> callback) {
        Thread thread = new Thread(() -> {
            try {
                String data = Files.readString(Path.of("data/tareas/" + nombre + "/tarea.txt"));
                String[] dataSplit = data.split(",");
                Tarea tarea = new Tarea(dataSplit[0], dataSplit[1], LocalDateTime.parse(dataSplit[2]));
                callback.call(tarea);
            } catch (IOException e) {
                System.out.println("Error al cargar la tarea");
                throw new RuntimeException(e);
            }
        }, "loadTarea_" + nombre);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return thread;
    }

    public Tarea(String nombre) throws InterruptedException {
        Thread loadThread = load(nombre, tarea -> {
            this.nombre = tarea.nombre;
            this.descripcion = tarea.descripcion;
            this.fechaTermino = tarea.fechaTermino;
            return null;
        });
        loadThread.join();
    }

    public static Thread loadAll(Callback<ArrayList<Tarea>, Void> callback) {
        Thread thread = new Thread(() -> {
            try {
                File[] files = new File("data/tareas").listFiles();
                ArrayList<Tarea> tareas = new ArrayList<>();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            Tarea tarea = new Tarea(file.getName());
                            tareas.add(tarea);
                        }
                    }
                }
                callback.call(tareas);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error al cargar las tareas");
            }
        }, "loadingTareas");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public String toString() {
        return "Tarea{\n" +
                "nombre='" + nombre + '\'' +
                ",\ndescripcion='" + descripcion + '\'' +
                ",\nfechaTermino=" + fechaTermino +
                "\n}";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(LocalDateTime fechaTermino) {
        this.fechaTermino = fechaTermino;
    }
}
