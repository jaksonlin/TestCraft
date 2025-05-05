package com.github.jaksonlin.testcraft.infrastructure.commands;


public class CommandCancellationException extends Exception{

    public CommandCancellationException(String message) {
        super(message);
    }
}