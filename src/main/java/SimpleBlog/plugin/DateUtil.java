package SimpleBlog.plugin;

import java.util.Calendar;

/**
 * Created by lyoo on 9/25/2015.
 */
public class DateUtil {
    public String getTextDate() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy.MM.dd");
        return f.format(c.getTime());
    }

    public String getEvernoteDate() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        return String.valueOf(c.get(Calendar.YEAR)) +
                String.valueOf(c.get(Calendar.MONTH)) +
                String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1) + "T" +
                String.valueOf(c.get(Calendar.HOUR_OF_DAY)) +
                String.valueOf(c.get(Calendar.MINUTE)) +
                String.valueOf(c.get(Calendar.SECOND)) + "Z";
    }
}
