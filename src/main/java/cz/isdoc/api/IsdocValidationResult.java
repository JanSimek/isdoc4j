package cz.isdoc.api;

import java.util.List;

/**
 * Outcome of validating an ISDOC document against the 6.0.2 XSD schema.
 * Errors are returned as preformatted strings (level + line/column + message)
 * so callers can log or surface them without re-parsing the SAX exceptions.
 */
public final class IsdocValidationResult {

    private final boolean valid;
    private final List<String> errors;

    public IsdocValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = List.copyOf(errors);
    }

    public static IsdocValidationResult valid() {
        return new IsdocValidationResult(true, List.of());
    }

    public static IsdocValidationResult invalid(List<String> errors) {
        return new IsdocValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return valid
                ? "ISDOC document is valid"
                : "ISDOC document is invalid. Errors: " + String.join("; ", errors);
    }
}
