# ISDOC4j - Java Library for ISDOC 6.0.2

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/)
[![Maven Central](https://img.shields.io/maven-central/v/cz.isdoc/isdoc4j.svg)](https://search.maven.org/search?q=g:cz.isdoc%20AND%20a:isdoc4j)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Java library providing **JAXB-generated classes** for the **ISDOC 6.0.2** (Information System Document) format - the Czech national standard for electronic invoices.

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>cz.isdoc</groupId>
    <artifactId>isdoc4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle Dependency

```groovy
implementation 'cz.isdoc:isdoc4j:1.0.0'
```

## 📋 What is ISDOC?

**ISDOC** (Information System Document) is the **Czech national standard** for electronic invoices, developed by the Association for Electronic Commerce (APEK). It provides a standardized XML format for business-to-business and business-to-government invoice exchange in the Czech Republic.

### Key Features
- 🇨🇿 **Czech national standard** for electronic invoicing
- 📄 **XML-based format** with strict schema validation
- 🏛️ **Government-approved** for public sector transactions
- 💼 **B2B & B2G** invoice exchange support
- 📦 **ZIP packaging** with manifest and attachments

## 🏗️ Library Overview

This library provides **strongly-typed Java classes** generated directly from the official ISDOC 6.0.2 XSD schema using **JAXB/XJC**.

### Generated Classes

All classes are generated in the `cz.isdoc.schema.invoice.v602` package:

```java
// Main classes
Invoice                    // Root invoice document
InvoiceLineType           // Individual invoice line items
PartyType                 // Supplier/customer information
AccountingSupplierPartyType
AccountingCustomerPartyType
LegalMonetaryTotalType    // Invoice totals
TaxTotalType              // VAT information
// ... and 60+ more classes
```

### Package Structure

```
cz.isdoc.schema.invoice.v602/
├── Invoice.java                    # Root element
├── InvoiceLineType.java           # Line items
├── PartyType.java                 # Party information
├── AccountingSupplierPartyType.java
├── AccountingCustomerPartyType.java
├── LegalMonetaryTotalType.java    # Monetary totals
├── TaxTotalType.java              # VAT totals
├── ObjectFactory.java             # JAXB factory
└── ... (60+ generated classes)
```

## 💻 Usage Examples

The `cz.isdoc.api` package is the intended entry point. `IsdocProcessor` composes
marshalling, schema validation and `.isdocx` packaging, so most callers never touch
JAXB directly.

### Basic Invoice Creation

Note the element names: the root `Invoice` element's children are generated as
`createInvoiceXxx`, not `createXxx`, and their types come straight from the XSD —
`DocumentType` is a `BigInteger`, dates are `XMLGregorianCalendar`.

```java
import cz.isdoc.api.IsdocProcessor;
import cz.isdoc.schema.invoice.v602.Invoice;
import cz.isdoc.schema.invoice.v602.ObjectFactory;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;

ObjectFactory factory = new ObjectFactory();

Invoice invoice = new Invoice();
invoice.setVersion("6.0.2");   // required by the schema

// Children go into getContent() in the order the XSD declares them.
invoice.getContent().add(factory.createInvoiceDocumentType(BigInteger.ONE));
invoice.getContent().add(factory.createInvoiceID("INV-2026-001"));
invoice.getContent().add(factory.createInvoiceUUID("00000000-0000-4000-8000-000000000001"));
invoice.getContent().add(factory.createInvoiceIssueDate(
        DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                2026, 1, 31, DatatypeConstants.FIELD_UNDEFINED)));
// ... remaining required elements: VATApplicable, LocalCurrencyCode, CurrRate,
// RefCurrRate, both parties, InvoiceLines, TaxTotal, LegalMonetaryTotal

IsdocProcessor processor = new IsdocProcessor();

// Marshal and validate against the bundled XSD in one step; throws IsdocException
// if the document is not schema-valid.
byte[] xml = processor.marshalAndValidate(invoice);
```

### Writing and Reading `.isdocx`

```java
IsdocProcessor processor = new IsdocProcessor();

// Write a complete archive: manifest.xml plus invoice.isdoc
try (OutputStream out = Files.newOutputStream(Path.of("invoice.isdocx"))) {
    processor.write(invoice, out);
}

// Read one back
try (InputStream in = Files.newInputStream(Path.of("invoice.isdocx"))) {
    Invoice parsed = processor.read(in);
}
```

### Working with Invoice Content

`Invoice` exposes its children as an untyped `List<Object>` of `JAXBElement`s rather
than named getters, because the schema's content model is not expressible as distinct
properties. There is no `setID()`; you add and search the list yourself, and ordering
is your responsibility — schema validation is what catches a mistake.

```java
List<Object> content = invoice.getContent();

Optional<String> invoiceId = content.stream()
    .filter(JAXBElement.class::isInstance)
    .map(el -> (JAXBElement<?>) el)
    .filter(el -> "ID".equals(el.getName().getLocalPart()))
    .map(el -> (String) el.getValue())
    .findFirst();
```

### Validating an existing document

```java
IsdocValidationResult result = new IsdocProcessor().validate(xmlBytes);
if (!result.isValid()) {
    result.getErrors().forEach(System.err::println);
}
```

## 🛠️ Building from Source

### Prerequisites
- **Java 21** or later
- **Maven 3.8** or later
- **Internet connection** (for downloading XSD schema)

### Build Steps

```bash
# Clone repository
git clone https://github.com/JanSimek/isdoc4j.git
cd isdoc4j

# Generate classes and build
mvn clean compile

# Run tests (if any)
mvn test

# Install to local repository
mvn install

# Create distribution package
mvn package
```

### Code Generation Process

The build process automatically:
1. **Downloads** the official ISDOC 6.0.2 XSD schema
2. **Applies** custom JAXB bindings to resolve naming conflicts
3. **Generates** Java classes using XJC
4. **Compiles** the generated classes
5. **Packages** everything into a JAR

## ⚙️ Configuration & Customization

### JAXB Bindings

The library includes custom bindings (`src/main/resources/bindings.xjb`) to resolve XSD naming conflicts:

```xml
<!-- Rename 'id' attributes to avoid conflicts with 'ID' elements -->
<bindings node="//xs:attributeGroup[@name='IdAttribute']/xs:attribute[@name='id']">
    <property name="idAttribute"/>
</bindings>
```

### Maven Plugin Configuration

Code generation is configured in `pom.xml`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>xjc</id>
            <goals>
                <goal>xjc</goal>
            </goals>
            <configuration>
                <sources>
                    <source>src/main/resources/xsd/isdoc-invoice-6.0.2.xsd</source>
                </sources>
                <xjbSources>
                    <xjbSource>src/main/resources/bindings.xjb</xjbSource>
                </xjbSources>
                <packageName>cz.isdoc.schema.invoice.v602</packageName>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 🔗 Integration Examples

### Spring Boot Integration

`IsdocProcessor` is thread-safe and builds its `JAXBContext` once, so register a
single instance as a bean rather than constructing one per request.

```java
@Configuration
public class IsdocConfig {

    @Bean
    public IsdocProcessor isdocProcessor() {
        return new IsdocProcessor();
    }
}

@Service
public class InvoiceExportService {

    private final IsdocProcessor processor;

    public InvoiceExportService(IsdocProcessor processor) {
        this.processor = processor;
    }

    /** Raw ISDOC XML, e.g. to embed as a PDF attachment. */
    public byte[] toIsdocXml(Invoice invoice) {
        return processor.marshalAndValidate(invoice);
    }

    /** A complete .isdocx archive. */
    public void writeArchive(Invoice invoice, OutputStream out) {
        processor.write(invoice, out);
    }

    public Invoice read(InputStream isdocx) {
        return processor.read(isdocx);
    }
}
```

### Creating a Complete `.isdocx` File

Building the ZIP and manifest by hand is unnecessary — `IsdocPackager` does it, and
`IsdocProcessor.write` validates before packaging:

```java
public void createIsdocFile(Invoice invoice, Path outputPath) throws IOException {
    try (OutputStream out = Files.newOutputStream(outputPath)) {
        processor.write(invoice, out);
    }
}
```

The resulting archive holds `manifest.xml` followed by `invoice.isdoc`, with the
manifest in the `http://isdoc.cz/namespace/2013/manifest` namespace naming the
payload — the layout the ISDOC 6.0.2 specification requires.

## 🐛 Known Issues & Workarounds

### XSD Naming Conflicts
The original ISDOC XSD contains naming conflicts (attributes named `id` vs elements named `ID`). This library resolves these using custom JAXB bindings.

### Content List Approach
Due to schema complexity, the main `Invoice` class uses a content list rather than individual properties. Use the `ObjectFactory` for creating properly typed elements.

### Generated Code
Classes are generated during build - don't edit them directly. Modify the XSD bindings instead if customization is needed.

## 📚 Documentation & Resources

### Official ISDOC Resources
- [ISDOC Official Website](https://isdoc.cz/)
- [ISDOC 6.0.2 Specification](https://isdoc.cz/6.0.2/doc/isdoc.html)
- [XSD Schema](https://isdoc.cz/6.0.2/doc/isdoc-invoice-6.0.2.xsd)

### Related Projects
- [PHP ISDOC Library](https://github.com/adawolfa/isdoc)
- [Node.js ISDOC Library](https://github.com/deltazero-cz/node-isdoc)

### JAXB Documentation
- [Jakarta XML Binding](https://eclipse-ee4j.github.io/jaxb-ri/)
- [JAXB Tutorial](https://docs.oracle.com/javase/tutorial/jaxb/)

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md).

### Development Setup
1. Fork the repository
2. Clone your fork
3. Create a feature branch
4. Make your changes
5. Run tests: `mvn test`
6. Submit a pull request

### Reporting Issues
- Use [GitHub Issues](https://github.com/JanSimek/isdoc4j/issues)
- Include Java version, Maven version, and full error messages
- Provide minimal reproduction case when possible

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## ✨ Acknowledgments

- **APEK** (Association for Electronic Commerce) for creating and maintaining the ISDOC standard
- **Czech Ministry of Finance** for supporting electronic invoicing standards
- **Jakarta EE community** for the JAXB implementation
- All contributors who help improve this library

---

**Made with ❤️ for the Czech business community** 🇨🇿

For questions, issues, or contributions, please visit our [GitHub repository](https://github.com/JanSimek/isdoc4j).