package SimpleBlog.plugin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Date utilities Created by lyoo on 9/25/2015. */
public class DateUtil {
  public String getTextDate() {
    LocalDate localDate = LocalDate.now();
    return localDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd, E."));
  }

  public String getEvernoteDate() {
    LocalDateTime localDateTime = LocalDateTime.now();
    return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
  }

  String converTumblrDate(String date) {
    if (date.isEmpty()) return "";
    String dateString = date.replaceFirst("(^[0-9]+)[a-zA-Z]+ ", "$1 ");
    try {
      LocalDateTime localDateTime =
          LocalDateTime.parse(
              dateString + " 14:09:59", DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss"));
      return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    } catch (DateTimeParseException e) {
      e.printStackTrace();
    }
    return null;
  }
}
