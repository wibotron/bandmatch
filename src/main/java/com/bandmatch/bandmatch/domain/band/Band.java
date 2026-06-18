package com.bandmatch.bandmatch.domain.band;

import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.domain.user.BandMember;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entitas Band yang merepresentasikan sebuah grup musik.
 * Band memiliki informasi dasar, genre (multigenre), profil media sosial,
 * serta relasi ke manager, anggota (member), diskografi, dan rekrutmen.
 */
@Entity
public class Band {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    /**
     * Multigenre: mendukung lebih dari satu genre per band.
     * Disimpan sebagai tabel terpisah (band_genres) karena relasi @ElementCollection.
     */
    @ElementCollection
    @CollectionTable(name = "band_genres", joinColumns = @JoinColumn(name = "band_id"))
    @Column(name = "genre")
    private List<String> genres = new ArrayList<>();

    /** Profil YouTube band (URL lengkap) */
    private String youtubeProfile;

    /** Profil Spotify band (URL lengkap) */
    private String spotifyProfile;

    // =========================================================================
    // RELASI DENGAN ENTITAS LAIN
    // =========================================================================

    /**
     * Relasi Many-to-Many dengan BandMember (Agregasi).
     * Band dapat memiliki banyak member, dan member dapat bergabung ke banyak band.
     * Jika band dihapus, member tetap ada (lifecycle independen).
     */
    @ManyToMany
    @JoinTable(
            name = "band_members",
            joinColumns = @JoinColumn(name = "band_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<BandMember> members = new ArrayList<>();

    /**
     * Relasi Many-to-One dengan Manager (Directed Association).
     * Setiap band dikelola oleh satu manager.
     */
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    /**
     * Relasi One-to-Many dengan Discography (Komposisi).
     * Band memiliki banyak album/diskografi. Jika band dihapus, semua diskografi ikut terhapus.
     */
    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Discography> discographies = new ArrayList<>();

    /**
     * Relasi One-to-Many dengan Recruitment (Komposisi).
     * Band memiliki banyak rekrutmen. Jika band dihapus, semua rekrutmen ikut terhapus.
     */
    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recruitment> recruitments = new ArrayList<>();

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    public Band() {}

    /**
     * Constructor untuk membuat band baru dengan manager sebagai pemilik.
     *
     * @param name    nama band
     * @param manager manager yang mengelola band ini
     */
    public Band(String name, Manager manager) {
        this.name = name;
        this.manager = manager;
    }

    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================

    /**
     * Menambahkan diskografi/album ke dalam band.
     *
     * @param discography objek Discography yang akan ditambahkan
     */
    public void addDiscography(Discography discography) {
        discography.setBand(this);
        this.discographies.add(discography);
    }

    /**
     * Membuka rekrutmen baru untuk posisi tertentu.
     *
     * @param position   posisi yang dibutuhkan (misal: "Lead Gitar")
     * @param instrument instrumen spesifik yang dibutuhkan (misal: "Gitar Elektrik")
     * @param deadline   batas akhir pendaftaran
     * @return objek Recruitment yang baru dibuat
     */
    public Recruitment openRecruitment(String position, String instrument, LocalDateTime deadline) {
        Recruitment recruitment = new Recruitment(position, instrument, deadline);
        recruitment.setBand(this);
        this.recruitments.add(recruitment);
        return recruitment;
    }

    /**
     * Menutup rekrutmen yang sedang berlangsung.
     *
     * @param recruitment rekrutmen yang akan ditutup
     */
    public void closeRecruitment(Recruitment recruitment) {
        if (this.recruitments.contains(recruitment)) {
            recruitment.setStatus("CLOSED");
        }
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getYoutubeProfile() {
        return youtubeProfile;
    }

    public void setYoutubeProfile(String youtubeProfile) {
        this.youtubeProfile = youtubeProfile;
    }

    public String getSpotifyProfile() {
        return spotifyProfile;
    }

    public void setSpotifyProfile(String spotifyProfile) {
        this.spotifyProfile = spotifyProfile;
    }

    public List<BandMember> getMembers() {
        return members;
    }

    public void setMembers(List<BandMember> members) {
        this.members = members;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public List<Discography> getDiscographies() {
        return discographies;
    }

    public void setDiscographies(List<Discography> discographies) {
        this.discographies = discographies;
    }

    public List<Recruitment> getRecruitments() {
        return recruitments;
    }

    public void setRecruitments(List<Recruitment> recruitments) {
        this.recruitments = recruitments;
    }
}