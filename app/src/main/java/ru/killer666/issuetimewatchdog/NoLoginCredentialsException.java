package ru.killer666.issuetimewatchdog;

public class NoLoginCredentialsException extends RuntimeException {

    public NoLoginCredentialsException() {
        super("Set login credentials first!");
    }

}
