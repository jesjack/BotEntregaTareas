package com.example.bot;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import reactor.core.publisher.Mono;

public interface SlashCommandOptionCompletion {
    Mono<Void> execute(ChatInputAutoCompleteEvent event);
}
