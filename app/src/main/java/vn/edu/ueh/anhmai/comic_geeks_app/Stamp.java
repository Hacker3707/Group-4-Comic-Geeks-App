
package vn.edu.ueh.anhmai.comic_geeks_app;

import java.util.Date;

public class Stamp {
    private String stampId;
    private String userId;
    private StampType type;
    private Date timestamp;

    public Stamp(String stampId, String userId, StampType type) {
        this.stampId = stampId;
        this.userId = userId;
        this.type = type;
        this.timestamp = new Date();
    }

    public void changeStampType(StampType newType) {
        this.type = newType;
        this.timestamp = new Date();
    }


    public StampType getType() { return type; }
    public String getUserId() { return userId; }
}