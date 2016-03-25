package endpoints;

import database.sql.Database;
import database.xml.client.BaseXClient;
import org.basex.BaseXServer;
import org.json.simple.JSONObject;
import util.JsonResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

public class GetServerStatus extends HttpServlet {

    private static BaseXServer baseXserver;

    static {
        try {
            baseXserver = new BaseXServer();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error communication with databases");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean success;
        JSONObject resp = new JSONObject();
        try {
            BaseXClient client = BaseXClient.getClient();
            client.close();
            Database.getConnection();
            success = true;
        } catch (Exception e) {
            success = false;
            resp.put("error", e.getMessage());
        }
        resp.put("success", success);
        JsonResponse.writeJsonResponse(resp, response);
    }
}
