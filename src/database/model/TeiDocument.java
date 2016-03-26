package database.model;

import database.sql.DocumentViewLogger;
import database.xml.client.BaseXClient;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import util.ServerConfiguration;
import util.XSLTTransformer;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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

    @Nullable static TeiDocument fromSearchResults(BaseXClient.Query q) throws IOException {
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
        String query = "for $x in collection() where db:node-id($x) = %d return replace node $x//TEI with %s";
        query = String.format(query, getId(), getXmlData().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
        //I know the replace is crap but time constraints.
        //TODO: remove replace and parse the document properly.

        //Weird bug with BaseX?? Have to enumerate query object to ensure the update happens.
        BaseXClient.Query result = client.query(query);
        while(result.more()) {
            result.next();
        }
        client.close();
    }

    private TeiDocument(String title, String date, int id, String fileName, String xmlData) {
        this.title = title;
        this.date = date;
        this.id = id;
        this.fileName = fileName;
        this.xmlData = xmlData;
    }


    public void tag(String tagType, String tagValue) throws IOException {
        String query = "for $x in collection()\n" +
                "where db:node-id($x) = %d\n" +
                "where not ($x//correspDesc/note/name[@type=\"%s\"]/text() = \"%s\")\n" +
                "return insert node <note><name type=\"%s\">%s</name></note> into $x//correspDesc";
        query = String.format(query, getId(), tagType, tagValue, tagType, tagValue);
        BaseXClient client = BaseXClient.getClient();
        BaseXClient.Query result = client.query(query);
        while(result.more()) {
            result.next();
        }
        client.close();
    }

    public static TeiDocument insertFromXml(String xml, String fileName) throws IOException {
        BaseXClient client = BaseXClient.getClient();
        String path = UUID.randomUUID().toString() + "/" + fileName;
        System.out.println(client.preparedCommand("ADD to %s %s", path, xml));
        TeiDocument document = null;
        if(!xml.equals("")) {
            BaseXClient.Query q = client.preparedQuery("db:node-id(db:open(\"%s\", \"%s\"))", ServerConfiguration.getConfigurationString("database", "name"), path);
            String documentId = q.next();
            if(documentId != null) {
                document = fromId(Integer.valueOf(documentId));
            }
        }
        client.close();
        return document;
    }

    private TeiDocument() {}

    public String getTitle() {

        return title;
    }

    public String renderHTML() throws IOException, TransformerException {
        ByteArrayOutputStream string = new ByteArrayOutputStream();
        XSLTTransformer.transform(getXmlData(), string);
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

    public String getXmlData() {
        return xmlData;
    }

    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }

    public int getViewCount(Connection c) throws SQLException {
        PreparedStatement statement = c.prepareStatement("SELECT count(*) AS view_count FROM DOCUMENT_EVENTS WHERE DOCUMENT_ID = ? AND EVENT_TYPE = ?");
        statement.setInt(1, getId());
        statement.setInt(2, DocumentViewLogger.DocumentEventType.VIEW.DB_TYPE);
        ResultSet result = statement.executeQuery();
        if(result == null || !result.next()) {
            return 0;
        }
        return result.getInt("view_count");
    }
}
