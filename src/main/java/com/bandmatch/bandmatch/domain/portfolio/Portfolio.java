package com.bandmatch.bandmatch.domain.portfolio;

import jakarta.persistence.*;

/**
 * Entitas Portfolio yang merepresentasikan portofolio profesional dari seorang
 * BandMember (musisi). Portfolio berisi informasi seperti biografi, daftar
 * keahlian (skill set), dan tautan ke media sosial (YouTube & Instagram).
 * <p>
 * Portfolio memiliki relasi One-to-One dengan BandMember (Komposisi).
 * Jika BandMember dihapus, Portfolio ikut terhapus (cascade = ALL, orphanRemoval = true).
 * </p>
 */
@Entity
public class Portfolio {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Deskripsi diri, pengalaman, atau profil singkat musisi */
    private String bio;

    /** Keahlian musik (misal: "Gitar, Vokal, Keyboard, Produksi") */
    private String skillSet;

    /** Tautan atau handle channel YouTube (bisa berupa URL lengkap) */
    private String youtubeChannel;

    /** Username Instagram (disimpan tanpa karakter '@' untuk fleksibilitas) */
    private String instagramHandle;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /** Constructor default wajib untuk JPA (Hibernate) */
    public Portfolio() {}

    /**
     * Constructor untuk inisialisasi cepat portofolio dengan data dasar.
     *
     * @param bio      deskripsi diri musisi
     * @param skillSet daftar keahlian (dipisahkan koma misalnya)
     */
    public Portfolio(String bio, String skillSet) {
        this.bio = bio;
        this.skillSet = skillSet;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(String skillSet) {
        this.skillSet = skillSet;
    }

    public String getYoutubeChannel() {
        return youtubeChannel;
    }

    public void setYoutubeChannel(String youtubeChannel) {
        this.youtubeChannel = youtubeChannel;
    }

    public String getInstagramHandle() {
        return instagramHandle;
    }

    public void setInstagramHandle(String instagramHandle) {
        this.instagramHandle = instagramHandle;
    }
}