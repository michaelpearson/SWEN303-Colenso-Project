package database.documents;

import database.client.BaseXClient;
import database.model.TeiDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchChain {
    private List<Search> searches = new ArrayList<>();


    public SearchChain() {}

    public void addSearch(Search search) {
        searches.add(search);
    }

    public List<TeiDocument> executeSearch() throws IOException {
        String searchQuery = "collection()";
        for(Search s : searches) {
            searchQuery = s.getPreparedQuery(searchQuery);
        }
        searchQuery = String.format("for $x in (%s) where $x//TEI return " +
                "<data>" +
                "<id>{db:node-id($x)}</id>" +
                "<title>{$x//teiHeader//title/string()}</title>" +
                "<date>{$x//teiHeader//date/string()}</date>" +
                "<filename>{file:name(fn:base-uri($x))}</filename>" +
                "<xmldata>{$x}</xmldata>" +
                "</data>", searchQuery);

        BaseXClient client = BaseXClient.getClient();
        BaseXClient.Query q = client.preparedQuery(searchQuery);
        List<TeiDocument> results = new ArrayList<>();
        while (q.more()) {
            results.add(TeiDocument.fromSearchResuls(q));
        }
        return results;
    }
}
