package database.model;

import database.client.BaseXClient;
import database.documents.Search;
import database.documents.SearchChain;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import util.DocumentRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class TeiDocument {
    private String title;
    private String date;
    private int id;
    private String fileName;
    private String xmlData = "";

    private static Transformer transformer;

    static {
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Could not get transformer");
        }
    }
    public static TeiDocument fromId(int documentId) throws IOException {
        SearchChain sc = new SearchChain();
        sc.addSearch(new Search("id", String.format("%d", documentId)));
        List<TeiDocument> documents = sc.executeSearch();
        if(documents.size() == 1) {
            return documents.get(0);
        }
        throw new RuntimeException("Could not find document " + documentId);
    }

    public static TeiDocument fromSearchResuls(BaseXClient.Query q) throws IOException {
        if(q.more()) {
            Document document;
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = documentBuilder.parse(new ByteArrayInputStream(q.next().getBytes()));
            } catch (ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not get document builder");
            }

            String id = document.getElementsByTagName("id").item(0).getTextContent();
            String title = document.getElementsByTagName("title").item(0).getTextContent();
            String date = document.getElementsByTagName("date").item(0).getTextContent();
            String filename = document.getElementsByTagName("filename").item(0).getTextContent();

            DOMSource source = new DOMSource(document.getElementsByTagName("xmldata").item(0).getChildNodes().item(1));

            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {
                throw new RuntimeException("Could not generate XML");
            }
            String xmlData = stringWriter.toString();

            return new TeiDocument(title, date, Integer.parseInt(id), filename, xmlData);
        }
        return null;
    }

    public void update() throws IOException {
        BaseXClient client = BaseXClient.getClient();
        String query = "for $x in collection() where db:node-id($x) = %d return replace node $x/TEI with %s";
        query = String.format(query, getId(), getXmlData().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
        //I know the replace is crap but time constraints.
        //TODO: remove replace and parse the document properly.
        client.preparedQuery(query);
        client.close();
    }

    public TeiDocument(String title, String date, int id, String fileName, String xmlData) {
        this.title = title;
        this.date = date;
        this.id = id;
        this.fileName = fileName;
        this.xmlData = xmlData;
    }

    private TeiDocument() {}

    public String getTitle() {

        return title;
    }

    public String renderHTML() throws IOException, TransformerException {
        ByteArrayOutputStream string = new ByteArrayOutputStream();
        DocumentRenderer.simpleTransform(getXmlData(), string);
        return string.toString("utf-8");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getXmlData() {
        return xmlData;
    }

    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }
}
