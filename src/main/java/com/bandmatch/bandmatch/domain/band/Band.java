package com.bandmatch.bandmatch.domain.band;

import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.domain.user.BandMember;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Band {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String location;
    private String spotifyEmbedUrl;

    // --- RELASI: MANY-TO-MANY dengan BandMember (AGREGASI) ---
    // Lifecycle independen: Band bubar, Member tetap ada
    @ManyToMany
    @JoinTable(
            name = "band_members",
            joinColumns = @JoinColumn(name = "band_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<BandMember> members = new ArrayList<>();

    // --- RELASI: MANAGER (Directed Association) ---
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    // --- RELASI: KOMPOSISI (Band mati, Discography & Recruitment ikut mati) ---
    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Discography> discographies = new ArrayList<>();

    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recruitment> recruitments = new ArrayList<>();

    // --- CONSTRUCTORS ---
    public Band() {}

    public Band(String name, Manager manager) {
        this.name = name;
        this.manager = manager;
    }

    // --- BUSINESS METHODS ---
    public void addDiscography(Discography discography) {
        discography.setBand(this);
        this.discographies.add(discography);
    }

    public Recruitment openRecruitment(String position, String instrument, java.time.LocalDateTime deadline) {
        Recruitment recruitment = new Recruitment(position, instrument, deadline);
        recruitment.setBand(this);
        this.recruitments.add(recruitment);
        return recruitment;
    }

    public void closeRecruitment(Recruitment recruitment) {
        if (this.recruitments.contains(recruitment)) {
            recruitment.setStatus("CLOSED");
        }
    }

    // --- GETTERS & SETTERS (Generate sendiri ya, wajib ada untuk JPA) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSpotifyEmbedUrl() { return spotifyEmbedUrl; }
    public void setSpotifyEmbedUrl(String spotifyEmbedUrl) { this.spotifyEmbedUrl = spotifyEmbedUrl; }
    public List<BandMember> getMembers() { return members; }
    public void setMembers(List<BandMember> members) { this.members = members; }
    public Manager getManager() { return manager; }
    public void setManager(Manager manager) { this.manager = manager; }
    public List<Discography> getDiscographies() { return discographies; }
    public void setDiscographies(List<Discography> discographies) { this.discographies = discographies; }
    public List<Recruitment> getRecruitments() { return recruitments; }
    public void setRecruitments(List<Recruitment> recruitments) { this.recruitments = recruitments; }
}