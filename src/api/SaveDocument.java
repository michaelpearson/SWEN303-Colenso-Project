package api;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import util.Response;
import util.ServerConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class SaveDocument extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean save = "1".equals(req.getParameter("save"));
        int documentId = 0;
        if(save) {
            documentId = Integer.parseInt(req.getParameter("id"));
        }
        String xml = req.getParameter("xml");



        final JSONArray errors = new JSONArray();
        final JSONArray fatalErrors = new JSONArray();
        final JSONArray warnings = new JSONArray();

        PropertyMapBuilder builder = new PropertyMapBuilder();
        builder.put(ValidateProperty.ERROR_HANDLER, new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                addExceptionToResponse(warnings, exception);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                addExceptionToResponse(errors, exception);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                addExceptionToResponse(fatalErrors, exception);
            }
        });

        InputSource inRng = ValidationDriver.fileInputSource(ServerConfiguration.getConfigurationString("rng"));
        inRng.setEncoding("UTF-8");
        InputSource inXml = new InputSource(new ByteArrayInputStream(xml.getBytes()));
        inXml.setEncoding("UTF-8");
        ValidationDriver driver = new ValidationDriver(builder.toPropertyMap(), new AutoSchemaReader());
        boolean isValid = false;
        try {
            driver.loadSchema(inRng);
            isValid = driver.validate(inXml);
        } catch (SAXException e) {
            isValid = false;
        }


        JSONObject response = new JSONObject();
        response.put("errors", errors);
        response.put("fatalErrors", fatalErrors);
        response.put("warnings", warnings);
        response.put("valid", isValid);
        Response.writeJsonResponse(response, resp);
    }
    private void addExceptionToResponse(JSONArray parent, SAXParseException e) {
        JSONObject obj = new JSONObject();
        obj.put("lineNumber", e.getLineNumber());
        obj.put("description", e.getMessage());
        parent.add(obj);
    }


}
