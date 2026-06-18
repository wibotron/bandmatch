package com.bandmatch.bandmatch.domain.portfolio;

import jakarta.persistence.*;

@Entity
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;
    private String skillSet;         // misal: "Gitar, Vokal, Keyboard"
    private String youtubeChannel;
    private String instagramHandle;

    // Constructor default wajib untuk JPA
    public Portfolio() {}

    // Constructor untuk inisialisasi cepat
    public Portfolio(String bio, String skillSet) {
        this.bio = bio;
        this.skillSet = skillSet;
    }

    // --- GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSkillSet() { return skillSet; }
    public void setSkillSet(String skillSet) { this.skillSet = skillSet; }
    public String getYoutubeChannel() { return youtubeChannel; }
    public void setYoutubeChannel(String youtubeChannel) { this.youtubeChannel = youtubeChannel; }
    public String getInstagramHandle() { return instagramHandle; }
    public void setInstagramHandle(String instagramHandle) { this.instagramHandle = instagramHandle; }
}
