package com.serverless.model;

import java.util.List;

public class GetTracksResponse {

    private final List<GetTrackResponse> tracks;

    public GetTracksResponse(List<GetTrackResponse> tracks) {
        this.tracks = tracks;
    }

    public List<GetTrackResponse> getTracks() {
        return tracks;
    }
}
