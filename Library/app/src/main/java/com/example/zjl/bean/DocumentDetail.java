package com.example.zjl.bean;

/**
 * Created by zjl on 2016/6/15.
 */
public class DocumentDetail {


    private User.ID id;
    private String DocTitle;
    private String UploadTime;
    private String ViewCount;//xx
    private String DownloadCount;//xx
    private String Library;//文档属于哪个文库
    private String Editorname;//作者
    private String filesize;
    private String DocAbstract;
    private String DocDepartment;
    private String[] Tags;


    public void setTags(String[] tags) {
        Tags = tags;
    }

    public String[] getTags() {
        return Tags;
    }

    public void setDocDepartment(String docDepartment) {
        DocDepartment = docDepartment;
    }

    public String getDocDepartment() {
        return DocDepartment;
    }

    public void setDocAbstract(String docAbstract) {
        DocAbstract = docAbstract;
    }

    public String getDocAbstract() {
        return DocAbstract;
    }

    public String getSize() {
        return filesize;
    }

    public void setSize(String size) {
        this.filesize = size;
    }

    public DocumentDetail() {
        id = null;
        DocTitle = "";
        UploadTime = "";
        ViewCount = "";
        DownloadCount = "";
        Library = "";
        Editorname = "";
    }

    public User.ID getId() {
        return id;
    }

    public void setId(User.ID id) {
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
