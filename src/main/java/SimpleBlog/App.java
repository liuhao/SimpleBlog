package SimpleBlog;

import SimpleBlog.entity.Blog;
import SimpleBlog.plugin.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

/** Hello world! */
public class App {

  public static void main(String[] args) {
    ApplicationContext context;
    context = new ClassPathXmlApplicationContext("spring.xml");
    String arg = args.length > 0 ? args[0] : "";
    switch (arg) {
      case "tumblr":
        tumblrImport(context);
        break;
      case "new":
        newExportFile(context);
        break;
      case "mail":
        sendMail(context);
        break;
      case "mailAPI":
        sendMailAPI(context);
        break;
      default:
        System.out.println(
            "java -cp Blog-1.0-SNAPSHOT.jar SimpleBlog.App [ new | tumblr | mail | mailAPI ]");
        break;
    }

    /*
     * try {
     * byte[] c = importTumblr.fetchRemoteFile("file:///d|/birthday.png");
     * importTumblr.calculateResourceHash(c);
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     */
  }

  private static Blog newNote(WeatherData data, DateUtil date) {
    Blog blog = new Blog();
    blog.setAuthor("Hao Liu");
    blog.setSubject(date.getTextDate() + " " + data.getWeather() + " " + data.getLocation());
    blog.setContent("What a wonderful day. 美好的一天。");
    blog.setCreate(date.getEvernoteDate());
    blog.setUpdate(date.getEvernoteDate());
    String tags = "@" + LocalDate.now().get(ChronoField.YEAR) + " Diary";
    blog.setTags(tags);
    return blog;
  }

  private static void sendMail(ApplicationContext context) {
    ConvertToEvernote cte = (ConvertToEvernote) context.getBean("convertToEvernote");
    // YahooWeatherData data = (YahooWeatherData)
    // context.getBean("yahooData");
    OpenWeatherMapData data = (OpenWeatherMapData) context.getBean("openweathermapData");
    DateUtil date = (DateUtil) context.getBean("dateUtil");
    MailToEvernote mailToEvernote = (MailToEvernote) context.getBean("mailToEvernote");
    Blog blog = newNote(data, date);

    mailToEvernote.send(blog);
  }

  private static void sendMailAPI(ApplicationContext context) {
    ConvertToEvernote cte = (ConvertToEvernote) context.getBean("convertToEvernote");
    // YahooWeatherData data = (YahooWeatherData)
    // context.getBean("yahooData");
    OpenWeatherMapData data = (OpenWeatherMapData) context.getBean("openweathermapData");
    DateUtil date = (DateUtil) context.getBean("dateUtil");
    MailToEvernote mailToEvernote = (MailToEvernote) context.getBean("mailToEvernote");
    Blog blog = newNote(data, date);

    mailToEvernote.sendBySendgridAPI(blog);
  }

  private static void newExportFile(ApplicationContext context) {
    ConvertToEvernote cte = (ConvertToEvernote) context.getBean("convertToEvernote");
    // YahooWeatherData data = (YahooWeatherData)
    // context.getBean("yahooData");
    OpenWeatherMapData data = (OpenWeatherMapData) context.getBean("openweathermapData");
    DateUtil date = (DateUtil) context.getBean("dateUtil");

    Blog blog = newNote(data, date);
    List<Blog> blogs = new ArrayList<>();
    blogs.add(blog);

    if (new File("./NewExport.enex").exists()) {
      cte.updateEnex(blogs, "NewExport.enex");
    } else {
      cte.createEnex(blogs, "NewExport.enex");
    }
  }

  private static void tumblrImport(ApplicationContext context) {
    ConvertToEvernote cte = (ConvertToEvernote) context.getBean("convertToEvernote");
    ImportTumblrPostData importTumblr =
        (ImportTumblrPostData) context.getBean("importTumblrPostData");
    String tumblrUrl = "http://liuhao2012.tumblr.com/page/";
    for (int i = 40; i < 41; i++) {
      System.out.println(i);
      cte.updateEnex(importTumblr.getXmlDocument(tumblrUrl + i), "TumblrPostExport.enex");
    }
  }
}
