package com.bandmatch.bandmatch.domain.band;

import jakarta.persistence.*;

@Entity
public class Discography {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String albumTitle;
    private Integer releaseYear;
    private String spotifyAlbumId; // ID album dari Spotify

    @ManyToOne
    @JoinColumn(name = "band_id")
    private Band band;

    public Discography() {}

    public Discography(String albumTitle, Integer releaseYear) {
        this.albumTitle = albumTitle;
        this.releaseYear = releaseYear;
    }

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAlbumTitle() { return albumTitle; }
    public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public String getSpotifyAlbumId() { return spotifyAlbumId; }
    public void setSpotifyAlbumId(String spotifyAlbumId) { this.spotifyAlbumId = spotifyAlbumId; }
    public Band getBand() { return band; }
    public void setBand(Band band) { this.band = band; }
}