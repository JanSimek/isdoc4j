package cz.isdoc.api;

import cz.isdoc.schema.invoice.v602.Invoice;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Top-level entry point for working with ISDOC 6.0.2 invoice documents.
 *
 * <p>This facade composes {@link IsdocMarshaller}, {@link IsdocSchemaValidator}
 * and {@link IsdocPackager} so callers can do the common operations
 * (marshal + validate, write archive, read archive) in a single call.
 *
 * <pre>{@code
 * IsdocProcessor processor = new IsdocProcessor();
 *
 * // Build a JAXB Invoice from your domain model, then write a complete .isdocx
 * processor.write(invoice, outputStream);
 *
 * // Or marshal to raw XML for embedding (e.g. in a PDF attachment)
 * byte[] xml = processor.marshalAndValidate(invoice);
 *
 * // Or read an .isdocx archive back into a JAXB tree
 * Invoice parsed = processor.read(inputStream);
 * }</pre>
 *
 * <p>By default {@link #write(Invoice, OutputStream)} and {@link #marshalAndValidate(Invoice)}
 * validate the produced XML against the bundled XSD; pass {@code validate=false}
 * to skip validation when you've already validated the payload separately.
 */
public class IsdocProcessor {

    private final IsdocMarshaller marshaller;
    private final IsdocSchemaValidator validator;
    private final IsdocPackager packager;

    public IsdocProcessor() {
        this(new IsdocMarshaller(), new IsdocSchemaValidator(), new IsdocPackager());
    }

    public IsdocProcessor(IsdocMarshaller marshaller, IsdocSchemaValidator validator, IsdocPackager packager) {
        this.marshaller = marshaller;
        this.validator = validator;
        this.packager = packager;
    }

    /**
     * Marshal {@code invoice} to XML and validate it against the ISDOC schema.
     *
     * @throws IsdocException if marshalling fails or the XML is not schema-valid.
     */
    public byte[] marshalAndValidate(Invoice invoice) {
        return marshalAndValidate(invoice, true);
    }

    public byte[] marshalAndValidate(Invoice invoice, boolean validate) {
        byte[] xml = marshaller.marshalToBytes(invoice);
        if (validate) {
            IsdocValidationResult result = validator.validate(xml);
            if (!result.isValid()) {
                throw new IsdocException("Generated ISDOC XML is not valid: " + result.getErrors());
            }
        }
        return xml;
    }

    /**
     * Marshal, validate, then write a complete {@code .isdocx} (manifest + invoice.isdoc)
     * archive to {@code output}.
     */
    public void write(Invoice invoice, OutputStream output) {
        write(invoice, output, true);
    }

    public void write(Invoice invoice, OutputStream output, boolean validate) {
        byte[] xml = marshalAndValidate(invoice, validate);
        packager.pack(xml, output);
    }

    /**
     * Read an {@code .isdocx} archive's main document and parse it back into a
     * JAXB {@link Invoice}.
     */
    public Invoice read(InputStream input) {
        byte[] xml = packager.unpackMainDocument(input);
        return marshaller.unmarshal(xml);
    }

    /**
     * Validate raw ISDOC XML bytes (without unpacking).
     */
    public IsdocValidationResult validate(byte[] xml) {
        return validator.validate(xml);
    }

    public IsdocMarshaller marshaller() {
        return marshaller;
    }

    public IsdocSchemaValidator validator() {
        return validator;
    }

    public IsdocPackager packager() {
        return packager;
    }
}
