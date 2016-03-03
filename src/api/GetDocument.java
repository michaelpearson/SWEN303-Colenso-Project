package api;

import database.BaseXClient;
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

@WebServlet(name = "GetDocument")
public class GetDocument extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String documentId = request.getParameter("documentId");
        if(documentId == null || documentId.equals("")) {
            throw new IllegalArgumentException("Document ID must be supplied");
        }
        BaseXClient client = BaseXClient.getClient();

        BaseXClient.Query q = client.preparedQuery("for $b in collection()\n" +
                "where $b//TEI[@xml:id=\"%s\"]\n" +
                "return $b", documentId);

        Document d = Jsoup.parse(q.next(), "", Parser.xmlParser());

        JSONObject resp = new JSONObject();
        resp.put("body", d.getElementsByTag("body").first().html());
        Response.writeJsonResponse(resp, response);
    }
}
