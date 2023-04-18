module com.example.citas_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires discord4j.core;
    requires discord4j.common;
    requires discord4j.gateway;
    requires discord4j.rest;
    requires discord4j.discordjson;
    requires discord4j.store.api;
    requires discord4j.store.jdk;
    requires discord4j.voice;
    requires discord4j.discordjson.api;
    requires reactor.core;
    requires org.reactivestreams;
    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j.simple;


    opens com.example.citas_java to javafx.fxml;
    exports com.example.citas_java;
    exports com.example.study;
}