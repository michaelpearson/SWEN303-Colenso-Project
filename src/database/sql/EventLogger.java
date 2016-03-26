package database.sql;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Map;

public abstract  class EventLogger<E> {
    public static int extractMemberToken(Cookie[] cookies) {
        if(cookies == null) {
            return -1;
        }
        for(Cookie c : cookies) {
            if(c.getName().equals("memberid")) {
                return Integer.parseInt(c.getValue());
            }
        }
        return -1;
    }

    public void logEvent(HttpServletRequest request, HttpServletResponse response, E event, Map<String, Object> extra) {
        Connection c = Database.getConnection();
        int memberToken = extractMemberToken(request.getCookies());

        if(memberToken == -1) {
            try {
                PreparedStatement query = c.prepareStatement("INSERT INTO MEMBERS VALUES(NULL, 'New Member')",Statement.RETURN_GENERATED_KEYS);
                query.executeUpdate();
                ResultSet result = query.getGeneratedKeys();
                if(result != null && result.next()) {
                    memberToken = result.getInt(1);
                    Cookie cookie = new Cookie("memberid", String.format("%d", memberToken));
                    cookie.setMaxAge(Integer.MAX_VALUE);
                    response.addCookie(cookie);
                }
            } catch(SQLException e) {
                e.printStackTrace();
                return;
            }
        }

        logEvent(memberToken, event, c, extra);
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logEvent(HttpServletRequest request, HttpServletResponse response, E event) {
        logEvent(request, response, event, (Map<String, Object>)null);
    }

    protected abstract void logEvent(int memberToken, E event, Connection c, Map<String, Object> extra);
}
