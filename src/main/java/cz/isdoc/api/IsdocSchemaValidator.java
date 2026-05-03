package cz.isdoc.api;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates ISDOC XML payloads against the embedded ISDOC 6.0.2 XSD schema.
 *
 * <p>The schema is loaded from the library's classpath ({@value #SCHEMA_PATH}),
 * so consumers don't need to ship their own copy. XXE protection is enabled on
 * the document builder.
 */
public class IsdocSchemaValidator {

    private static final String SCHEMA_PATH = "/xsd/isdoc-invoice-6.0.2.xsd";

    public IsdocValidationResult validate(byte[] xmlContent) {
        try {
            Schema schema = loadSchema();
            Document document = parse(xmlContent);

            CollectingErrorHandler handler = new CollectingErrorHandler();
            Validator validator = schema.newValidator();
            validator.setErrorHandler(handler);
            validator.validate(new DOMSource(document));

            return handler.hasErrors()
                    ? IsdocValidationResult.invalid(handler.allMessages())
                    : IsdocValidationResult.valid();
        } catch (Exception e) {
            return IsdocValidationResult.invalid(List.of("Validation failed: " + e.getMessage()));
        }
    }

    private Schema loadSchema() throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        InputStream resource = IsdocSchemaValidator.class.getResourceAsStream(SCHEMA_PATH);
        if (resource == null) {
            throw new IllegalStateException("ISDOC schema not found on classpath: " + SCHEMA_PATH);
        }
        return factory.newSchema(new javax.xml.transform.stream.StreamSource(resource));
    }

    private Document parse(byte[] xmlContent) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent));
    }

    private static final class CollectingErrorHandler implements ErrorHandler {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        @Override
        public void warning(SAXParseException e) {
            warnings.add(format("Warning", e));
        }

        @Override
        public void error(SAXParseException e) {
            errors.add(format("Error", e));
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            errors.add(format("Fatal Error", e));
            throw e;
        }

        boolean hasErrors() {
            return !errors.isEmpty();
        }

        List<String> allMessages() {
            List<String> all = new ArrayList<>(errors.size() + warnings.size());
            all.addAll(errors);
            all.addAll(warnings);
            return all;
        }

        private static String format(String level, SAXParseException e) {
            return String.format("%s at line %d, column %d: %s",
                    level, e.getLineNumber(), e.getColumnNumber(), e.getMessage());
        }
    }
}
