package cz.isdoc.api;

import jakarta.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for processing ISDOC invoice documents.
 * This interface will be implemented once the XSD code generation issues are resolved.
 */
public interface IsdocProcessor {
    
    /**
     * Parse an ISDOC invoice from an InputStream.
     * 
     * @param inputStream The input stream containing the ISDOC XML
     * @return The parsed invoice object
     * @throws JAXBException If parsing fails
     */
    Object parseInvoice(InputStream inputStream) throws JAXBException;
    
    /**
     * Marshal an invoice object to an OutputStream.
     * 
     * @param invoice The invoice object to marshal
     * @param outputStream The output stream to write to
     * @throws JAXBException If marshalling fails
     */
    void marshalInvoice(Object invoice, OutputStream outputStream) throws JAXBException;
    
    /**
     * Validate an ISDOC invoice against the XSD schema.
     * 
     * @param invoice The invoice object to validate
     * @return True if valid, false otherwise
     */
    boolean validateInvoice(Object invoice);
}