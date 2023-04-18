package com.example.study;

import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Entrega {
    public String fileName;
    public long idEstudiante;
    public String tareaName;

    public Entrega(String fileName, long idEstudiante, String tareaName) {
        this.fileName = fileName;
        this.idEstudiante = idEstudiante;
        this.tareaName = tareaName;
    }

    public File getFile() {
        return new File("data/tareas/" + this.tareaName + "/entregas/" + this.idEstudiante + "/" + this.fileName);
    }

    public Tarea getTarea() {
        try {
            return new Tarea(this.tareaName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Estudiante getEstudiante() {
        try {
            return new Estudiante(this.idEstudiante);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getEstudianteNombre() {
        try {
            return new Estudiante(this.idEstudiante).name;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Thread loadAll(Estudiante estudiante, Tarea tarea, Callback<ArrayList<Entrega>, Void> callback) {
        Thread thread = new Thread(() -> {
            try {
                File[] files = new File("data/tareas/" + tarea.nombre + "/entregas/" + estudiante.id).listFiles();
                ArrayList<Entrega> entregas = new ArrayList<>();
                if (files != null) {
                    for (File file : files) {
                        entregas.add(new Entrega(file.getName(), estudiante.id, tarea.nombre));
                    }
                }
                callback.call(entregas);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error al cargar las entregas");
            }
        }, "loadEntregas_" + tarea.nombre + "_" + estudiante.id);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public static Thread loadAll(Tarea tarea, Callback<ArrayList<Entrega>, Void> callback) {
        Thread thread = new Thread(() -> {
            try {
                File[] files = new File("data/tareas/" + tarea.nombre + "/entregas").listFiles();
                ArrayList<Entrega> entregas = new ArrayList<>();
                if (files != null) {
                    for (File file : files) {
                        File[] files2 = file.listFiles();
                        if (files2 != null) {
                            for (File file2 : files2) {
                                entregas.add(new Entrega(file2.getName(), Long.parseLong(file.getName()), tarea.nombre));
                            }
                        }
                    }
                }
                callback.call(entregas);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error al cargar las entregas");
            }
        }, "loadEntregas_" + tarea.nombre);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public static Thread loadAll(Estudiante estudiante, Callback<ArrayList<Entrega>, Void> callback) {
        Thread thread = new Thread(() -> {
            try {
                File[] files = new File("data/tareas").listFiles();
                ArrayList<Entrega> entregas = new ArrayList<>();
                if (files != null) {
                    for (File file : files) {
                        File[] files2 = new File("data/tareas/" + file.getName() + "/entregas/" + estudiante.id).listFiles();
                        if (files2 != null) {
                            for (File file2 : files2) {
                                entregas.add(new Entrega(file2.getName(), estudiante.id, file.getName()));
                            }
                        }
                    }
                }
                callback.call(entregas);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error al cargar las entregas");
            }
        }, "loadEntregas_" + estudiante.id);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public static Thread loadAll(Callback<ArrayList<Entrega>, Void> callback) {
        Thread thread = new Thread(() -> {
            try {
                File[] files = new File("data/tareas").listFiles();
                ArrayList<Entrega> entregas = new ArrayList<>();
                if (files != null) {
                    for (File file : files) {
                        File[] files2 = new File("data/tareas/" + file.getName() + "/entregas").listFiles();
                        if (files2 != null) {
                            for (File file2 : files2) {
                                File[] files3 = file2.listFiles();
                                if (files3 != null) {
                                    for (File file3 : files3) {
                                        entregas.add(new Entrega(file3.getName(), Long.parseLong(file2.getName()), file.getName()));
                                    }
                                }
                            }
                        }
                    }
                }
                callback.call(entregas);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error al cargar las entregas");
            }
        }, "loadEntregas");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return thread;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(long idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getTareaName() {
        return tareaName;
    }

    public void setTareaName(String tareaName) {
        this.tareaName = tareaName;
    }

    public void openFile() {
        /**
         * Windows
         *
         * Runtime.getRuntime().exec("explorer /select, <file path>")
         * Linux
         *
         * Runtime.getRuntime().exec("xdg-open <file path>");
         * MacOS
         *
         * Runtime.getRuntime().exec("open -R <file path>");
         */

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                Runtime.getRuntime().exec("explorer /select, " + this.getFile().getAbsolutePath());
            } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                Runtime.getRuntime().exec("xdg-open " + this.getFile().getAbsolutePath());
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Runtime.getRuntime().exec("open -R " + this.getFile().getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
