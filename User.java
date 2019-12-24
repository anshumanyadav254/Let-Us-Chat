package com.luciferhacker.letuschat;

public class User {

    public String name;
    public String image;
    public String status;
    public String thumbImage;

    public User() {

    }

    public User(String name, String image, String status, String thumbImage) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumbImage = thumbImage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public String getThumbImage() {
        return thumbImage;
    }
}
