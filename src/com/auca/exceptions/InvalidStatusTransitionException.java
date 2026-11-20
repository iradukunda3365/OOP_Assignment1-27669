package com.auca.exceptions;

public class InvalidStatusTransitionException extends ValidationException {
    public InvalidStatusTransitionException(String message) { super(message); }
}
