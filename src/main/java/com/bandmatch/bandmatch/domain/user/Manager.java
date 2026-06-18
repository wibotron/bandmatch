package com.bandmatch.bandmatch.domain.user;

import com.bandmatch.bandmatch.domain.band.Band;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Manager extends User {

    private String companyPosition;

    // Satu Manager bisa mengelola banyak Band (Aggregation/Association)
    @OneToMany(mappedBy = "manager")
    private List<Band> managedBands = new ArrayList<>();

    public Manager() {}

    public Manager(String name, String email, String password, String companyPosition) {
        super(name, email, password);
        this.companyPosition = companyPosition;
    }

    @Override
    public String getDashboardRedirectUrl() {
        return "/manager/dashboard";
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("MANAGE_BAND", "SEARCH_MUSICIAN", "SEND_OFFER");
    }

    // --- BUSINESS METHOD ---
    public Band createBand(String bandName) {
        Band band = new Band(bandName, this);
        this.managedBands.add(band);
        return band;
    }

    // GETTERS & SETTERS
    public String getCompanyPosition() { return companyPosition; }
    public void setCompanyPosition(String companyPosition) { this.companyPosition = companyPosition; }
    public List<Band> getManagedBands() { return managedBands; }
    public void setManagedBands(List<Band> managedBands) { this.managedBands = managedBands; }
}