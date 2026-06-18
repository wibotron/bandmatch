package com.bandmatch.bandmatch.domain.interaction;

import com.bandmatch.bandmatch.domain.band.Recruitment;
import com.bandmatch.bandmatch.domain.user.BandMember;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Application implements MusicInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String motivationLetter;
    private String availabilityNote;

    @Enumerated(EnumType.STRING)
    private InteractionStatus status = InteractionStatus.PENDING;

    @ManyToOne
    private BandMember applicant;

    @ManyToOne
    private Recruitment targetRecruitment;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Application() {}

    public Application(String motivationLetter, String availabilityNote,
                       BandMember applicant, Recruitment targetRecruitment) {
        this.motivationLetter = motivationLetter;
        this.availabilityNote = availabilityNote;
        this.applicant = applicant;
        this.targetRecruitment = targetRecruitment;
    }

    @Override
    public void send() {
        if (!targetRecruitment.isStillOpen()) {
            this.status = InteractionStatus.EXPIRED;
            throw new IllegalStateException("Rekrutmen sudah tutup!");
        }
        this.status = InteractionStatus.PENDING;
    }

    @Override
    public void respond(boolean isAccepted) {
        this.status = isAccepted ? InteractionStatus.ACCEPTED : InteractionStatus.REJECTED;
    }

    @Override
    public InteractionStatus getStatus() { return status; }

    @Override
    public LocalDateTime getTimestamp() { return createdAt; }

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMotivationLetter() { return motivationLetter; }
    public void setMotivationLetter(String motivationLetter) { this.motivationLetter = motivationLetter; }
    public String getAvailabilityNote() { return availabilityNote; }
    public void setAvailabilityNote(String availabilityNote) { this.availabilityNote = availabilityNote; }
    public BandMember getApplicant() { return applicant; }
    public void setApplicant(BandMember applicant) { this.applicant = applicant; }
    public Recruitment getTargetRecruitment() { return targetRecruitment; }
    public void setTargetRecruitment(Recruitment targetRecruitment) { this.targetRecruitment = targetRecruitment; }
}