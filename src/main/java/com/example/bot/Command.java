package com.example.bot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface Command {
    /**
     * // Since we are expecting to do reactive things in this method, like
     * // send a message, then this method will also return a reactive type.
     * @param event The event that triggered this command.
     * @return A Mono that completes when the command is done executing.
     */
    Mono<Void> execute(MessageCreateEvent event);
}
