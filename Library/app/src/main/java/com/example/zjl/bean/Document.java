package com.example.zjl.bean;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjl on 2016/5/29.
 */
public class Document implements Serializable {

 /*   [{"id":"573c802bbf04ec30fcf356b1","DocTitle":"ceshi.docx","UploadTime":"2016","Editorname":null,"filesize":"0.03","ViewCount":69,"DownloadCount":9,"Library":"DL"},
    {"id":"575faec4234815f003000032","DocTitle":"123","UploadTime":"16\/06\/14","Editorname":"110","filesize":"0.07","ViewCount":2,"DownloadCount":3,"Library":"DL"},
    {"id":"575fd627234815b81b000033","DocTitle":"1111","UploadTime":"16\/06\/14","Editorname":"123","filesize":"0.02","ViewCount":1,"DownloadCount":1,"Library":"PL"}]
            06-15 12:33:41.828 25150-25150/com.example.zjl.myapplication E/---------------: 更新UI
    */
    private String id;
    private String DocTitle;
    private String UploadTime;
    private String ViewCount;//xx
    private String DownloadCount;//xx
    private String Library;//文档属于哪个文库
    private String Editorname;//作者
    private String filesize;

    public String getSize() {
        return filesize;
    }

    public void setSize(String size) {
        this.filesize = size;
    }

    public Document() {
        id = "";
        DocTitle = "";
        UploadTime = "";
        ViewCount = "";
        DownloadCount = "";
        Library = "";
        Editorname = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocTitle() {
        return DocTitle;
    }

    public void setDocTitle(String docTitle) {
        this.DocTitle = docTitle;
    }

    public String getUploadTime() {
        return UploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.UploadTime = uploadTime;
    }

    public String getViewCount() {
        return ViewCount;
    }

    public void setViewCount(String viewCount) {
        this.ViewCount = viewCount;
    }

    public String getDownloadCount() {
        return DownloadCount;
    }

    public void setDownloadCount(String downloadCount) {
        this.DownloadCount = downloadCount;
    }

    public String getLibrary() {
        return Library;
    }

    public void setLibrary(String library) {
        this.Library = library;
    }


    public String getEditor() {
        return Editorname;
    }

    public void setEditor(String editor) {
        this.Editorname = editor;
    }

    @Override
    public String toString() {
        return id+getDocTitle()+getUploadTime()+getViewCount()+getDownloadCount()+getLibrary()+getEditor()+getSize();
    }
}
