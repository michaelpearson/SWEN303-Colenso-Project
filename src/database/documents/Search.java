package database.documents;

import database.SearchQueryProcessor;
import database.client.BaseXClient;
import database.model.TeiDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import util.Strings;

import java.io.IOException;
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
            q = client.query("for $x in collection()/TEI\n" +
                    "return <data>\n" +
                    "         <id>{db:node-id($x)}</id>\n" +
                    "         <title>{$x/teiHeader//title/string()}</title>\n" +
                    "         <date>{$x/teiHeader//date/string()}</date>\n" +
                    "       </data>");
        } else {
            switch (searchType) {
                default:
                case FULLTEXT:
                    q = client.preparedQuery("for $x in collection()/TEI where $x//text() contains text \"%s\" using fuzzy return " +
                            "<data>" +
                            "<id>{db:node-id($x)}</id>" +
                            "<title>{$x/teiHeader//title/string()}</title>" +
                            "<date>{$x/teiHeader//date/string()}</date>" +
                            "</data>", Strings.addSlashes(query));
                    break;
                case XQUERY:
                    q = client.preparedQuery("for $x in collection()/TEI where $x%s return " +
                            "<data>" +
                            "<id>{db:node-id($x)}</id>" +
                            "<title>{$x/teiHeader//title/string()}</title>" +
                            "<date>{$x/teiHeader//date/string()}</date>" +
                            "</data>", Strings.addSlashes(query));
                    break;
                case LOGICAL:
                    String search = SearchQueryProcessor.processQuery(query);
                    System.out.println(search);
                    q = client.preparedQuery("for $x in collection()/TEI " +
                            "where $x/string() contains text %s" +
                            "return " +
                            "<data>" +
                            "<id>{db:node-id($x)}</id>" +
                            "<title>{$x/teiHeader//title/string()}</title>" +
                            "<date>{$x/teiHeader//date/string()}</date>" +
                            "</data>",  search);
                    break;
            }
        }
        String row;
        List<TeiDocument> results = new ArrayList<>();
        while ((row = q.next()) != null) {
            Document dom = Jsoup.parse(row, "", Parser.xmlParser());
            String id = dom.getElementsByTag("id").first().text();
            String title = dom.getElementsByTag("title").first().text();
            String date = dom.getElementsByTag("date").first().text();
            results.add(new TeiDocument(title, date, Integer.parseInt(id)));
        }
        return results;
    }
}
