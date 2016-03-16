package api;

import database.client.BaseXClient;
import org.apache.commons.io.output.WriterOutputStream;
import util.DocumentRenderer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@WebServlet(name = "GetDocument")
public class GetDocument extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");

        try {
            int documentId = Integer.parseInt(request.getParameter("documentId"));
            String downloadType = request.getParameter("type");
            downloadType = downloadType == null ? "html" : downloadType;

            boolean forceDownload = request.getParameter("download") != null && request.getParameter("download").equals("true");

            BaseXClient client = BaseXClient.getClient();
            BaseXClient.Query q = client.preparedQuery("for $b in collection()/TEI\n" +
                    "where db:node-id($b)=%d\n" +
                    "return $b", documentId);
            String document = q.next();
            if(document == null) {
                throw new RuntimeException("Document not found");
            }
            switch(downloadType) {
                default:
                case "html":
                    if(forceDownload) {
                        response.setHeader("Content-Disposition", "attachment;filename=document.html");
                    }
                    try {
                        DocumentRenderer.simpleTransform(document, new WriterOutputStream(response.getWriter()));
                    } catch (TransformerException e) {
                        throw new RuntimeException("Error rendering document");
                    }

                    break;
                case "xml":
                    if(forceDownload) {
                        response.setHeader("Content-Disposition", "attachment;filename=document.xml");
                    }
                    response.setHeader("content-type", "application/xml");
                    response.getWriter().print(document);
                    break;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Document ID must be supplied");
        }
    }
}
