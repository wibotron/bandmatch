package com.bandmatch.bandmatch.domain.interaction;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.domain.user.BandMember;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entitas Offer yang merepresentasikan tawaran (penawaran) yang dikirim oleh Manager
 * kepada seorang BandMember untuk bergabung ke dalam sebuah Band.
 * <p>
 * Offer mengimplementasikan interface {@link MusicInteraction}, sehingga
 * memiliki perilaku standar: send() untuk mengirim tawaran, dan respond() untuk
 * merespon tawaran (accept/reject).
 * </p>
 * <p>
 * Status tawaran: PENDING (menunggu), ACCEPTED (diterima), REJECTED (ditolak),
 * atau EXPIRED (kadaluarsa karena melewati batas waktu).
 * </p>
 */
@Entity
public class Offer implements MusicInteraction {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Pesan atau deskripsi tawaran dari manager */
    private String message;

    /** Nominal gaji atau kompensasi finansial yang ditawarkan */
    private BigDecimal salaryOffered;

    /** Batas waktu berlakunya tawaran (jika lewat, status menjadi EXPIRED) */
    private LocalDateTime expiredDate;

    /**
     * Status tawaran (PENDING, ACCEPTED, REJECTED, EXPIRED).
     * Menggunakan EnumType.STRING agar nilai enum disimpan sebagai teks di database.
     */
    @Enumerated(EnumType.STRING)
    private InteractionStatus status = InteractionStatus.PENDING;

    // =========================================================================
    // RELASI DENGAN ENTITAS LAIN
    // =========================================================================

    /**
     * Relasi Many-to-One ke Manager (pengirim tawaran).
     * Seorang manager dapat mengirim banyak tawaran.
     */
    @ManyToOne
    private Manager sender;

    /**
     * Relasi Many-to-One ke BandMember (penerima tawaran).
     * Seorang member dapat menerima banyak tawaran.
     */
    @ManyToOne
    private BandMember receiver;

    /**
     * Relasi Many-to-One ke Band (band tujuan).
     * Tawaran selalu ditujukan untuk bergabung ke band tertentu.
     * Jika band dihapus, tawaran tetap ada (tidak menggunakan cascade delete).
     */
    @ManyToOne
    @JoinColumn(name = "band_id")
    private Band band;

    /** Waktu pembuatan tawaran (otomatis diisi saat instance dibuat) */
    private LocalDateTime createdAt = LocalDateTime.now();

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /** Constructor default wajib untuk JPA */
    public Offer() {}

    /**
     * Constructor untuk membuat tawaran baru.
     *
     * @param message      pesan tawaran
     * @param salaryOffered nominal gaji yang ditawarkan
     * @param expiredDate  batas waktu tawaran
     * @param sender       manager pengirim
     * @param receiver     member penerima
     * @param band         band tujuan (tempat member akan bergabung jika menerima)
     */
    public Offer(String message, BigDecimal salaryOffered, LocalDateTime expiredDate,
                 Manager sender, BandMember receiver, Band band) {
        this.message = message;
        this.salaryOffered = salaryOffered;
        this.expiredDate = expiredDate;
        this.sender = sender;
        this.receiver = receiver;
        this.band = band;
    }

    // =========================================================================
    // IMPLEMENTASI INTERFACE MusicInteraction
    // =========================================================================

    /**
     * Mengirim tawaran (menyimpan status PENDING).
     * Sebelum mengirim, validasi dilakukan: apakah tawaran sudah melewati batas waktu.
     * Jika sudah melewati expiredDate, status menjadi EXPIRED.
     */
    @Override
    public void send() {
        if (LocalDateTime.now().isAfter(expiredDate)) {
            this.status = InteractionStatus.EXPIRED;
        } else {
            this.status = InteractionStatus.PENDING;
        }
    }

    /**
     * Merespon tawaran (accept/reject) oleh BandMember penerima.
     * Jika status sudah EXPIRED, maka tidak dapat diubah dan exception dilempar.
     * Mengubah status menjadi ACCEPTED atau REJECTED sesuai parameter.
     *
     * @param isAccepted true jika tawaran diterima, false jika ditolak
     * @throws IllegalStateException jika tawaran sudah kadaluarsa
     */
    @Override
    public void respond(boolean isAccepted) {
        if (this.status == InteractionStatus.EXPIRED) {
            throw new IllegalStateException("Offer sudah kadaluarsa!");
        }
        this.status = isAccepted ? InteractionStatus.ACCEPTED : InteractionStatus.REJECTED;
    }

    /**
     * Mengembalikan status tawaran saat ini.
     *
     * @return status tawaran (PENDING, ACCEPTED, REJECTED, EXPIRED)
     */
    @Override
    public InteractionStatus getStatus() {
        return status;
    }

    /**
     * Mengembalikan waktu pembuatan tawaran.
     *
     * @return waktu tawaran dibuat
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getSalaryOffered() {
        return salaryOffered;
    }

    public void setSalaryOffered(BigDecimal salaryOffered) {
        this.salaryOffered = salaryOffered;
    }

    public LocalDateTime getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDateTime expiredDate) {
        this.expiredDate = expiredDate;
    }

    public Manager getSender() {
        return sender;
    }

    public void setSender(Manager sender) {
        this.sender = sender;
    }

    public BandMember getReceiver() {
        return receiver;
    }

    public void setReceiver(BandMember receiver) {
        this.receiver = receiver;
    }

    public Band getBand() {
        return band;
    }

    public void setBand(Band band) {
        this.band = band;
    }
}