package com.github.jaksonlin.testcraft.commands;


public class CommandCancellationException extends Exception{

    public CommandCancellationException(String message) {
        super(message);
    }
}