package org.bahmni.eventrouterservice.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FailedToLoadConfigurationTest {

    @Test
    public void givenPublisherId_whenCreatingException_thenCreateExceptionMessageInExpectedFormat() {
        FailedToLoadConfiguration exception = new FailedToLoadConfiguration("invalid-file.json", new IOException("not able to read file"));
        assertEquals("Failed to load configuration file : invalid-file.json", exception.getMessage());
    }
}