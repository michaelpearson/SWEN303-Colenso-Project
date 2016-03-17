package api;

import database.model.TeiDocument;

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
            TeiDocument document = TeiDocument.fromId(documentId);

            switch(downloadType) {
                default:
                case "html":
                    if(forceDownload) {
                        response.setHeader("Content-Disposition", "attachment;filename=document.html");
                    }
                    try {
                        response.getWriter().print(document.renderHTML());
                    } catch(TransformerException e) {
                        e.printStackTrace();
                    }
                    break;
                case "xml":
                    if(forceDownload) {
                        response.setHeader("Content-Disposition", "attachment;filename=document.xml");
                    }
                    response.setHeader("content-type", "application/xml");
                    response.getWriter().print(document.getXmlData());
                    break;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Document ID must be supplied");
        }
    }
}
