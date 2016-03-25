package endpoints;

import database.model.Search;
import database.model.SearchChain;
import database.model.TeiDocument;
import database.sql.Database;
import database.sql.SearchLogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.DocumentZip;
import util.JsonResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SearchDocuments extends HttpServlet {

    private static Integer tryParseInt(String value, Integer defaultValue) {
        try {
            defaultValue = Integer.parseInt(value);
        } catch (NumberFormatException ignore) {}
        return defaultValue;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SearchChain searchChain = new SearchChain();
        if(request.getParameter("chained") != null && request.getParameter("chained").equals("1")) {
            String[] searchTypes = request.getParameterValues("type[]");
            String[] searchQueries = request.getParameterValues("query[]");
            if(searchTypes != null && searchQueries != null && searchTypes.length == searchQueries.length) {
                for(int a = 0;a < searchQueries.length;a++) {
                    searchChain.addSearch(new Search(searchTypes[a], searchQueries[a]));
                }
            } else {
                throw new RuntimeException("Invalid arguments");
            }
        } else {
            String searchType = request.getParameter("type");
            String searchQuery = request.getParameter("query");
            if(searchType != null) {
                searchQuery = searchQuery == null ? "" : searchQuery;
                searchChain.addSearch(new Search(searchType, searchQuery));
            } else {
                throw new RuntimeException("Invalid arguments");
            }
        }
        int page = tryParseInt(request.getParameter("page"), 1) - 1;
        int count = tryParseInt(request.getParameter("count"), 0);
        boolean download = "1".equals(request.getParameter("download"));
        int startIndex = page * count;

        List<TeiDocument> searchResults = searchChain.executeSearch();
        new SearchLogger().logEvent(request, searchChain);

        if(download) {
            response.setHeader("Content-Disposition", "filename=search.zip");
            DocumentZip.writeDocumentToStream(searchResults, response.getOutputStream());
        } else {
            Connection c = Database.getConnection();
            JSONObject resp = new JSONObject();
            int i = 0;
            JSONArray documents = new JSONArray();
            for(int a = startIndex; a < startIndex + count;a++) {
                JSONObject document = new JSONObject();
                if(searchResults.size() > a && searchResults.get(a) != null) {
                    document.put("id", searchResults.get(a).getId());
                    document.put("title", searchResults.get(a).getTitle());
                    document.put("date", searchResults.get(a).getDate());
                    try {
                        document.put("viewCount", searchResults.get(a).getViewCount(c));
                    } catch (SQLException e) {
                        document.put("viewCount", 0);
                    }
                    documents.add(document);
                    i++;
                }
            }
            try {
                c.close();
            } catch (SQLException ignore) {}

            resp.put("total", searchResults.size());
            resp.put("start", startIndex);
            resp.put("end", startIndex + i);
            resp.put("documents", documents);
            JsonResponse.writeJsonResponse(resp, response);
        }
    }
}
