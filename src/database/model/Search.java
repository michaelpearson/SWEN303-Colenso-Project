package database.model;

import database.xml.SearchQueryProcessor;

public class Search {
    enum SearchType {
        FULLTEXT,
        XQUERY,
        LOGICAL,
        ID
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
            case "id":
                searchType = SearchType.ID;
                break;
        }
        this.query = query;
    }

    public String getPreparedQuery() {
        return getPreparedQuery("collection()");
    }


    public String getPreparedQuery(String datasource) {
        if (query.equals("")) {
            return datasource;
        } else {
            String q;
            switch (searchType) {
                default:
                case FULLTEXT:
                    q = String.format("for $x in (%s) where $x//text() contains text \"%s\" using fuzzy return $x", datasource, addSlashes(query));
                    break;
                case XQUERY:
                    q = String.format("for $x in (%s) where $x%s return $x", datasource, addSlashes(query));
                    break;
                case LOGICAL:
                    String search = SearchQueryProcessor.processQuery(query);
                    q = String.format("for $x in (%s) where $x/string() contains text %s return $x", datasource, search);
                    break;
                case ID:
                    q = String.format("for $x in (%s) where db:node-id($x) = %d return $x", datasource, Integer.parseInt(query));
                    break;
            }
            return q;
        }
    }
    private static String addSlashes(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        s = s.replaceAll("\\n", "\\\\n");
        s = s.replaceAll("\\r", "\\\\r");
        s = s.replaceAll("\\00", "\\\\0");
        s = s.replaceAll("'", "\\\\'");
        return s;
    }
}