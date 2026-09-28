package com.revconnect.post.dto;

import jakarta.validation.constraints.Size;

public class PostRequest {
    @Size(max = 2200)
    private String caption;

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
