package database.sql;

import database.model.Search;
import database.model.SearchChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Date;
import java.util.Map;

public class SearchLogger extends EventLogger<SearchChain> {
    @Override
    protected void logEvent(int memberToken, SearchChain search, Connection c, Map<String, Object> extra) {
        if(!(search.results != null && search.searches != null && search.searches.size() > 0)) {
            return;
        }
        try {
            PreparedStatement statement = c.prepareStatement("INSERT INTO SEARCH_CHAIN VALUES(null, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, search.results.size());
            statement.setLong(2, new Date().getTime());
            statement.setInt(3, memberToken);
            statement.setString(4, generateSearchHash(search));
            statement.executeUpdate();
            ResultSet result = statement.getGeneratedKeys();
            if(result == null || !result.next()) {
                return;
            }
            int searchChainId = result.getInt(1);
            for(Search s : search.searches) {
                PreparedStatement query = c.prepareStatement("INSERT INTO SEARCH VALUES(null, ?, ?, ?)");
                query.setInt(1, searchChainId);
                query.setString(2, s.getQuery());
                query.setInt(3, s.getSearchType().DB_TYPE);
                query.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static String generateSearchHash(SearchChain chain) {
        StringBuilder builder = new StringBuilder();
        for(Search s : chain.searches) {
            builder.append(s.getQuery());
            builder.append("/");
            builder.append(s.getSearchType());
            builder.append("/");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return bytesToHex(md.digest(builder.toString().getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unsupported JDK");
        }

    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
