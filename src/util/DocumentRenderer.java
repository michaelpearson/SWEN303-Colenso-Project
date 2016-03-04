package util;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class DocumentRenderer {
    private static Transformer transformer;

    static {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            transformer = tFactory.newTransformer(new StreamSource(new File(ServerConfiguration.getConfigurationString("htmlxslt"))));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load xslt file");
        }
    }

    public static void simpleTransform(String inputDocument, OutputStream outputStream) throws TransformerException, IOException {
        transformer.transform(new StreamSource(new StringReader(inputDocument)), new StreamResult(outputStream));
    }
}
