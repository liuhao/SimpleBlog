package SimpleBlog.plugin;

import SimpleBlog.entity.Blog;
import SimpleBlog.entity.NoteResource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
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
        blog.setResources(new HashMap<String, NoteResource>());
        blog.setContent(parseContent(post.select(contentXPath)));
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

    private String parseContent(Elements content) {
        String rtn = "Error";
        Element e = content.get(0);
        if(content.size() == 1) {
            switch (e.className()) {
                case "content text" :
                    rtn = "content text";
                    parseText(e);
                    break;
                case "content image" : rtn = "content image";
                    break;
                case "content video" : rtn = "content video";
                    break;
                case "content audio" : rtn = "content audio";
                    break;
                case "content chat" : rtn = "content chat";
                    break;
                case "content link" : rtn = "content link";
                    break;
                case "content quote" : rtn = "content quote";
                    break;
                default : rtn = "Error";
                    break;
            }
        }
        return rtn;
    }

    private void parseText(Element e) {
        if (e.select("div.go").size() == 1) {
            Element allElement = e.select("div.go").get(0);
            Elements imageElement = allElement.select("figure");
            if (imageElement.size() > 0) {
                for (Element img : imageElement) {
                    NoteResource res = new NoteResource();
                    res.setWidth(Integer.getInteger(img.select("img").attr("width")));
                    res.setHeight(Integer.getInteger(img.select("img").attr("height")));
                    res.setMimeType(img.select("img").attr("src"));
                    byte[] imgBinData = null;
                    res.setSourceUrl(img.select("img").attr("src"));
                    try {
                        imgBinData = fetchRemoteFile(res.getSourceUrl());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    if (imgBinData != null) {
                        res.setData(base64Encode(imgBinData));
                        res.setFileHashcode(calculateResourceHash(imgBinData));
                    }

                }
            }

        }
    }

    public byte[] fetchRemoteFile(String location) throws Exception {
        URL url = new URL(location);
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = url.openStream ();
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e) {
            //handle errors
            e.printStackTrace();
        }
        finally {
            if (is != null) is.close();
        }
        return bytes;
    }

    public String calculateResourceHash(byte[] content)
    {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");


        md.update(content);
        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
            for (byte aByteData : byteData) {
                sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
            }

        System.out.println("Digest(in hex format):: " + sb.toString());

        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
        System.out.println("Digest(in hex format):: " + hexString.toString());

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String base64Encode(byte[] content) {
        Base64 coder = new Base64();
        return coder.encodeToString(content);
    }
}
