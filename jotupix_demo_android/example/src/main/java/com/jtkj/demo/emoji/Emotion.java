package com.jtkj.demo.emoji;

public class Emotion {

    public String code;
    public String name;
    public int id;
    public int imageId;

    public Emotion(String code, String name, int id, int imageId) {
        this.code = code;
        this.name = name;
        this.id = id;
        this.imageId = imageId;
    }

    public Emotion() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return "Emotion{" + "code='" + code + '\'' + ", name='" + name + '\'' + ", id=" + id + ", styleImageId=" + imageId + '}';
    }
}
