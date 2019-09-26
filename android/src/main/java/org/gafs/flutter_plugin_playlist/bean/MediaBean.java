package org.gafs.flutter_plugin_playlist.bean;

import java.io.Serializable;

public class MediaBean implements Serializable {

    String id = "";
    private String name = ""; //音乐名称
    private String author = ""; //音乐作者
    private String album = ""; //音乐专辑
    private long duration = 0; //音乐时长
    private long size = 0; //音乐大小
    private String source = ""; //音乐地址
    private String imageUrl = ""; //图片地址

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MediaBean() {
    }

    public MediaBean(String id, String name, String author, String album, long duration, long size, String source) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.album = album;
        this.duration = duration;
        this.size = size;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }


}
