package SimpleBlog.entity;

import java.util.HashMap;

/**
 * SimpleBlog post entity
 * Created by lyoo on 9/25/2015.
 */
public class Blog {
    private String subject;
    private String content;
    private String create;
    private String update;
    private String author;
    private String tags;
    private String source;
    private String sourceUrl;
    private HashMap<String, NoteResource> resources;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreate() {
        return create;
    }

    public void setCreate(String create) {
        this.create = create;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public HashMap<String, NoteResource> getResources() {
        return resources;
    }

    public void setResources(HashMap<String, NoteResource> resources) {
        this.resources = resources;
    }
}
