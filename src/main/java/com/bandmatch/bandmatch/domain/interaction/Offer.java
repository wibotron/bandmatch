package com.bandmatch.bandmatch.domain.interaction;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.domain.user.BandMember;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Offer implements MusicInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private BigDecimal salaryOffered;
    private LocalDateTime expiredDate;

    @Enumerated(EnumType.STRING)
    private InteractionStatus status = InteractionStatus.PENDING;

    @ManyToOne
    private Manager sender;

    @ManyToOne
    private BandMember receiver;

    @ManyToOne
    @JoinColumn(name = "band_id") // <-- INI BARU
    private Band band;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Offer() {}

    public Offer(String message, BigDecimal salaryOffered, LocalDateTime expiredDate,
                 Manager sender, BandMember receiver, Band band) { // <-- TAMBAH band di constructor
        this.message = message;
        this.salaryOffered = salaryOffered;
        this.expiredDate = expiredDate;
        this.sender = sender;
        this.receiver = receiver;
        this.band = band;
    }

    @Override
    public void send() {
        if (LocalDateTime.now().isAfter(expiredDate)) {
            this.status = InteractionStatus.EXPIRED;
        } else {
            this.status = InteractionStatus.PENDING;
        }
    }

    @Override
    public void respond(boolean isAccepted) {
        if (this.status == InteractionStatus.EXPIRED) {
            throw new IllegalStateException("Offer sudah kadaluarsa!");
        }
        this.status = isAccepted ? InteractionStatus.ACCEPTED : InteractionStatus.REJECTED;
    }

    @Override
    public InteractionStatus getStatus() { return status; }

    @Override
    public LocalDateTime getTimestamp() { return createdAt; }

    // --- GETTERS & SETTERS (tambahkan getter/setter untuk band) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public BigDecimal getSalaryOffered() { return salaryOffered; }
    public void setSalaryOffered(BigDecimal salaryOffered) { this.salaryOffered = salaryOffered; }
    public LocalDateTime getExpiredDate() { return expiredDate; }
    public void setExpiredDate(LocalDateTime expiredDate) { this.expiredDate = expiredDate; }
    public Manager getSender() { return sender; }
    public void setSender(Manager sender) { this.sender = sender; }
    public BandMember getReceiver() { return receiver; }
    public void setReceiver(BandMember receiver) { this.receiver = receiver; }
    public Band getBand() { return band; }          // <-- BARU
    public void setBand(Band band) { this.band = band; } // <-- BARU
}