package database.sql;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public abstract  class EventLogger<E> {
    static int extractMemberToken(Cookie[] cookies) {
        for(Cookie c : cookies) {
            if(c.getName().equals("memberid")) {
                return Integer.parseInt(c.getValue());
            }
        }
        return 0;
    }

    public void logEvent(HttpServletRequest request, E event, Map<String, Object> extra) {
        Connection c = Database.getConnection();
        logEvent(request, event, c, extra);
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logEvent(HttpServletRequest request, E event) {
        logEvent(request, event, (Map<String, Object>)null);
    }

    protected abstract void logEvent(HttpServletRequest request, E event, Connection c, Map<String, Object> extra);
}
