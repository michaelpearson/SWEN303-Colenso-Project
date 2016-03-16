package util;

import database.model.TeiDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class DocumentZip {
    public static void writeDocumentToStream(List<TeiDocument> documents, OutputStream outputStream) throws IOException {
        ZipOutputStream os = new ZipOutputStream(outputStream);
        for(TeiDocument document : documents) {
            try {
                os.putNextEntry(new ZipEntry(document.getFileName()));
                os.write(document.getXmlData().getBytes());
            } catch (ZipException ignore) {
                //TODO: not ignore this message
            }
        }
        os.close();
    }
}