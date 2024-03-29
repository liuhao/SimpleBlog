package simpleblog.plugin;

import simpleblog.entity.Blog;
import simpleblog.entity.NoteResource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Convert Tumblr Post to simpleblog Post. Created by lyoo on 9/28/2015. */
public class ImportTumblrPostData {

  private static final Logger logger = LogManager.getLogger(ImportTumblrPostData.class.getName());

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

  public void setDateUtil(DateUtil dateUtil1) {
    this.dateUtil = dateUtil1;
  }

  public List<Blog> getXmlDocument(String tumblrUrl) {
    if (tumblrUrl.isEmpty()) tumblrUrl = TUMBLR_POST_DATA_GET_URL;

    try {
      List<Blog> blogs = new ArrayList<>();
      Document doc = Jsoup.connect(tumblrUrl).get();
      Elements posts = doc.select(postsXPath);
      String albumName = doc.select(albumXPath).text();

      if (posts != null) {
        String tempDate = "";
        for (Element p : posts) {
          Blog blog = parsePost(p);
          if (blog != null) {
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
    blog.setSubject(post.select(subjectXPath).text());
    blog.setResources(new HashMap<>());
    blog.setContent(parseContent(post.select(contentXPath), blog.getResources()));
    String d = dateUtil.converTumblrDate(post.select(dateXPath).text());
    blog.setCreate(d);
    blog.setUpdate(d);
    blog.setSource("Tumblr");
    blog.setSourceUrl(post.select(sourceUrlXPath).attr("href"));
    blog.setTags(post.select(tagsXPath).text());
    blog.setAuthor("Hao Liu");

    if (blog.getSubject().isEmpty()) blog.setSubject("Title");

    return blog;
  }

  private String parseContent(Elements content, HashMap<String, NoteResource> resMap) {
    StringBuilder rtn = new StringBuilder();
    Element e = content.get(0);
    if (content.size() == 1) {
      switch (e.className()) {
        case "content text":
          rtn.append(StringEscapeUtils.escapeXml10(e.select("div.go").text()));
          rtn.append(parseText(e, resMap));
          break;
        case "content image":
          rtn.append(StringEscapeUtils.escapeXml10(e.select("div.description").text()));
          rtn.append(parseImage(e, "a[rel]", resMap));
          break;
        case "content video":
          rtn.append(StringEscapeUtils.escapeXml10(e.select("div.description").text()));

          Elements imageElements =
              parseIFrame(e.select("iframe.photoset").attr("src"), "div.photoset");
          if (imageElements != null)
            rtn.append(parseImage(imageElements.first(), "a.photoset_photo", resMap));
          else {
            Elements videoElements =
                parseIFrame(e.select("iframe.tumblr_video_iframe").attr("src"), "video.crt-video");
            if (videoElements != null)
              rtn.append(parseVideo(videoElements.first(), "source", resMap));
          }
          break;
        case "content audio":
          rtn.append("content audio");
          break;
        case "content chat":
          rtn.append("content chat");
          break;
        case "content link":
          rtn.append("content link");
          break;
        case "content quote":
          rtn.append("content quote");
          break;
        default:
          rtn.append("Error");
          break;
      }
    }
    return rtn.toString();
  }

  private String parseText(Element e, HashMap<String, NoteResource> resMap) {
    StringBuilder resContent = new StringBuilder();
    if (e.select("div.go").size() == 1) {
      Element allElement = e.select("div.go").get(0);
      Elements imageElement = allElement.select("figure");
      if (imageElement.size() > 0) {
        for (Element img : imageElement) {
          NoteResource res = new NoteResource();
          res.setWidth(img.select("img").attr("width"));
          res.setHeight(img.select("img").attr("height"));
          String fileExtension = extractFileExtension(img.select("img").attr("src"));
          res.setMimeType("image/" + fileExtension);
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
            res.setFileName(res.getFileHashcode() + "." + fileExtension);
          }
          resMap.put(res.getFileHashcode(), res);
          resContent
              .append(
                  "<div style=\"margin-block-start:;margin-block-end:;-moz-margin-start:;-moz-margin-end:;margin-top:0px;margin-bottom:0px;\">\n"
                      + "\t<en-media width=\"")
              .append(res.getWidth())
              .append("\" height=\"")
              .append(res.getHeight())
              .append("\" alt=\"image\" hash=\"")
              .append(res.getFileHashcode())
              .append("\" type=\"")
              .append(res.getMimeType())
              .append("\" style=\"max-width:400px;\"/>\n")
              .append("</div>\n");
        }
      } else {
        imageElement = allElement.select("img");
        if (imageElement.size() > 0) {
          for (Element img : imageElement) {
            parseTextPostImage(img, "src", resMap, resContent);
          }
        }
      }
    }
    return resContent.toString();
  }

  private String parseTextPostImage(
      Element img,
      String attribute,
      HashMap<String, NoteResource> resMap,
      StringBuilder resContent) {
    if (img != null) {
      NoteResource res = new NoteResource();
      String fileExtension = extractFileExtension(img.attr("src"));
      res.setMimeType("image/" + fileExtension);
      byte[] imgBinData = null;
      res.setSourceUrl(img.attr(attribute));
      try {
        imgBinData = fetchRemoteFile(res.getSourceUrl());
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      setBinResource(imgBinData, res, fileExtension);
      resMap.put(res.getFileHashcode(), res);
      resContent
          .append(
              "<div style=\"margin-block-start:;margin-block-end:;-moz-margin-start:;-moz-margin-end:;margin-top:0px;margin-bottom:0px;\">\n"
                  + "\t<en-media width=\"")
          .append(res.getWidth())
          .append("\" height=\"")
          .append(res.getHeight())
          .append("\" alt=\"image\" hash=\"")
          .append(res.getFileHashcode())
          .append("\" type=\"")
          .append(res.getMimeType())
          .append("\" style=\"max-width:400px;\"/>\n")
          .append("</div>\n");
    }
    return resContent.toString();
  }

  private String parseImage(Element e, String pattern, HashMap<String, NoteResource> resMap) {
    StringBuilder resContent = new StringBuilder();
    Elements imageElements = e.select(pattern);
    for (Element imageElement : imageElements) {
      parseTextPostImage(imageElement, "href", resMap, resContent);
    }
    return resContent.toString();
  }

  private void setBinResource(byte[] imgBinData, NoteResource res, String fileExtension) {
    if (imgBinData != null) {
      InputStream in = new ByteArrayInputStream(imgBinData);
      try {
        BufferedImage bImageFromConvert = ImageIO.read(in);
        res.setWidth(String.valueOf(bImageFromConvert.getWidth()));
        res.setHeight(String.valueOf(bImageFromConvert.getHeight()));
      } catch (IOException e1) {
        e1.printStackTrace();
      } finally {
        try {
          in.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
      res.setData(base64Encode(imgBinData));
      res.setFileHashcode(calculateResourceHash(imgBinData));
      res.setFileName(res.getFileHashcode() + "." + fileExtension);
    }
  }

  private String parseVideo(Element e, String pattern, HashMap<String, NoteResource> resMap) {
    StringBuilder resContent = new StringBuilder();
    Element videoElement = e.select(pattern).first();
    if (videoElement != null) {
      NoteResource res = new NoteResource();
      res.setMimeType(videoElement.attr("type"));
      byte[] videoBinData = null;
      res.setSourceUrl(videoElement.attr("src"));
      try {
        videoBinData = fetchRemoteFile(res.getSourceUrl());
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      if (videoBinData != null) {
        res.setFileHashcode(calculateResourceHash(videoBinData));
        res.setData(base64Encode(videoBinData));
        String[] extension = res.getMimeType().split("/");
        res.setFileName(res.getFileHashcode() + "." + extension[extension.length - 1]);
      }
      resMap.put(res.getFileHashcode(), res);
      resContent
          .append("<div><en-media type=\"")
          .append(res.getMimeType())
          .append("\" style=\"cursor:pointer;\" height=\"43\" hash=\"")
          .append(res.getFileHashcode())
          .append("\"/></div>\n");
    }

    return resContent.toString();
  }

  private Elements parseIFrame(String url, String pattern) {
    Document html = null;
    Elements resource = null;
    if (!url.isEmpty()) {
      try {
        url = url.replace("https", "http");
        html = Jsoup.connect(url).get();
      } catch (IOException e) {
        logger.catching(e);
        logger.error(
            "no protocol is specified, or an unknown protocol is found, or iFrame url is null.");
      }
    }
    if (html != null) resource = html.select(pattern);
    return resource;
  }

  private String extractFileExtension(String url) {
    String[] name = url.split("\\.(?=[^\\.]+$)");
    return name[name.length - 1];
  }

  private byte[] fetchRemoteFile(String location) throws Exception {
    URL url = new URL(location);
    InputStream is;
    byte[] bytes = null;
    try {
      is = url.openStream();
      bytes = IOUtils.toByteArray(is);
    } catch (IOException e) {
      // handle errors
      e.printStackTrace();
    }
    return bytes;
  }

  private String calculateResourceHash(byte[] content) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");

      md.update(content);
      byte[] byteData = md.digest();

      // convert the byte to hex format method 1
      StringBuilder sb = new StringBuilder();
      for (byte aByteData : byteData) {
        sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
      }

      System.out.println("Digest(in hex format):: " + sb.toString());

      // convert the byte to hex format method 2
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

  private String base64Encode(byte[] content) {
    Base64 coder = new Base64();
    StringBuilder sb = new StringBuilder("\n");
    String code = coder.encodeToString(content);

    int countPlusOne = 65;
    for (int i = 0; i < code.length(); i += countPlusOne) {
      sb.append(
          code.substring(i, (i + countPlusOne) > code.length() ? code.length() : i + countPlusOne));
      sb.append("\n");
    }
    return sb.toString();

    // return code;

  }
}
