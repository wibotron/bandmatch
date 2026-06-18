package com.bandmatch.bandmatch.domain.band;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String position;           // "Gitaris", "Vokalis"
    private String requiredInstrument; // "Gitar Elektrik"
    private String status;             // "OPEN" atau "CLOSED"
    private LocalDateTime deadline;

    @ManyToOne
    @JoinColumn(name = "band_id")
    private Band band;

    public Recruitment() {}

    public Recruitment(String position, String requiredInstrument, LocalDateTime deadline) {
        this.position = position;
        this.requiredInstrument = requiredInstrument;
        this.deadline = deadline;
        this.status = "OPEN";
    }

    public boolean isStillOpen() {
        return "OPEN".equals(this.status) && LocalDateTime.now().isBefore(deadline);
    }

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getRequiredInstrument() { return requiredInstrument; }
    public void setRequiredInstrument(String requiredInstrument) { this.requiredInstrument = requiredInstrument; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Band getBand() { return band; }
    public void setBand(Band band) { this.band = band; }
}