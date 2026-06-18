package com.bandmatch.bandmatch.domain.interaction;

import com.bandmatch.bandmatch.domain.band.Recruitment;
import com.bandmatch.bandmatch.domain.user.BandMember;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entitas Application yang merepresentasikan lamaran yang diajukan oleh BandMember
 * ke sebuah Recruitment (rekrutmen) yang dibuka oleh Manager.
 * <p>
 * Application mengimplementasikan interface {@link MusicInteraction}, sehingga
 * memiliki perilaku standar: send() untuk mengirim lamaran, dan respond() untuk
 * merespon lamaran (accept/reject).
 * </p>
 * <p>
 * Status lamaran: PENDING (menunggu), ACCEPTED (diterima), REJECTED (ditolak),
 * atau EXPIRED (rekrutmen sudah ditutup).
 * </p>
 */
@Entity
public class Application implements MusicInteraction {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Surat motivasi dari pelamar */
    private String motivationLetter;

    /** Catatan ketersediaan pelamar (misal: kapan bisa mulai bergabung) */
    private String availabilityNote;

    /**
     * Status lamaran (PENDING, ACCEPTED, REJECTED, EXPIRED).
     * Menggunakan EnumType.STRING agar nilai enum disimpan sebagai teks di database.
     */
    @Enumerated(EnumType.STRING)
    private InteractionStatus status = InteractionStatus.PENDING;

    // =========================================================================
    // RELASI DENGAN ENTITAS LAIN
    // =========================================================================

    /**
     * Relasi Many-to-One ke BandMember (pelamar).
     * Seorang member dapat mengajukan banyak lamaran.
     */
    @ManyToOne
    private BandMember applicant;

    /**
     * Relasi Many-to-One ke Recruitment (rekrutmen tujuan).
     * Sebuah rekrutmen dapat menerima banyak lamaran.
     */
    @ManyToOne
    private Recruitment targetRecruitment;

    /** Waktu pembuatan lamaran (otomatis diisi saat instance dibuat) */
    private LocalDateTime createdAt = LocalDateTime.now();

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /** Constructor default wajib untuk JPA */
    public Application() {}

    /**
     * Constructor untuk membuat lamaran baru.
     *
     * @param motivationLetter  surat motivasi pelamar
     * @param availabilityNote  catatan ketersediaan pelamar
     * @param applicant         member yang mengajukan lamaran
     * @param targetRecruitment rekrutmen yang dilamar
     */
    public Application(String motivationLetter, String availabilityNote,
                       BandMember applicant, Recruitment targetRecruitment) {
        this.motivationLetter = motivationLetter;
        this.availabilityNote = availabilityNote;
        this.applicant = applicant;
        this.targetRecruitment = targetRecruitment;
    }

    // =========================================================================
    // IMPLEMENTASI INTERFACE MusicInteraction
    // =========================================================================

    /**
     * Mengirim lamaran (menyimpan status PENDING).
     * Sebelum mengirim, validasi dilakukan: rekrutmen harus masih terbuka.
     * Jika rekrutmen sudah ditutup atau melewati deadline, status menjadi EXPIRED
     * dan exception dilempar.
     *
     * @throws IllegalStateException jika rekrutmen sudah ditutup
     */
    @Override
    public void send() {
        // Validasi: apakah rekrutmen masih terbuka?
        if (!targetRecruitment.isStillOpen()) {
            this.status = InteractionStatus.EXPIRED;
            throw new IllegalStateException("Rekrutmen sudah tutup!");
        }
        this.status = InteractionStatus.PENDING;
    }

    /**
     * Merespon lamaran (accept/reject) oleh Manager.
     * Mengubah status menjadi ACCEPTED atau REJECTED sesuai parameter.
     *
     * @param isAccepted true jika lamaran diterima, false jika ditolak
     */
    @Override
    public void respond(boolean isAccepted) {
        this.status = isAccepted ? InteractionStatus.ACCEPTED : InteractionStatus.REJECTED;
    }

    /**
     * Mengembalikan status lamaran saat ini.
     *
     * @return status lamaran (PENDING, ACCEPTED, REJECTED, EXPIRED)
     */
    @Override
    public InteractionStatus getStatus() {
        return status;
    }

    /**
     * Mengembalikan waktu pembuatan lamaran.
     *
     * @return waktu lamaran dibuat
     */
    @Override
    public LocalDateTime getTimestamp() {
        return createdAt;
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

    public String getMotivationLetter() {
        return motivationLetter;
    }

    public void setMotivationLetter(String motivationLetter) {
        this.motivationLetter = motivationLetter;
    }

    public String getAvailabilityNote() {
        return availabilityNote;
    }

    public void setAvailabilityNote(String availabilityNote) {
        this.availabilityNote = availabilityNote;
    }

    public BandMember getApplicant() {
        return applicant;
    }

    public void setApplicant(BandMember applicant) {
        this.applicant = applicant;
    }

    public Recruitment getTargetRecruitment() {
        return targetRecruitment;
    }

    public void setTargetRecruitment(Recruitment targetRecruitment) {
        this.targetRecruitment = targetRecruitment;
    }
}