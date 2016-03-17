package database.documents;

import database.SearchQueryProcessor;
import database.client.BaseXClient;
import database.model.TeiDocument;
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

        List<TeiDocument> results = new ArrayList<>();
        while (q.more()) {
            results.add(TeiDocument.fromSearchResuls(q));
        }
        return results;
    }
}
