package ru.killer666.issuetimewatchdog;

class NoLoginCredentialsException extends RuntimeException {
    NoLoginCredentialsException() {
        super("Set login credentials first!");
    }
}
