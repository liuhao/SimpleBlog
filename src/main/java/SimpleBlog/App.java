package SimpleBlog;

import SimpleBlog.entity.Blog;
import SimpleBlog.plugin.ConvertToEvernote;
import SimpleBlog.plugin.DateUtil;
import SimpleBlog.plugin.ImportTumblrPostData;
import SimpleBlog.plugin.YahooWeatherData;
import org.dom4j.io.XMLWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        ApplicationContext context;
        context = new ClassPathXmlApplicationContext("spring.xml");

        ConvertToEvernote cte = (ConvertToEvernote) context.getBean("convertToEvernote");
        YahooWeatherData data = (YahooWeatherData) context.getBean("yahooData");
        ImportTumblrPostData importTumblr = (ImportTumblrPostData) context.getBean("importTumblrPostData");
        DateUtil date = (DateUtil) context.getBean("dateUtil");
        Blog blog = new Blog();

        blog.setAuthor("Hao Liu");
        blog.setSubject(date.getTextDate() + " " + data.getWeather() + " " + data.getLocation());
        blog.setContent("What a wondful day. 美好的一天。");
        blog.setCreate(date.getEvernoteDate());
        blog.setUpdate(date.getEvernoteDate());
        blog.setTags("@2015 Diary");

        //XMLWriter writer = new XMLWriter(new OutputStreamWriter(System.out, "UTF-8"));
        //writer.write(cte.exportEvernoteXml(blog));
        //writer.close();
/*
        List<Blog> blogs = new ArrayList<Blog>();
        blogs.add(blog);

        if (new File("./NewExport.enex").exists())
            cte.updateEnex(blogs, "NewExport.enex");
        else
            cte.createEnex(blogs, "NewExport.enex");

*/
        String tumblrUrl = "http://liuhao2012.tumblr.com/page/";
        for (int i = 1; i < 2; i++) {
            System.out.println(i);
            cte.updateEnex(importTumblr.getXmlDocument(tumblrUrl + String.valueOf(i)),
                "TumblrPostExport.enex");
        }
/*
        try {
            byte[] c = importTumblr.fetchRemoteFile("file:///d|/birthday.png");
            importTumblr.calculateResourceHash(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }
}
