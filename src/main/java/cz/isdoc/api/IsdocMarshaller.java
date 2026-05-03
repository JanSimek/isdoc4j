package cz.isdoc.api;

import cz.isdoc.schema.invoice.v602.Invoice;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * JAXB serialization for ISDOC {@link Invoice} documents. The
 * {@link JAXBContext} is created once per instance and reused, so callers
 * should hold on to a single {@code IsdocMarshaller} rather than constructing
 * a new one per request.
 */
public class IsdocMarshaller {

    private final JAXBContext context;

    public IsdocMarshaller() {
        try {
            this.context = JAXBContext.newInstance(Invoice.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create ISDOC JAXB context", e);
        }
    }

    public byte[] marshalToBytes(Invoice invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshal(invoice, out);
        return out.toByteArray();
    }

    public void marshal(Invoice invoice, OutputStream output) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            marshaller.marshal(invoice, output);
        } catch (JAXBException e) {
            throw new IsdocException("Unable to marshal ISDOC invoice", e);
        }
    }

    public Invoice unmarshal(byte[] xml) {
        return unmarshal(new ByteArrayInputStream(xml));
    }

    public Invoice unmarshal(InputStream input) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (Invoice) unmarshaller.unmarshal(input);
        } catch (JAXBException e) {
            throw new IsdocException("Unable to parse ISDOC invoice", e);
        }
    }
}
