package com.gamezone.persistence;

/**
 * Logger that prints warnings to the console.
 */
public class ConsoleDataLogger implements DataLogger {

    /**
     * Prints the message to the standard output.
     *
     * @param message the message to print
     */
    @Override
    public void warn(String message) {
        System.out.println(message);
    }
}