package com.lstephan.spacedev.musiclist;

public class Song {

    private String path;
    private String name;

    public Song(String path, String name){
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;

    }

    public String getName() {
        return name;
    }
}
