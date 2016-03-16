package database.documents;

import database.SearchQueryProcessor;
import database.client.BaseXClient;
import database.model.TeiDocument;
import net.sf.saxon.TransformerFactoryImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import util.Strings;

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
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Search {
    enum SearchType {
        FULLTEXT,
        XQUERY,
        LOGICAL,
    }

    private SearchType searchType;
    private String query;

    public Search(String type, String query) {
        switch(type) {
            case "fulltext":
            default:
                searchType = SearchType.FULLTEXT;
                break;
            case "xquery":
                searchType = SearchType.XQUERY;
                break;
            case "logical":
                searchType = SearchType.LOGICAL;
                break;
        }
        this.query = query;
    }

    public List<TeiDocument> executeQuery() throws IOException {
        BaseXClient client = BaseXClient.getClient();
        BaseXClient.Query q;
        if(query.equals("")) {
            q = client.query("for $x in collection()/TEI return " +
                    "<data>" +
                        "<id>{db:node-id($x)}</id>" +
                        "<title>{$x/teiHeader//title/string()}</title>" +
                        "<date>{$x/teiHeader//date/string()}</date>" +
                        "<filename>{file:name(fn:base-uri($x))}</filename>" +
                        "<xmldata>{$x}</xmldata>" +
                    "</data>");
        } else {
            switch (searchType) {
                default:
                case FULLTEXT:
                    q = client.preparedQuery("for $x in collection()/TEI where $x//text() contains text \"%s\" using fuzzy return " +
                            "<data>" +
                                "<id>{db:node-id($x)}</id>" +
                                "<title>{$x/teiHeader//title/string()}</title>" +
                                "<date>{$x/teiHeader//date/string()}</date>" +
                                "<filename>{file:name(fn:base-uri($x))}</filename>" +
                                "<xmldata>{$x}</xmldata>" +
                            "</data>", Strings.addSlashes(query));
                    break;
                case XQUERY:
                    q = client.preparedQuery("for $x in collection()/TEI where $x%s return " +
                            "<data>" +
                                "<id>{db:node-id($x)}</id>" +
                                "<title>{$x/teiHeader//title/string()}</title>" +
                                "<date>{$x/teiHeader//date/string()}</date>" +
                                "<filename>{file:name(fn:base-uri($x))}</filename>" +
                            "<xmldata>{$x}</xmldata>" +
                            "</data>", Strings.addSlashes(query));
                    break;
                case LOGICAL:
                    String search = SearchQueryProcessor.processQuery(query);
                    System.out.println(search);
                    q = client.preparedQuery("for $x in collection()/TEI  where $x/string() contains text %s return " +
                            "<data>" +
                                "<id>{db:node-id($x)}</id>" +
                                "<title>{$x/teiHeader//title/string()}</title>" +
                                "<date>{$x/teiHeader//date/string()}</date>" +
                                "<filename>{file:name(fn:base-uri($x))}</filename>" +
                            "<xmldata>{$x}</xmldata>" +
                            "</data>",  search);
                    break;
            }
        }

        Transformer transformer;
        try {
            transformer = TransformerFactoryImpl.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Could not get xml transformer");
        }

        String row;
        List<TeiDocument> results = new ArrayList<>();
        while ((row = q.next()) != null) {
            Document document;
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = documentBuilder.parse(new ByteArrayInputStream(row.getBytes()));
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
            results.add(new TeiDocument(title, date, Integer.parseInt(id), filename, xmlData));
        }
        return results;
    }
}
