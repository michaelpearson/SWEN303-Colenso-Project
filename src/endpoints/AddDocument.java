package endpoints;

import database.model.TeiDocument;
import database.xml.client.BaseXClient;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.JsonResponse;
import util.ServerConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class AddDocument extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setSizeMax(1024 * 1024 * 5);

        JSONArray documentsAdded = new JSONArray();
        try {
            List<FileItem> files = upload.parseRequest(request);
            for(FileItem file : files) {
                int c;
                InputStream is = file.getInputStream();
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer, Charset.defaultCharset());
                TeiDocument document = TeiDocument.insertFromXml(writer.toString(), file.getName());
                if(document != null) {
                    documentsAdded.add(document.getId());
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        JSONObject resp = new JSONObject();
        resp.put("documentsAdded", documentsAdded);
        JsonResponse.writeJsonResponse(resp, response);
    }
}
