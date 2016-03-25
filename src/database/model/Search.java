package database.model;

import database.xml.SearchQueryProcessor;

import java.sql.*;

public class Search {
    public enum SearchType {
        FULLTEXT(0, "fulltext"),
        XQUERY(1, "xquery"),
        LOGICAL(2, "logical"),
        ID(3, "id");
        public final int DB_TYPE;
        public final String value;
        SearchType(int type, String value) {
            DB_TYPE = type;
            this.value = value;
        }
        public static SearchType fromDbType(int t) {
            for(SearchType st : SearchType.values()) {
                if(st.DB_TYPE == t) {
                    return st;
                }
            }
            return null;
        }
        public static SearchType fromString(String s) {
            for(SearchType st : SearchType.values()) {
                if(s.equals(st.value)) {
                    return st;
                }
            }
            return null;
        }
    }

    private SearchType searchType;
    private String query;

    public Search(String type, String query) {
        searchType = SearchType.fromString(type);
        this.query = query;
    }

    private Search() {}

    public static Search fromDatabase(int id, Connection c) throws SQLException {
        PreparedStatement query = c.prepareStatement("select QUERY as query, TYPE as type from SEARCH where id = ?");
        query.setInt(1, id);
        ResultSet result = query.executeQuery();
        if(result == null || !result.next()) {
            throw new RuntimeException("Could not find search");
        }
        Search s = new Search();
        s.query = result.getString("query");
        s.searchType = SearchType.fromDbType(result.getInt("type"));
        return s;
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
                    if(query.toLowerCase().startsWith("for")) {
                        q = String.format("for $x in (%s) where %s return $x", datasource, query);
                    } else {
                        q = String.format("for $x in (%s) where $x%s return $x", datasource, query);
                    }
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

    public SearchType getSearchType() {
        return searchType;
    }

    public String getQuery() {
        return query;
    }
}
