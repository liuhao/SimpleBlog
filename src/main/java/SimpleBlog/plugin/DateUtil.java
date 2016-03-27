package SimpleBlog.plugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by lyoo on 9/25/2015.
 */
public class DateUtil {
    public String getTextDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd, E.");
        return f.format(c.getTime());
    }

    public String getEvernoteDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        return f.format(c.getTime());
    }

    public String converTumblrDate(String date) {
        if (date.isEmpty())
            return "";
        Calendar c = Calendar.getInstance();
        String dateString = date.replaceFirst("(^[0-9]+)[a-zA-Z]+ ", "$1 ");
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy HH:mm:ss");
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        try {
            c.setTime(sdf.parse(dateString + " 14:09:59"));
            return f.format(c.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
