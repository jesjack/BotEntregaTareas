package com.example.bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class SlashCommand {
    public String name;
    public String description;
    public SlashCommandOption[] options;
    public SlashCommandEvent action;

    public SlashCommand(String name, String description, SlashCommandOption[] options, SlashCommandEvent action) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.action = action;
    }

    public SlashCommand(String name, String description, SlashCommandOption[] options, SlashCommandEvent action, boolean autoCompletion) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.action = action;
    }

    public ApplicationCommandRequest commandRequest() {
        var command = ApplicationCommandRequest.builder()
                .name(name)
                .description(description);
        for (var option : options) {
            command.addOption(option.optionRequest());
        }
        return command.build();
    }

    public void addCommandToClient(GatewayDiscordClient client) {
        long applicationId = client.getRestClient().getApplicationId().block();
        ApplicationCommandRequest request = commandRequest();
        client.getRestClient().getApplicationService()
                .createGlobalApplicationCommand(applicationId, request)
                .block();
    }
}
