package com.serverless.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class CreateTrackRequest {

    private String title;

    private String artist;

    public CreateTrackRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public static CreateTrackRequest fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, CreateTrackRequest.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static CreateTrackRequest fromMap(Map<Object, Object> map) {
        if (map == null) {
            return null;
        }

        String title = (String) map.get("title");
        String artist = (String) map.get("artist");
        CreateTrackRequest createTrackRequest = new CreateTrackRequest();
        createTrackRequest.setTitle(title);
        createTrackRequest.setArtist(artist);
        return createTrackRequest;
    }
}
