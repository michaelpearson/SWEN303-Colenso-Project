package endpoints;

import database.model.Search;
import database.model.SearchChain;
import database.sql.Database;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.JsonResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GetSearchStats extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection c = Database.getConnection();
        try {
            Statement query = c.createStatement();
            ResultSet result = query.executeQuery("select max(id) as id, count(*) as search_count from SEARCH_CHAIN  group by SEARCH_HASH order by search_count desc;");

            JSONArray searchChains = new JSONArray();

            while(result.next()) {
                JSONObject searchChainObj = new JSONObject();
                JSONArray searchChainQueries = new JSONArray();
                SearchChain searchChain = SearchChain.fromDatabase(result.getInt("id"), c);
                searchChainObj.put("searchCount", result.getInt("search_count"));
                List<Search> searches = searchChain.getSearches();
                if(searches == null) {
                    continue;
                }
                searchChainQueries.addAll(searches.stream().map(this::getSearchObject).collect(Collectors.toList()));
                searchChainObj.put("queries", searchChainQueries);
                searchChains.add(searchChainObj);
            }

            JSONObject response = new JSONObject();
            response.put("searches", searchChains);

            JsonResponse.writeJsonResponse(response, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonResponse.writeJsonResponse(new JSONObject(), resp);
        }
    }

    private JSONObject getSearchObject(Search s) {
        JSONObject search = new JSONObject();
        search.put("type", s.getSearchType().value);
        search.put("query", s.getQuery());
        return search;
    }
}
