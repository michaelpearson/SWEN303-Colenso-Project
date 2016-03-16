package database.model;

import database.client.BaseXClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;

public class TeiDocument {
    private String title;
    private String date;
    private int id;
    private String fileName;
    private String xmlData = "";


    public static TeiDocument fromId(int documentId) throws IOException {
        BaseXClient client = BaseXClient.getClient();
        String documentQuery = String.format("db:node-id($x) = %d", documentId);
        BaseXClient.Query q = client.preparedQuery("for $x in /TEI where %s return " +
                "<data>" +
                    "<id>{db:node-id($x)}</id>\n" +
                    "<title>{$x/teiHeader//title/string()}</title>\n" +
                    "<date>{$x/teiHeader//date/string()}</date>\n" +
                    "<filename>{file:name(fn:base-uri($x))}</filename>\n" +
                "</data>", documentQuery);
        TeiDocument document = new TeiDocument();
        Document dom = Jsoup.parse(q.next(), "", Parser.xmlParser());
        document.id = Integer.parseInt(dom.getElementsByTag("id").first().text());
        document.title = dom.getElementsByTag("title").first().text();
        document.date = dom.getElementsByTag("date").first().text();
        document.fileName = dom.getElementsByTag("filename").first().text();
        return document;
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
