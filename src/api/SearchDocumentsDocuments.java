package api;

import database.BaseXClient;
import org.basex.BaseX;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import util.Response;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SearchDocumentsDocuments extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject resp = new JSONObject();
        BaseXClient client = BaseXClient.getClient();

        String searchType = request.getParameter("type");
        String searchQuery = request.getParameter("query");

        searchQuery = searchQuery == null ? "" : searchQuery;

        if(searchType == null || searchType.equals("")) {
            BaseXClient.Query q = client.query("for $x in collection()/TEI\n" +
                    "return <data>\n" +
                    "         <id>{$x/@xml:id/string()}</id>\n" +
                    "         <title>{$x/teiHeader//title/string()}</title>\n" +
                    "         <date>{$x/teiHeader//date/string()}</date>\n" +
                    "       </data>");
            String row;
            JSONArray documents = new JSONArray();

            while((row = q.next()) != null) {
                Document dom = Jsoup.parse(row, "", Parser.xmlParser());
                String id = dom.getElementsByTag("id").first().text();
                String title = dom.getElementsByTag("title").first().text();
                String date = dom.getElementsByTag("date").first().text();
                JSONObject document = new JSONObject();
                document.put("id", id);
                document.put("title", title);
                document.put("date", date);
                documents.add(document);
            }
            resp.put("documents", documents);
        } else {

        }

        Response.writeJsonResponse(resp, response);
        client.close();
    }
}
