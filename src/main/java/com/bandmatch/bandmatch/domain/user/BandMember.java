package com.bandmatch.bandmatch.domain.user;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.portfolio.Portfolio;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class BandMember extends User {

    private String stageName;
    private String primaryInstrument; // "Gitar", "Drum", "Vokal"

    // KOMPOSISI: BandMember mati, Portfolio ikut mati
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
    // Tambahkan field ini:
    @ManyToMany(mappedBy = "members")  // merujuk ke field 'members' di class Band
    private List<Band> bands = new ArrayList<>();

    // Tambahkan getter dan setter-nya:
    public List<Band> getBands() {
        return bands;
    }

    public void setBands(List<Band> bands) {
        this.bands = bands;
    }
    public BandMember() {}

    public BandMember(String name, String email, String password, String stageName, String primaryInstrument) {
        super(name, email, password);
        this.stageName = stageName;
        this.primaryInstrument = primaryInstrument;
        this.portfolio = new Portfolio(); // Portofolio default
    }

    @Override
    public String getDashboardRedirectUrl() {
        return "/member/dashboard";
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("MANAGE_PORTFOLIO", "EXPLORE_BAND", "SUBMIT_APPLICATION");
    }

    // --- BUSINESS METHOD ---
    public void updatePortfolio(Portfolio newPortfolio) {
        if (newPortfolio != null) {
            this.portfolio = newPortfolio;
        }
    }

    // GETTERS & SETTERS
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public String getPrimaryInstrument() { return primaryInstrument; }
    public void setPrimaryInstrument(String primaryInstrument) { this.primaryInstrument = primaryInstrument; }
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
}