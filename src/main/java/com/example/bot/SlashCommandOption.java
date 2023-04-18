package com.example.bot;

import discord4j.discordjson.json.ApplicationCommandOptionData;

public class SlashCommandOption {
    public boolean autoCompletion;
    public String name;
    public String description;
    public int type;
    public boolean required;
    public SlashCommandOptionCompletion completion;

    public SlashCommandOption(String name, String description, int type, boolean required) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
    }

    public SlashCommandOption(String name, String description, int type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = false;
    }

    public SlashCommandOption(String name, String description, int type, SlashCommandOptionCompletion completion) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = false;
        this.completion = completion;
        this.autoCompletion = true;
    }

    public SlashCommandOption(String name, String description, int type, boolean required, SlashCommandOptionCompletion completion) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.completion = completion;
        this.autoCompletion = true;
    }

    public ApplicationCommandOptionData optionRequest() {
        return ApplicationCommandOptionData.builder()
                .name(name)
                .description(description)
                .type(type)
                .required(required)
                .autocomplete(autoCompletion)
                .build();
    }
}
