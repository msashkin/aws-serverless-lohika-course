package com.serverless.model;

public class GetTrackResponse {

    private final int id;

    private final String title;

    private final String artist;

    public GetTrackResponse(int id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
