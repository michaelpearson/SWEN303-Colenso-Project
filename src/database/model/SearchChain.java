package database.model;

import database.sql.LoggableSearchChain;
import database.xml.client.BaseXClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchChain extends LoggableSearchChain {

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
            results.add(TeiDocument.fromSearchResults(q));
        }
        this.results = results;
        return results;
    }
}
