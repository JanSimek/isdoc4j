package cz.isdoc.api;

import cz.isdoc.schema.invoice.v602.Invoice;
import cz.isdoc.schema.invoice.v602.ObjectFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsdocProcessorTest {

    private final IsdocProcessor processor = new IsdocProcessor();
    private final ObjectFactory factory = new ObjectFactory();

    @Test
    void packagerRoundTripPreservesXmlBytes() {
        byte[] payload = "<?xml version=\"1.0\"?><dummy/>".getBytes();
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        new IsdocPackager().pack(payload, packed);

        byte[] unpacked = new IsdocPackager().unpackMainDocument(new ByteArrayInputStream(packed.toByteArray()));
        assertArrayEquals(payload, unpacked);
    }

    @Test
    void marshallerRoundTripPreservesInvoiceVersion() {
        Invoice invoice = new Invoice();
        invoice.setVersion("6.0.2");
        invoice.getContent().add(factory.createInvoiceDocumentType(BigInteger.ONE));
        invoice.getContent().add(factory.createInvoiceID("RT-1"));

        IsdocMarshaller marshaller = new IsdocMarshaller();
        byte[] xml = marshaller.marshalToBytes(invoice);
        Invoice parsed = marshaller.unmarshal(xml);

        assertEquals("6.0.2", parsed.getVersion());
    }

    @Test
    void validatorRejectsObviouslyInvalidXml() {
        byte[] notIsdoc = "<?xml version=\"1.0\"?><nope/>".getBytes();
        IsdocValidationResult result = processor.validate(notIsdoc);
        assertFalse(result.isValid());
        assertNotNull(result.getErrors());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void processorMarshalAndValidateThrowsWhenInvoiceIsIncomplete() {
        // An Invoice with no content fails schema validation — the facade
        // surfaces this as IsdocException so callers don't need to look at
        // the validation result themselves.
        Invoice invoice = new Invoice();
        invoice.setVersion("6.0.2");

        IsdocException ex = assertThrows(IsdocException.class,
                () -> processor.marshalAndValidate(invoice));
        assertTrue(ex.getMessage().contains("not valid"));
    }
}
