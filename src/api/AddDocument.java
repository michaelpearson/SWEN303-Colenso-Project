package api;

import database.client.BaseXClient;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.Response;
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

                BaseXClient client = BaseXClient.getClient();
                String path = UUID.randomUUID().toString() + "/" + file.getName();
                System.out.println(client.preparedCommand("ADD to %s %s", path, writer.toString()));
                if(!writer.toString().equals("")) {
                    BaseXClient.Query q = client.preparedQuery("db:open(\"%s\", \"%s\")/db:node-id(TEI)", ServerConfiguration.getConfigurationString("database", "name"), path);
                    String documentId = q.next();
                    if(documentId != null) {
                        documentsAdded.add(Integer.valueOf(documentId));
                    }
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        JSONObject resp = new JSONObject();
        resp.put("documentsAdded", documentsAdded);
        Response.writeJsonResponse(resp, response);
    }
}
