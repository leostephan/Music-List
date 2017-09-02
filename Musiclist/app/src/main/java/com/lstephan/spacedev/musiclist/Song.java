package com.lstephan.spacedev.musiclist;

public class Song {

    private String path;
    private String name;
    private int duration;
    private String artist;

    public Song(String path, String name, int duration, String artist){
        this.path = path;
        this.name = name;
        this.duration = duration;
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }
}
