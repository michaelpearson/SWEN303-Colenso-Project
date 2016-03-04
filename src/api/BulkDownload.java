package api;

import database.BaseXClient;
import org.apache.commons.io.output.WriterOutputStream;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BulkDownload extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BaseXClient client = BaseXClient.getClient();
        String[] documents = request.getParameterValues("documents[]");
        if(documents == null || documents.length == 0) {
            throw new RuntimeException("You must supply at least one document");
        }
        response.setHeader("content-type", "application/zip");


        ZipOutputStream os = new ZipOutputStream(response.getOutputStream());

        for(String documentId : documents) {
            String documentQuery = String.format("db:node-id($x) = %d", Integer.parseInt(documentId));

            BaseXClient.Query q = client.preparedQuery("for $x in /TEI where %s return $x", documentQuery);
            String document = q.next();

            q = client.preparedQuery("for $x in /TEI where %s return file:name(fn:base-uri($x))", documentQuery);
            String fileName = q.next();

            if(document == null || fileName == null) {
                throw new RuntimeException("Could not find one or more documents");
            }

            os.putNextEntry(new ZipEntry(fileName));
            os.write(document.getBytes());
        }
        os.close();
    }
}
