package database.sql;

import database.model.TeiDocument;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class DocumentViewLogger extends EventLogger<TeiDocument> {

    @Override
    protected void logEvent(HttpServletRequest request, TeiDocument document, Connection c, Map<String, Object> extra) {
        if(document == null) {
            return;
        }
        try {
            PreparedStatement statement = c.prepareStatement("INSERT INTO DOCUMENT_EVENTS VALUES(null, ?, ?, ?, ?);");
            statement.setInt(1, document.getId());

            DocumentEventType eventType = DocumentEventType.VIEW;
            if(extra != null && extra.get("eventType") != null) {
                eventType = (DocumentEventType)extra.get("eventType");
            }
            statement.setInt(2, eventType.DB_TYPE);

            statement.setLong(3, new Date().getTime());
            statement.setInt(4, extractMemberToken(request.getCookies()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public enum DocumentEventType {
        VIEW (1);
        public final int DB_TYPE;
        DocumentEventType(int type) {
            DB_TYPE = type;
        }
    }
}
