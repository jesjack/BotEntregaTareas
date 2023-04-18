package com.example.bot;

import com.example.study.Estudiante;
import com.example.study.Tarea;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.*;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.Attachment;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.Color;
import javafx.util.Callback;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class TestDiscordBot {

    private static final Map<String, Command> commands = new HashMap<>();
    private static final ArrayList<SlashCommand> slashCommands = new ArrayList<>();

    static {
        commands.put("ping", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("Pong!"))
                .then());

        slashCommands.add(new SlashCommand("ping", "Ping the bot", new SlashCommandOption[0],
                event -> event.reply("Pong!")));
        slashCommands.add(new SlashCommand("file", "Test file option", new SlashCommandOption[] {
                new SlashCommandOption(
                        "file",
                        "File to upload",
                        ApplicationCommandOption.Type.ATTACHMENT.getValue(),
                        true)
        }, event -> {
            String attachmentsInfo = event.getInteraction().getCommandInteraction()
                    .flatMap(ApplicationCommandInteraction::getResolved)
                    .map(resolved -> resolved.getAttachments()
                            .values()
                            .stream()
                            .map(Attachment::toString)
                            .collect(Collectors.joining("\n")))
                    .orElse("Command run without attachments");

            Attachment attachment = event.getInteraction().getCommandInteraction()
                    .flatMap(ApplicationCommandInteraction::getResolved).flatMap(resolved -> resolved.getAttachments()
                            .values()
                            .stream()
                            .findFirst())
                    .orElse(null);

            if (attachment != null) {
                System.out.println("attachment.getFilename() = " + attachment.getFilename());
            }

            return event.reply()
                    .withContent("Thanks for submitting your report:\n" + attachmentsInfo);
        }));
        slashCommands.add(new SlashCommand("registrarse", "Registrate como estudiante para las tareas asignadas.", new SlashCommandOption[0],
                event -> {
                    long id = event.getInteraction().getUser().getId().asLong();

                    if (Estudiante.exists(id)) {
                        try {
                            Estudiante estudiante = new Estudiante(id);
                            return event.reply("Ya estas registrado como estudiante, pero aquí están tus datos: ```" + estudiante + "```");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    return event.presentModal(InteractionPresentModalSpec.builder()
                            .title("Formulario de registro")
                            .customId("register")
                            .addComponent(ActionRow.of(TextInput.small("nombre", "Nombre", "Escribe tu nombre")))
                            .addComponent(ActionRow.of(TextInput.small("apellido", "Apellido", "Escribe tu apellido")))
                            .addComponent(ActionRow.of(TextInput.small("controlNumber", "Numero de control", "Escribe tu numero de control")))
                            .build());
                }));
        slashCommands.add(new SlashCommand("entregar", "Entrega una tarea", new SlashCommandOption[]{
                new SlashCommandOption(
                        "tarea",
                        "El nombre de la tarea",
                        ApplicationCommandOption.Type.STRING.getValue(),
                        true, event -> {
                    String typed = event.getOption("tarea").flatMap(ApplicationCommandInteractionOption::getValue).map(Object::toString).orElse("");
//                          use Tarea.loadAll method, thats is a thread and hava a callback
                    ArrayList<Tarea> tareas = new ArrayList<>();
                    try {
                        Tarea.loadAll( tareas1 -> {
                            tareas.addAll(tareas1);
                            return null;
                        }).join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    List<ApplicationCommandOptionChoiceData> sugestions = new ArrayList<>();
                    for (Tarea tarea : tareas) {
                        sugestions.add(ApplicationCommandOptionChoiceData.builder()
                                .name(tarea.getNombre())
                                .value(tarea.getNombre())
                                .build());
                    }
                    return event.respondWithSuggestions(sugestions);
                }),
                new SlashCommandOption(
                        "archivo",
                        "El archivo de la tarea",
                        ApplicationCommandOption.Type.ATTACHMENT.getValue(),
                        true)
        }, event -> {
            String tarea = event.getOption("tarea")
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .orElse("");

            if (tarea.isEmpty()) {
                return event.reply("No se encontro la tarea");
            }

            Attachment attachment = event.getInteraction().getCommandInteraction()
                    .flatMap(ApplicationCommandInteraction::getResolved).flatMap(resolved -> resolved.getAttachments()
                            .values()
                            .stream()
                            .findFirst())
                    .orElse(null);

            if (attachment == null) {
                return event.reply("No se encontro ningun archivo");
            }

            Tarea tarea1;
            try {
                tarea1 = new Tarea(tarea);
            } catch (InterruptedException e) {
                return event.reply("No se encontro la tarea");
            }

            long id = event.getInteraction().getUser().getId().asLong();
            if (!Estudiante.exists(id)) {
                return event.reply("No estas registrado como estudiante");
            }

//            try {
//                Estudiante estudiante = new Estudiante(id);
////                tarea1.addEntrega(File, long)
//            } catch (InterruptedException e) {
//                return event.reply("No se pudo entregar la tarea");
//            }

//            convert attachment to file, download it
            String URL_ATTACHMENT = attachment.getUrl();
            String FILENAME = attachment.getFilename();
            File file;
            try {
                InputStream in = new URL(URL_ATTACHMENT).openStream();
                Files.copy(in, Paths.get(FILENAME), StandardCopyOption.REPLACE_EXISTING);
                file = new File(FILENAME);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            tarea1.addEntrega(file, id);

            return event.reply("Tarea entregada");
        }));
    }

    final GatewayDiscordClient client;
    public final Mono<Void> onDisconnect;

    public TestDiscordBot(String token, Callback<Double, Void> loadProgress) {
        client = DiscordClientBuilder.create(token)
            .build()
            .login()
            .block();

        loadProgress.call(0.1);

        assert client != null;

        client.getEventDispatcher().on(MessageCreateEvent.class)
                // 3.1 Message.getContent() is a String
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                        .flatMap(content -> Flux.fromIterable(commands.entrySet())
                                // We will be using ! as our "prefix" to any command in the system.
                                .filter(entry -> content.startsWith('!' + entry.getKey()))
                                .flatMap(entry -> entry.getValue().execute(event))
                                .next()))
                .subscribe();

        loadProgress.call(0.2);

        for (SlashCommand command : slashCommands) {
            command.addCommandToClient(client);
            loadProgress.call(0.2 + 0.8 * slashCommands.indexOf(command) / slashCommands.size());
        }

        client.getEventDispatcher().on(ChatInputInteractionEvent.class)
                .flatMap(event -> Mono.just(event.getCommandName())
                        .flatMap(commandName -> Flux.fromIterable(slashCommands)
                                .filter(command -> command.name.equals(commandName))
                                .flatMap(command -> command.action.execute(event))
                                .next()))
                .subscribe();

//        SlashCommandOption autocomplete events
        var options = new ArrayList<SlashCommandOption>();
        for (SlashCommand command : slashCommands) {
            options.addAll(Arrays.asList(command.options));
        }
        client.getEventDispatcher().on(ChatInputAutoCompleteEvent.class)
                .flatMap(event -> Mono.just(event.getCommandName())
                        .flatMap(commandName -> Flux.fromIterable(slashCommands)
                                .filter(command -> command.name.equals(commandName))
                                .flatMap(command -> {
                                    for (SlashCommandOption option : command.options) {
                                        if (option.name.equals(event.getFocusedOption().getName())) {
                                            return option.completion.execute(event);
                                        }
                                    }
                                    return Mono.empty();
                                })
                                .next()))
                .subscribe();

        loadProgress.call(0.9);

        client.getEventDispatcher().on(ModalSubmitInteractionEvent.class).flatMap(event -> {
            System.out.println("event.getCustomId() = " + event.getCustomId());
            String customId = event.getCustomId();
            if (customId.equals("register")) {

                long id = event.getInteraction().getUser().getId().asLong();

                if (Estudiante.exists(id)) {
                    try {
                        Estudiante estudiante = new Estudiante(id);
                        return event.reply("Ya estas registrado, pero aquí están tus datos: ```" + estudiante + "```");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                var inputs = event.getComponents(TextInput.class);
                TextInput nombre = inputs.stream().filter(input -> input.getCustomId().equals("nombre")).findFirst().orElse(null);
                TextInput apellido = inputs.stream().filter(input -> input.getCustomId().equals("apellido")).findFirst().orElse(null);
                TextInput controlNumber = inputs.stream().filter(input -> input.getCustomId().equals("controlNumber")).findFirst().orElse(null);

                if (nombre == null || apellido == null || controlNumber == null) {
                    return event.reply("Error al registrar, intenta de nuevo.");
                }

                String nombreValue = nombre.getValue().orElse("");
                String apellidoValue = apellido.getValue().orElse("");
                String controlNumberValue = controlNumber.getValue().orElse("");

                if (nombreValue.isEmpty() || apellidoValue.isEmpty() || controlNumberValue.isEmpty()) {
                    return event.reply("Error al registrar, intenta de nuevo.");
                }

                int controlNumberInt = Integer.parseInt(controlNumberValue);

                if (controlNumberInt < 0) {
                    return event.reply("Numero de control invalido.");
                }

                Estudiante estudiante = new Estudiante(nombreValue, apellidoValue, controlNumberInt, id);

                try {
                    estudiante.save().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return event.reply("Ocurrió un error al intentar registrar el estudiante... + " + e.getMessage());
                }

                return event.reply("Registro exitoso!");
            } else {
                return event.reply("Aún no se ha implementado esta funcionalidad.");
            }
        }).subscribe();

        this.onDisconnect = client.onDisconnect();

        System.out.println("Logged in!");
        loadProgress.call(1.0);
    }

    public Thread notifyTarea(Tarea tarea) { // notificar a todos los estudiantes registrados sobre esa tarea.
        Thread thread = new Thread(() -> {
            Thread loading = Estudiante.loadAll(estudiantes -> {
                estudiantes.forEach(estudiante -> {
                    try {
                        estudiante.notifyTarea(tarea, this.client).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                return null;
            });

            try {
                loading.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Tarea-" + tarea.nombre);
        thread.start();
        return thread;
    }

    public static void main(String[] args) {
        new TestDiscordBot(args[0], (progress) -> {
            System.out.println("progress = " + progress);
            return null;
        });
    }

}
