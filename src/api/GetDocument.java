package api;

import database.BaseXClient;
import org.apache.commons.io.output.WriterOutputStream;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import util.DocumentRenderer;
import util.Response;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

@WebServlet(name = "GetDocument")
public class GetDocument extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");

        String documentId = request.getParameter("documentId");
        if(documentId == null || documentId.equals("")) {
            throw new IllegalArgumentException("Document ID must be supplied");
        }
        BaseXClient client = BaseXClient.getClient();

        BaseXClient.Query q = client.preparedQuery("for $b in collection()\n" +
                "where $b//TEI[@xml:id=\"%s\"]\n" +
                "return $b", documentId);

        try {
            DocumentRenderer.simpleTransform(q.next(), new WriterOutputStream(response.getWriter()));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
