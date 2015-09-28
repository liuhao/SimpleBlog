package SimpleBlog.plugin;

import SimpleBlog.entity.Blog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;

/**
 * Created by lyoo on 9/25/2015.
 */
public class ConvertToEvernote {
    private static Logger logger = LogManager.getLogger(ConvertToEvernote.class.getName());
    private Resource templatePath;
    private String enexPath;

    public void setEnexPath(String enexPath) {
        this.enexPath = enexPath;
    }

    public void setTemplatePath(Resource templatePath) {
        this.templatePath = templatePath;
    }

    public Document exportEvernoteXml(Blog blog) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(templatePath.getInputStream()); // 读取XML文件,获得document对象
            if (document == null) {
                logger.error("fail to get XML Document");
            } else {
                Element exportElm = document.getRootElement();
                if (exportElm == null) {
                    logger.error("the en-export element is not exist!");
                } else {
                    exportElm.addAttribute("export-date", blog.getCreate());
                }

                Element titleElm = document.getRootElement().element("note").element("title");
                if (exportElm == null) {
                    logger.error("the title element is not exist!");
                } else {
                    titleElm.setText(blog.getSubject());
                }

                Element createdElm = document.getRootElement().element("note").element("created");
                if (exportElm == null) {
                    logger.error("the created element is not exist!");
                } else {
                    createdElm.setText(blog.getCreate());
                }

                Element updatedElm = document.getRootElement().element("note").element("updated");
                if (exportElm == null) {
                    logger.error("the updated element is not exist!");
                } else {
                    updatedElm.setText(blog.getUpdate());
                }
                return document;
            }
        } catch (Exception e) {
            logger.catching(e);
            logger.error("SAXRead error");
        }
        return null;
    }

    public void createEnex(Blog blog) {
        XMLWriter writer;
        try {
            writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(
                    enexPath + "/" + blog.getCreate() + ".enex"), "UTF-8"));
            writer.write(exportEvernoteXml(blog)); //输出到文件
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
