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

### Basic Invoice Creation

```java
import cz.isdoc.schema.invoice.v602.*;
import jakarta.xml.bind.*;

// Create invoice
Invoice invoice = new Invoice();
invoice.setVersion("6.0.2");

// Add basic information
JAXBElement<String> documentType = objectFactory.createDocumentType("1");
JAXBElement<String> id = objectFactory.createID("INV-2024-001");
invoice.getContent().add(documentType);
invoice.getContent().add(id);

// Marshal to XML
JAXBContext context = JAXBContext.newInstance(Invoice.class);
Marshaller marshaller = context.createMarshaller();
marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
marshaller.marshal(invoice, System.out);
```

### Reading ISDOC File

```java
// Unmarshal from XML
JAXBContext context = JAXBContext.newInstance(Invoice.class);
Unmarshaller unmarshaller = context.createUnmarshaller();

// From file
Invoice invoice = (Invoice) unmarshaller.unmarshal(new File("invoice.xml"));

// From ISDOCX archive (ZIP format)
try (ZipInputStream zis = new ZipInputStream(new FileInputStream("invoice-2024-001.isdocx"))) {
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
        if (entry.getName().endsWith(".isdoc")) {
            Invoice invoice = (Invoice) unmarshaller.unmarshal(zis);
            break;
        }
    }
}
```

### Working with Invoice Content

ISDOC uses a content list approach for flexibility:

```java
Invoice invoice = new Invoice();

// Access content list
List<Object> content = invoice.getContent();

// Add elements using ObjectFactory
ObjectFactory factory = new ObjectFactory();
content.add(factory.createDocumentType("1"));
content.add(factory.createID("INV-001"));
content.add(factory.createIssueDate(LocalDate.now()));

// Find specific elements
Optional<String> invoiceId = content.stream()
    .filter(JAXBElement.class::isInstance)
    .map(JAXBElement.class::cast)
    .filter(el -> "ID".equals(el.getName().getLocalPart()))
    .map(el -> (String) el.getValue())
    .findFirst();
```

## 🛠️ Building from Source

### Prerequisites
- **Java 21** or later
- **Maven 3.8** or later
- **Internet connection** (for downloading XSD schema)

### Build Steps

```bash
# Clone repository
git clone https://github.com/your-username/isdoc4j.git
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

```java
@Service
public class IsdocService {
    
    private final JAXBContext jaxbContext;
    
    public IsdocService() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(Invoice.class);
    }
    
    public Invoice parseIsdoc(InputStream inputStream) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (Invoice) unmarshaller.unmarshal(inputStream);
    }
    
    public void writeIsdoc(Invoice invoice, OutputStream outputStream) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(invoice, outputStream);
    }
}
```

### Creating Complete ISDOC File

```java
public void createIsdocFile(Invoice invoice, Path outputPath) throws IOException, JAXBException {
    // Create ZIP with manifest and invoice XML
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputPath))) {
        
        // Add ISDOC manifest (root level, not META-INF)
        zos.putNextEntry(new ZipEntry("manifest.xml"));
        String manifest = createManifest("invoice-2024-001.isdoc");
        zos.write(manifest.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
        
        // Add main ISDOC document
        zos.putNextEntry(new ZipEntry("invoice-2024-001.isdoc"));
        JAXBContext context = JAXBContext.newInstance(Invoice.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(invoice, zos);
        zos.closeEntry();
    }
}

private String createManifest(String filename) {
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <manifest xmlns="http://isdoc.cz/namespace/2013/manifest">
            <maindocument filename="%s"/>
        </manifest>
        """.formatted(filename);
}
```

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
- Use [GitHub Issues](https://github.com/your-username/isdoc4j/issues)
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

For questions, issues, or contributions, please visit our [GitHub repository](https://github.com/your-username/isdoc4j).