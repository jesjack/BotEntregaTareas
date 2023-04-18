package com.example.bot;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface SlashCommandEvent {
    Mono<Void> execute(ChatInputInteractionEvent event);
}
