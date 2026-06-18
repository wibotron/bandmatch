package com.bandmatch.bandmatch.domain.band;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entitas Recruitment yang merepresentasikan rekrutmen / lowongan posisi di sebuah band.
 * Rekrutmen dapat dibuka dan ditutup oleh manager, dan dapat dilamar oleh member.
 * Status rekrutmen dikelola secara internal melalui method bisnis.
 */
@Entity
public class Recruitment {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nama posisi yang dibutuhkan (misal: "Lead Gitar", "Vokalis", dll) */
    private String position;

    /** Instrumen spesifik yang dibutuhkan (misal: "Gitar Elektrik", "Bass", dll) */
    private String requiredInstrument;

    /**
     * Status rekrutmen: "OPEN" atau "CLOSED".
     * Diatur secara internal melalui business method.
     */
    private String status;

    /** Batas akhir pendaftaran (deadline) */
    private LocalDateTime deadline;

    // =========================================================================
    // RELASI DENGAN ENTITAS LAIN
    // =========================================================================

    /**
     * Relasi Many-to-One dengan Band (Komposisi).
     * Setiap rekrutmen terikat pada satu band. Jika band dihapus, rekrutmen ikut terhapus.
     */
    @ManyToOne
    @JoinColumn(name = "band_id")
    private Band band;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /** Constructor default wajib untuk JPA */
    public Recruitment() {}

    /**
     * Constructor untuk membuat rekrutmen baru.
     * Status akan otomatis di-set menjadi "OPEN" saat dibuat.
     *
     * @param position            posisi yang dibutuhkan
     * @param requiredInstrument  instrumen spesifik yang dibutuhkan
     * @param deadline            batas akhir pendaftaran
     */
    public Recruitment(String position, String requiredInstrument, LocalDateTime deadline) {
        this.position = position;
        this.requiredInstrument = requiredInstrument;
        this.deadline = deadline;
        this.status = "OPEN"; // Status default saat rekrutmen baru dibuat
    }

    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================

    /**
     * Mengecek apakah rekrutmen masih terbuka untuk pendaftaran.
     * Syarat: status "OPEN" DAN deadline belum lewat dari waktu sekarang.
     *
     * @return true jika rekrutmen masih terbuka, false jika sudah ditutup atau melewati deadline
     */
    public boolean isStillOpen() {
        return "OPEN".equals(this.status) && LocalDateTime.now().isBefore(deadline);
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRequiredInstrument() {
        return requiredInstrument;
    }

    public void setRequiredInstrument(String requiredInstrument) {
        this.requiredInstrument = requiredInstrument;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Band getBand() {
        return band;
    }

    public void setBand(Band band) {
        this.band = band;
    }
}