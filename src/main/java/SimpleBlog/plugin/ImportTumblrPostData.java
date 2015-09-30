package SimpleBlog.plugin;

import SimpleBlog.entity.Blog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;
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

    public Document getXmlDocument() {
        String url = TUMBLR_POST_DATA_GET_URL;

        try {
            Document doc = Jsoup.connect(url).get();
            Elements posts = doc.select(postsXPath);
            String albumName = doc.select(albumXPath).text();

            if (posts != null) {
                List<Blog> blogs = new ArrayList<Blog>();
                for (Element p : posts) {
                    blogs.add(parsePost(p));
                }
            }
        } catch (IOException e) {
            logger.catching(e);
            logger.error("no protocol is specified, or an unknown protocol is found, or url is null.");
        }
        return null;
    }

    private Blog parsePost(Element post) {
        Blog blog = new Blog();
        System.out.println(post.toString());
        blog.setSubject(post.select(subjectXPath).text());
        blog.setContent(post.select(contentXPath).text());
        String d = dateUtil.converTumblrDate(post.select(dateXPath).text());
        blog.setCreate(d);
        blog.setUpdate(d);
        blog.setSource("Tumblr");
        blog.setSourceUrl(post.select(sourceUrlXPath).attr("href"));
        blog.setTags(post.select(tagsXPath).text());
        blog.setAuthor("Hao Liu");
        return blog;
    }
}
