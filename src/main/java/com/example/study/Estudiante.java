package com.example.study;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Estudiante {
    public String name;
    public String lastName;
    public int controlNumber;
    public long id;

    public Estudiante(String name, String lastName, int controlNumber, long id) {
        this.name = name;
        this.lastName = lastName;
        this.controlNumber = controlNumber;
        this.id = id;
    }

    public Thread save() {
        Thread thread = new Thread(() -> {
            try {
                Files.createDirectories(Path.of("data/estudiantes"));
                String data = name + "," + lastName + "," + controlNumber + "," + id;
                Files.writeString(Path.of("data/estudiantes/" + id + ".txt"), data);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al guardar el estudiante");
            }
        });
        thread.start();
        return thread;
    }

    public static Thread delete(long id) {
        Thread thread = new Thread(() -> {
            try {
                Files.delete(Path.of("data/estudiantes/" + id + ".txt"));
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al eliminar el estudiante");
            }
        });
        thread.start();
        return thread;
    }

    public static Thread load(long id, Callback<Estudiante, Void> callback) {
        Thread thread = new Thread(() -> {
            try {
                String data = Files.readString(Path.of("data/estudiantes/" + id + ".txt"));
                String[] dataSplit = data.split(",");
                Estudiante estudiante = new Estudiante(dataSplit[0], dataSplit[1], Integer.parseInt(dataSplit[2]), Long.parseLong(dataSplit[3]));
                callback.call(estudiante);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al cargar el estudiante");
            }
        }, "loading_estudiante_" + id);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return thread;
    }

    public Estudiante(long id) throws InterruptedException {
        Thread thread = Estudiante.load(id, estudiante -> {
            this.name = estudiante.name;
            this.lastName = estudiante.lastName;
            this.controlNumber = estudiante.controlNumber;
            this.id = estudiante.id;
            return null;
        });
        thread.join();
    }

    public static Thread loadAll(Callback<ArrayList<Estudiante>, Void> callback) { // using load method
        Thread thread = new Thread(() -> {
            try {
                ArrayList<Estudiante> estudiantes = new ArrayList<>();
                Files.list(Path.of("data/estudiantes")).forEach(path -> {
                    String pathString = path.toString();
                    // normalize pathString
                    pathString = pathString.replace("\\", "/");
                    String idString = pathString.substring(pathString.lastIndexOf("/") + 1, pathString.lastIndexOf("."));
                    long id = Long.parseLong(idString);
                    Thread thread1 = Estudiante.load(id, estudiante -> {
                        estudiantes.add(estudiante);
                        return null;
                    });
                    try {
                        thread1.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                callback.call(estudiantes);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al cargar los estudiantes");
            }
        }, "loading_estudiantes");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public static boolean exists(long id) {
        return Files.exists(Path.of("data/estudiantes/" + id + ".txt"));
    }

    public String toString() {
        return "Estudiante{\n" +
                "\tname='" + name + "'\n" +
                "\tlastName='" + lastName + "'\n" +
                "\tcontrolNumber=" + controlNumber + "\n" +
                "\tid=" + id + "\n" +
                '}';
    }

    public Thread notifyTarea(Tarea tarea, GatewayDiscordClient client) {
        Thread thread = new Thread(() -> {
            Snowflake snowflake = Snowflake.of(id);
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(Color.MAGENTA)
                    .title("Tarea creada")
                    .description("Aquí está la información de tu tarea:")
                    .addField("Nombre", tarea.nombre, true)
                    .addField("Fecha de término", tarea.fechaTermino.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true)
                    .addField("Descripción", tarea.descripcion, false)
                    .addField("Días restantes", String.valueOf(-tarea.fechaTermino.until(LocalDateTime.now(), ChronoUnit.DAYS)), true)
                    .build();
            PrivateChannel privateChannel = client.getUserById(snowflake).block().getPrivateChannel().block();
            privateChannel.createMessage(embed).block();

        }, "notifying_tarea_" + tarea.nombre + "_to_" + id);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getControlNumber() {
        return controlNumber;
    }

    public void setControlNumber(int controlNumber) {
        this.controlNumber = controlNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
