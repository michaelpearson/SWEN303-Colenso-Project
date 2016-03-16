package api;

import database.client.BaseXClient;
import database.documents.Search;
import database.model.TeiDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import util.DocumentZip;
import util.Response;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "SearchDocuments")
public class SearchDocuments extends HttpServlet {

    private static Integer tryParseInt(String value, Integer defaultValue) {
        try {
            defaultValue = Integer.parseInt(value);
        } catch (NumberFormatException ignore) {}
        return defaultValue;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchType = request.getParameter("type");
        String searchQuery = request.getParameter("query");
        int page = tryParseInt(request.getParameter("page"), 1) - 1;
        int count = tryParseInt(request.getParameter("count"), 0);
        boolean download = "1".equals(request.getParameter("download"));
        int startIndex = page * count;
        searchQuery = searchQuery == null ? "" : searchQuery;

        Search search = new Search(searchType, searchQuery);
        List<TeiDocument> searchResults = search.executeQuery();

        if(download) {
            DocumentZip.writeDocumentToStream(searchResults, response.getOutputStream());
        } else {
            JSONObject resp = new JSONObject();
            int i = 0;
            JSONArray documents = new JSONArray();
            for(int a = startIndex; a < startIndex + count;a++) {
                JSONObject document = new JSONObject();
                if(searchResults.size() > a && searchResults.get(a) != null) {
                    document.put("id", searchResults.get(a).getId());
                    document.put("title", searchResults.get(a).getTitle());
                    document.put("date", searchResults.get(a).getDate());
                    documents.add(document);
                    i++;
                }
            }
            resp.put("total", searchResults.size());
            resp.put("start", startIndex);
            resp.put("end", startIndex + i);
            resp.put("documents", documents);
            Response.writeJsonResponse(resp, response);

        }
    }
}
