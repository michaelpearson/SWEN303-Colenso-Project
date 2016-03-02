package util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Response {
    public static void writeJsonResponse(@Nullable JSONObject jsonResponse, @NotNull HttpServletResponse response) throws IOException {
        response.setHeader("content-type", "application/json");
        if(jsonResponse != null) {
            response.getWriter().print(jsonResponse.toJSONString());
        } else {
            response.getWriter().print("null");
        }
    }
}
