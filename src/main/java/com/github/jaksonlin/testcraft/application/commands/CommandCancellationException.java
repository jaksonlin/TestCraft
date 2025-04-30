package com.github.jaksonlin.testcraft.application.commands;


public class CommandCancellationException extends Exception{

    public CommandCancellationException(String message) {
        super(message);
    }
}