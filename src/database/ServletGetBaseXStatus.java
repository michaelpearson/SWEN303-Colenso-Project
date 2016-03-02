package database;

import org.basex.BaseXServer;
import org.json.simple.JSONObject;
import util.Response;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ServletGetBaseXStatus")
public class ServletGetBaseXStatus extends HttpServlet {

    private static BaseXServer server;

    static {
        try {
            server = new BaseXServer();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error communication with server");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean success;
        JSONObject resp = new JSONObject();
        try {
            BaseXClient client = BaseXClient.getClient();
            client.close();
            success = true;
        } catch (Exception e) {
            success = false;
            resp.put("error", e.getMessage());
        }
        resp.put("success", success);
        Response.writeJsonResponse(resp, response);
    }
}
