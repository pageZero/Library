package com.example.zjl.bean;

import java.util.List;

/**
 * Created by zjl on 2016/6/12.
 */
public class DocumentList {
    /*对应的JSON串
    {"documents":[
        {"id":"0x0045", "title":"about.pdf","editor":"zzz","uploadTime":"2016-5-24 15:32:40","docAbstract":"test doc",
        "library":"personal","sourceFileUrl":"http://xxx.pdf","tags":["design","PPT","team document"]},
        {"id":"0x0045", "title":"about.pdf","editor":"zzz","uploadTime":"2016-5-24 15:32:40","docAbstract":"test doc",
        "library":"personal","sourceFileUrl":"http://xxx.pdf","tags":["design","PPT","team document"]},
        {"id":"0x0045", "title":"about.pdf","editor":"zzz","uploadTime":"2016-5-24 15:32:40","docAbstract":"test doc",
        "library":"personal","sourceFileUrl":"http://xxx.pdf","tags":["design","PPT","team document"]}
    ]}
     */
    private List<Document> documents;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
