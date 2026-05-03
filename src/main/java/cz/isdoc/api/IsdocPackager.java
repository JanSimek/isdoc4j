package cz.isdoc.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Packs and unpacks the {@code .isdocx} ZIP envelope used to ship ISDOC
 * documents. The envelope contains a {@code manifest.xml} pointing at the
 * main {@code invoice.isdoc} XML payload.
 */
public class IsdocPackager {

    public static final String MAIN_DOCUMENT_NAME = "invoice.isdoc";
    public static final String MANIFEST_NAME = "manifest.xml";

    private static final String MANIFEST_TEMPLATE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<manifest xmlns=\"http://isdoc.cz/namespace/2013/manifest\">" +
                    "<maindocument filename=\"" + MAIN_DOCUMENT_NAME + "\"/>" +
                    "</manifest>";

    public void pack(byte[] invoiceXml, OutputStream output) {
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            writeEntry(zip, MANIFEST_NAME, MANIFEST_TEMPLATE.getBytes(StandardCharsets.UTF_8));
            writeEntry(zip, MAIN_DOCUMENT_NAME, invoiceXml);
            zip.finish();
        } catch (IOException e) {
            throw new IsdocException("Unable to package ISDOC archive", e);
        }
    }

    public byte[] unpackMainDocument(InputStream packagedInput) {
        try (ZipInputStream zip = new ZipInputStream(packagedInput)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals(MAIN_DOCUMENT_NAME)) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    zip.transferTo(buffer);
                    return buffer.toByteArray();
                }
            }
            throw new IsdocException("ISDOC archive does not contain " + MAIN_DOCUMENT_NAME);
        } catch (IOException e) {
            throw new IsdocException("Unable to read ISDOC archive", e);
        }
    }

    private void writeEntry(ZipOutputStream zip, String name, byte[] payload) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(payload);
        zip.closeEntry();
    }
}
