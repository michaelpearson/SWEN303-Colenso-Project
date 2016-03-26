package endpoints;

import database.model.TeiDocument;
import org.json.simple.JSONObject;
import util.JsonResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TagDocuments extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] documents = req.getParameterValues("ids[]");
        String tagType = req.getParameter("tagType");
        String tagValue = req.getParameter("tagValue");
        int ids[] = new int[documents.length];
        int a = 0;
        for(String documentId : documents) {
            TeiDocument.fromId(Integer.valueOf(documentId)).tag(tagType, tagValue);
        }
        JSONObject response = new JSONObject();
        response.put("success", true);
        JsonResponse.writeJsonResponse(response, resp);
    }
}
