package api;

import database.BaseXClient;
import database.SearchQueryProcessor;
import org.basex.BaseX;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import util.Response;
import util.Strings;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "SearchDocuments")
public class SearchDocuments extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject resp = new JSONObject();
        BaseXClient client = BaseXClient.getClient();

        String searchType = request.getParameter("type");
        String searchQuery = request.getParameter("query");
        int page = Integer.valueOf(request.getParameter("page")) - 1;
        int count = Integer.valueOf(request.getParameter("count"));
        int startIndex = page * count;

        searchQuery = searchQuery == null ? "" : searchQuery;

        BaseXClient.Query q;

        if(searchType == null || searchType.equals("") || searchQuery.equals("")) {
            q = client.query("for $x in collection()/TEI\n" +
                    "return <data>\n" +
                    "         <id>{db:node-id($x)}</id>\n" +
                    "         <title>{$x/teiHeader//title/string()}</title>\n" +
                    "         <date>{$x/teiHeader//date/string()}</date>\n" +
                    "       </data>");
        } else {
            switch(searchType) {
                case "fulltext":
                default:
                    q = client.preparedQuery("for $x in collection()/TEI where $x//text() contains text \"%s\" using fuzzy return " +
                            "<data>" +
                            "<id>{db:node-id($x)}</id>" +
                            "<title>{$x/teiHeader//title/string()}</title>" +
                            "<date>{$x/teiHeader//date/string()}</date>" +
                            "</data>", Strings.addSlashes(searchQuery));
                    break;
                case "xquery":
                    q = client.preparedQuery("for $x in collection()/TEI where $x%s return " +
                            "<data>" +
                            "<id>{db:node-id($x)}</id>" +
                            "<title>{$x/teiHeader//title/string()}</title>" +
                            "<date>{$x/teiHeader//date/string()}</date>" +
                            "</data>", Strings.addSlashes(searchQuery));
                    break;

                case "logical":
                    String search = SearchQueryProcessor.processQuery(searchQuery);
                    System.out.println(search);
                    q = client.preparedQuery("for $x in collection()/TEI " +
                            "where $x/string() contains text %s" +
                            "return " +
                            "<data>" +
                            "<id>{db:node-id($x)}</id>" +
                            "<title>{$x/teiHeader//title/string()}</title>" +
                            "<date>{$x/teiHeader//date/string()}</date>" +
                            "</data>",  search);
                    break;
            }
        }
        String row;

        JSONArray documents = new JSONArray();
        int i = 0;
        try {
            while ((row = q.next()) != null) {
                if(i++ < startIndex) {
                    continue;
                }
                if(i > startIndex + count) {
                    continue;
                }
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
        } catch (IOException e) {
            resp.put("error", true);
            resp.put("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        resp.put("total", i);
        resp.put("start", startIndex);
        resp.put("end", startIndex + count > i ? i : startIndex + count);

        Response.writeJsonResponse(resp, response);
        client.close();
    }
}
