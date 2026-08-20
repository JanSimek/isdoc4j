package cz.isdoc.api;

import cz.isdoc.schema.invoice.v602.Invoice;
import cz.isdoc.schema.invoice.v602.ObjectFactory;
import org.junit.jupiter.api.Test;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsdocProcessorTest {

    private final IsdocProcessor processor = new IsdocProcessor();
    private final ObjectFactory factory = new ObjectFactory();

    /**
     * A real schema-valid ISDOC rather than a {@code <dummy/>} placeholder: packaging
     * only means anything if what comes out is a document a consumer would accept.
     */
    private static byte[] minimalInvoice() throws IOException {
        try (InputStream in = IsdocProcessorTest.class.getResourceAsStream("/minimal-invoice.isdoc")) {
            assertNotNull(in, "minimal-invoice.isdoc missing from test resources");
            return in.readAllBytes();
        }
    }

    @Test
    void packagerRoundTripPreservesXmlBytes() throws IOException {
        byte[] payload = minimalInvoice();
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        new IsdocPackager().pack(payload, packed);

        byte[] unpacked = new IsdocPackager().unpackMainDocument(new ByteArrayInputStream(packed.toByteArray()));
        assertArrayEquals(payload, unpacked);
    }

    @Test
    void packagedDocumentIsStillSchemaValid() throws IOException {
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        new IsdocPackager().pack(minimalInvoice(), packed);

        byte[] unpacked = new IsdocPackager().unpackMainDocument(new ByteArrayInputStream(packed.toByteArray()));
        assertTrue(processor.validate(unpacked).isValid(),
                "document extracted from the archive should still validate");
    }

    /**
     * ISDOC 6.0.2 section on the .isdocx envelope: the archive carries a manifest.xml
     * whose elements are in the http://isdoc.cz/namespace/2013/manifest namespace, with
     * a root "manifest" containing exactly one "maindocument" naming the payload.
     */
    @Test
    void manifestMatchesTheIsdocxSpecification() throws Exception {
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        new IsdocPackager().pack(minimalInvoice(), packed);

        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(packed.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.put(entry.getName(), zip.readAllBytes());
            }
        }

        assertEquals(List.of(IsdocPackager.MANIFEST_NAME, IsdocPackager.MAIN_DOCUMENT_NAME),
                List.copyOf(entries.keySet()),
                "manifest must precede the payload so a reader can locate it first");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Element manifest = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(entries.get(IsdocPackager.MANIFEST_NAME)))
                .getDocumentElement();

        assertEquals("manifest", manifest.getLocalName());
        assertEquals("http://isdoc.cz/namespace/2013/manifest", manifest.getNamespaceURI());

        NodeList main = manifest.getElementsByTagNameNS(
                "http://isdoc.cz/namespace/2013/manifest", "maindocument");
        assertEquals(1, main.getLength(), "exactly one maindocument is allowed");
        assertEquals(IsdocPackager.MAIN_DOCUMENT_NAME,
                ((Element) main.item(0)).getAttribute("filename"));
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
