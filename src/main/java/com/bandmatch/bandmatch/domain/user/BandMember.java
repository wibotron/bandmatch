package com.bandmatch.bandmatch.domain.user;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.portfolio.Portfolio;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entitas BandMember yang merepresentasikan seorang musisi / anggota band.
 * <p>
 * BandMember adalah turunan dari {@link User} (inheritance). Seorang BandMember
 * memiliki portofolio (komposisi) dan dapat bergabung ke banyak band (agregasi).
 * </p>
 * <p>
 * Hak akses (permissions) BandMember: mengelola portofolio, eksplorasi band,
 * dan mengajukan lamaran (application).
 * </p>
 */
@Entity
public class BandMember extends User {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    /** Nama panggung (stage name) musisi */
    private String stageName;

    /** Instrumen utama yang dikuasai (misal: "Gitar", "Drum", "Vokal") */
    private String primaryInstrument;

    // =========================================================================
    // RELASI DENGAN ENTITAS LAIN
    // =========================================================================

    /**
     * Relasi One-to-One dengan Portfolio (KOMPOSISI).
     * BandMember memiliki satu portofolio. Jika BandMember dihapus,
     * Portfolio ikut terhapus (cascade ALL + orphanRemoval = true).
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    /**
     * Relasi Many-to-Many dengan Band (AGREGASI).
     * Seorang member dapat bergabung ke banyak band, dan band dapat memiliki
     * banyak member. Relasi ini adalah sisi mappedBy, merujuk ke field 'members'
     * di kelas Band.
     * <p>
     * Jika band dihapus, member tetap ada di sistem (lifecycle independen).
     * </p>
     */
    @ManyToMany(mappedBy = "members")
    private List<Band> bands = new ArrayList<>();

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /** Constructor default wajib untuk JPA */
    public BandMember() {}

    /**
     * Constructor untuk membuat BandMember baru.
     * Secara otomatis membuat Portfolio default kosong.
     *
     * @param name               nama lengkap
     * @param email              alamat email (unik)
     * @param password           password (akan di-hash di service layer)
     * @param stageName          nama panggung
     * @param primaryInstrument  instrumen utama
     */
    public BandMember(String name, String email, String password, String stageName, String primaryInstrument) {
        super(name, email, password);
        this.stageName = stageName;
        this.primaryInstrument = primaryInstrument;
        this.portfolio = new Portfolio(); // Portofolio default kosong
    }

    // =========================================================================
    // IMPLEMENTASI METHOD ABSTRAK DARI User
    // =========================================================================

    /**
     * Mengembalikan URL redirect dashboard untuk BandMember.
     * Method ini meng-override method abstrak dari {@link User}.
     *
     * @return URL "/member/dashboard"
     */
    @Override
    public String getDashboardRedirectUrl() {
        return "/member/dashboard";
    }

    /**
     * Mengembalikan set permission / hak akses yang dimiliki BandMember.
     * Method ini meng-override method abstrak dari {@link User}.
     *
     * @return Set berisi permission MANAGE_PORTFOLIO, EXPLORE_BAND, SUBMIT_APPLICATION
     */
    @Override
    public Set<String> getPermissions() {
        return Set.of("MANAGE_PORTFOLIO", "EXPLORE_BAND", "SUBMIT_APPLICATION");
    }

    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================

    /**
     * Memperbarui portofolio member dengan data baru.
     *
     * @param newPortfolio objek Portfolio baru yang akan menggantikan portofolio lama
     */
    public void updatePortfolio(Portfolio newPortfolio) {
        if (newPortfolio != null) {
            this.portfolio = newPortfolio;
        }
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getPrimaryInstrument() {
        return primaryInstrument;
    }

    public void setPrimaryInstrument(String primaryInstrument) {
        this.primaryInstrument = primaryInstrument;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public List<Band> getBands() {
        return bands;
    }

    public void setBands(List<Band> bands) {
        this.bands = bands;
    }
}