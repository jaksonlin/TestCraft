package com.github.jaksonlin.pitestintellij.commands;


public class CommandCancellationException extends Exception{

    public CommandCancellationException(String message) {
        super(message);
    }
}