package SimpleBlog;

import SimpleBlog.entity.Blog;
import SimpleBlog.plugin.ConvertToEvernote;
import SimpleBlog.plugin.DateUtil;
import SimpleBlog.plugin.YahooWeatherData;
import org.dom4j.io.XMLWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        ApplicationContext context;
        context = new ClassPathXmlApplicationContext("spring.xml");

        ConvertToEvernote cte = (ConvertToEvernote)context.getBean("convertToEvernote");
        YahooWeatherData data = (YahooWeatherData)context.getBean("yahooData");
        DateUtil date = new DateUtil();
        Blog blog = new Blog();

        blog.setAuthor("Hao Liu");
        blog.setSubject(date.getTextDate() + " " + data.getWeather() + " " + data.getLocation());
        blog.setCreate(date.getEvernoteDate());
        blog.setUpdate(date.getEvernoteDate());
        blog.setTag("@2015 Diary");

        XMLWriter writer = new XMLWriter(new OutputStreamWriter(System.out, "UTF-8"));
        System.out.println("/r/n------------------Start------------------");
        writer.write(cte.exportEvernoteXml(blog));
        System.out.println("/r/n-------------------End-------------------");
        writer.close();

        cte.createEnex(blog);
    }
}
