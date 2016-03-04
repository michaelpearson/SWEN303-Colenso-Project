import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class XsltTest {

    /**
     * Simple transformation method.
     * @param sourcePath - Absolute path to source xml file.
     * @param xsltPath - Absolute path to xslt file.
     * @param resultDir - Directory where you want to put resulting files.
     */
    public static void simpleTransform(String sourcePath, String xsltPath, String resultDir) throws TransformerException, IOException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(new File(xsltPath)));
        transformer.transform(new StreamSource(new File(sourcePath)), new StreamResult(new File(resultDir)));
    }

    public static void main(String[] args) {
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        try {
            simpleTransform("/home/michael/workspace/SWEN303-Colenso-Project/xml files/Broughton/private_letters/PrLBrghtn-0001.xml", "/home/michael/workspace/SWEN303-Colenso-Project/Stylesheets/html5/html5.xsl", "/home/michael/workspace/SWEN303-Colenso-Project/Stylesheets/html5/out/file.html");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
