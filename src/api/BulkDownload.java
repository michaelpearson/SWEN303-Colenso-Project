package api;

import database.model.TeiDocument;
import util.DocumentZip;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BulkDownload extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] documents = request.getParameterValues("documents[]");
        if(documents == null) {
            String documentIds = request.getParameter("documents");
            if(documentIds != null) {
                documents = documentIds.split(",");
            }
        }
        if(documents == null || documents.length == 0) {
            throw new RuntimeException("You must supply at least one document");
        }
        response.setHeader("content-type", "application/zip");
        List<TeiDocument> teiDocuments = new ArrayList<>();
        for(String document : documents) {
            teiDocuments.add(TeiDocument.fromId(Integer.parseInt(document)));
        }
        DocumentZip.writeDocumentToStream(teiDocuments, response.getOutputStream());
    }
}
