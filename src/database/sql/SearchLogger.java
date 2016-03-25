package database.sql;

import database.model.Search;
import database.model.SearchChain;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.Date;
import java.util.Map;

public class SearchLogger extends EventLogger<SearchChain> {
    @Override
    protected void logEvent(HttpServletRequest request, SearchChain search, Connection c, Map<String, Object> extra) {
        if(!(search.results != null && search.searches != null && search.searches.size() > 0)) {
            return;
        }
        try {
            PreparedStatement statement = c.prepareStatement("INSERT INTO SEARCH_CHAIN VALUES(null, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, search.results.size());
            statement.setLong(2, new Date().getTime());
            statement.setInt(3, extractMemberToken(request.getCookies()));
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
}
