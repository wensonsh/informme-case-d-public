package de.dh.informme.error;

import lombok.Data;

@Data
public class DuplicatePatientError extends Exception {
    public DuplicatePatientError(String message) {
        super(message);
    }
}
