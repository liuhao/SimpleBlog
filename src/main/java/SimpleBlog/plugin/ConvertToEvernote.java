package SimpleBlog.plugin;

import SimpleBlog.entity.Blog;
import SimpleBlog.entity.NoteResource;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.*;
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
        String preContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\"><en-note style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space;\">";
        String postContent = "</en-note>";

        try {
            SAXReader saxReader = new SAXReader();
            boolean removeOnce = false;
            Document document = null;

            Document template = saxReader.read(templatePath.getInputStream()); // 读取XML文件,获得document对象
            Element noteTemplate = template.getRootElement().element("note").createCopy();

            if (type == CreateType.UPDATE) {
                try {
                    fis = new FileInputStream(enexPath);
                    document = saxReader.read(fis);
                } catch (FileNotFoundException | DocumentException e) {
                    //logger.catching(e);
                    logger.error("Target file no exist, SAXRead error");
                    document = template;
                    removeOnce = true;
                }
            } else {
                document = template;
                removeOnce = true;
            }

            if (document != null) {
                Element exportElm = document.getRootElement();
                if (exportElm != null) {
                    DateUtil dateUtil = new DateUtil();
                    exportElm.addAttribute("export-date", dateUtil.getEvernoteDate());

                    if (removeOnce) {
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
                            contentElm.setText("");
                            contentElm.addCDATA(preContent + blog.getContent() + postContent);
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

                        // add resource section
                        if (blog.getResources() != null && !blog.getResources().isEmpty()) {
                            for (NoteResource res : blog.getResources().values()) {
                                note.add(createResourceElm(res));
                            }
                        }
                        exportElm.add(note);
                    }
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

    public Element createResourceElm(NoteResource res) {
        Element root = new DocumentFactory().createElement("resource");

        Element data = new DocumentFactory().createElement("data");
        data.addAttribute("encoding", "base64");
        data.setText(res.getData());
        root.add(data);

        Element mime = new DocumentFactory().createElement("mime");
        mime.setText("image/" + res.getMimeType());
        root.add(mime);

        Element width = new DocumentFactory().createElement("width");
        width.setText(res.getWidth());
        root.add(width);

        Element height = new DocumentFactory().createElement("height");
        height.setText(res.getHeight());
        root.add(height);

        Element source_url = new DocumentFactory().createElement("source-url");
        source_url.setText(res.getSourceUrl());

        Element resource_attributes = new DocumentFactory().createElement("resource-attributes");
        resource_attributes.add(source_url);
        root.add(resource_attributes);

        return root;
    }

    public void createEnex(List<Blog> blogs, String enexPath) {
        XMLWriter writer;
        try {
            writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(enexPath), "UTF-8"));
            writer.setEscapeText(true);
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
            writer.setEscapeText(true);
            writer.write(document); // update file到文件
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
