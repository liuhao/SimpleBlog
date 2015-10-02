package SimpleBlog.plugin;

import SimpleBlog.entity.Blog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyoo on 9/28/2015.
 */
public class ImportTumblrPostData {
    private static Logger logger = LogManager.getLogger(ImportTumblrPostData.class.getName());

    private static final String TUMBLR_POST_DATA_GET_URL;
    static {
        TUMBLR_POST_DATA_GET_URL = "http://liuhao2012.tumblr.com/page/3";
    }

    private String albumXPath;
    private String postsXPath;
    // the children node of the post node
    private String sourceUrlXPath;
    private String tagsXPath;
    private String contentXPath;
    private String subjectXPath;
    private String dateXPath;
    private DateUtil dateUtil;

    public void setAlbumXPath(String albumXPath) {
        this.albumXPath = albumXPath;
    }

    public void setPostsXPath(String postsXPath) {
        this.postsXPath = postsXPath;
    }

    public void setSourceUrlXPath(String sourceUrlXPath) {
        this.sourceUrlXPath = sourceUrlXPath;
    }

    public void setTagsXPath(String tagsXPath) {
        this.tagsXPath = tagsXPath;
    }

    public void setContentXPath(String contentXPath) {
        this.contentXPath = contentXPath;
    }

    public void setSubjectXPath(String subjectXPath) {
        this.subjectXPath = subjectXPath;
    }

    public void setDateXPath(String dateXPath) {
        this.dateXPath = dateXPath;
    }

    public void setDateUtil(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    public List<Blog> getXmlDocument(String tumblrUrl) {
        if(tumblrUrl.isEmpty())
            tumblrUrl = TUMBLR_POST_DATA_GET_URL;

        try {
            List<Blog> blogs = new ArrayList<Blog>();
            Document doc = Jsoup.connect(tumblrUrl).get();
            Elements posts = doc.select(postsXPath);
            String albumName = doc.select(albumXPath).text();

            if (posts != null) {
                String tempDate = "";
                for (Element p : posts) {
                    Blog blog = parsePost(p);
                    if(blog != null) {
                        if (blog.getCreate().isEmpty()) {
                            blog.setCreate(tempDate);
                            blog.setUpdate(tempDate);
                        } else {
                            tempDate = blog.getCreate();
                        }
                        blogs.add(blog);
                    }
                }
            }
            return blogs;
        } catch (IOException e) {
            logger.catching(e);
            logger.error("no protocol is specified, or an unknown protocol is found, or url is null.");
        }
        return null;
    }

    private Blog parsePost(Element post) {
        Blog blog = new Blog();
        //System.out.println(post.toString());
        blog.setSubject(post.select(subjectXPath).text());
        blog.setContent(post.select(contentXPath).text());
        String d = dateUtil.converTumblrDate(post.select(dateXPath).text());
        blog.setCreate(d);
        blog.setUpdate(d);
        blog.setSource("Tumblr");
        blog.setSourceUrl(post.select(sourceUrlXPath).attr("href"));
        blog.setTags(post.select(tagsXPath).text());
        blog.setAuthor("Hao Liu");

        if (blog.getSubject().isEmpty())
            return null;
        else
            return blog;
    }
}
