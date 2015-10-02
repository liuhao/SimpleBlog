package SimpleBlog.plugin;

import SimpleBlog.entity.Blog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.List;

/**
 * Created by lyoo on 9/25/2015.
 */
public class ConvertToEvernote {
    private static Logger logger = LogManager.getLogger(ConvertToEvernote.class.getName());
    private Resource templatePath;

    private enum CreateType {
        UPDATE,
        CREATE
    }

    public void setTemplatePath(Resource templatePath) {
        this.templatePath = templatePath;
    }

    public Document exportEvernoteXml(List<Blog> blogs, CreateType type, String enexPath) {
        FileInputStream fis = null;
        String preContent = "<![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\"><en-note style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space;\">";
        String postContent = "</en-note>]]>";

        try {
            SAXReader saxReader = new SAXReader();
            Document document;

            if (type == CreateType.CREATE) {
                document = saxReader.read(templatePath.getInputStream()); // 读取XML文件,获得document对象
            } else {
                fis = new FileInputStream(enexPath);
                document = saxReader.read(fis);
            }

            if (document == null) {
                logger.error("fail to get XML Document");
            } else {
                Element exportElm = document.getRootElement();

                if (exportElm == null) {
                    logger.error("the en-export element is not exist!");
                } else {
                    DateUtil dateUtil = new DateUtil();
                    exportElm.addAttribute("export-date", dateUtil.getEvernoteDate());
                }
                Element noteTemplate = exportElm.element("note").createCopy();
                if (type == CreateType.CREATE) {
                    exportElm.remove(exportElm.element("note"));
                }

                for (Blog blog : blogs) {
                    Element note = noteTemplate.createCopy();
                    Element titleElm = note.element("title");
                    if (titleElm == null) {
                        logger.error("the title element is not exist!");
                    } else {
                        titleElm.setText(blog.getSubject());
                    }

                    Element contentElm = note.element("content");
                    if (contentElm == null) {
                        logger.error("the created element is not exist!");
                    } else {
                        contentElm.setText(preContent + blog.getContent() + postContent);
                    }

                    Element createdElm = note.element("created");
                    if (createdElm == null) {
                        logger.error("the created element is not exist!");
                    } else {
                        createdElm.setText(blog.getCreate());
                    }

                    Element updatedElm = note.element("updated");
                    if (updatedElm == null) {
                        logger.error("the updated element is not exist!");
                    } else {
                        updatedElm.setText(blog.getUpdate());
                    }
                    exportElm.add(note);
                }
                return document;
            }
        } catch (Exception e) {
            logger.catching(e);
            logger.error("SAXRead error");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error("将文件[" + enexPath + "]转换成Document,输入流关闭异常", e);
                }
            }
        }
        return null;
    }

    public void createEnex(List<Blog> blogs, String enexPath) {
        XMLWriter writer;
        try {
            writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(enexPath), "UTF-8"));
            writer.setEscapeText(false);
            writer.write(exportEvernoteXml(blogs, CreateType.CREATE, enexPath)); //输出到文件
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateEnex(List<Blog> blogs, String enexPath) {
        XMLWriter writer;
        try {
            Document document = exportEvernoteXml(blogs, CreateType.UPDATE, enexPath);
            writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(enexPath), "UTF-8"));
            writer.setEscapeText(false);
            writer.write(document); // update file到文件
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
