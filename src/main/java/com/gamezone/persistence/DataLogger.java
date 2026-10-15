package com.gamezone.persistence;

/**
 * Receives the warning messages produced by the persistence layer.
 */
public interface DataLogger {

    /**
     * Reports a warning message.
     *
     * @param message the message to report
     */
    void warn(String message);
}