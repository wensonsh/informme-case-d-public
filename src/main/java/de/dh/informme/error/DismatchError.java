package de.dh.informme.error;

import lombok.Data;

@Data
public class DismatchError extends Exception {
    public DismatchError(String message) {
        super(message);
    }
}
