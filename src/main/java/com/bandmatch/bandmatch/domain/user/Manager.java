package com.bandmatch.bandmatch.domain.user;

import com.bandmatch.bandmatch.domain.band.Band;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entitas Manager yang merepresentasikan seorang manajer band.
 * <p>
 * Manager adalah turunan dari {@link User} (inheritance). Seorang Manager
 * memiliki wewenang untuk mengelola satu atau lebih {@link Band}.
 * </p>
 * <p>
 * Hak akses (permissions) Manager: mengelola band, mencari musisi, dan mengirim
 * tawaran (offer) kepada musisi.
 * </p>
 */
@Entity
public class Manager extends User {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    /** Jabatan atau posisi manager di perusahaan / agensi (misal: "Talent Manager") */
    private String companyPosition;

    // =========================================================================
    // RELASI DENGAN ENTITAS LAIN
    // =========================================================================

    /**
     * Relasi One-to-Many dengan Band (Agregasi / Directed Association).
     * Seorang manager dapat mengelola banyak band, namun band hanya memiliki
     * satu manager. Relasi ini bersifat asosiasi biasa (bukan komposisi),
     * sehingga jika manager dihapus, band tidak otomatis terhapus.
     * <p>
     * MappedBy mengacu pada field 'manager' di sisi Band.
     * </p>
     */
    @OneToMany(mappedBy = "manager")
    private List<Band> managedBands = new ArrayList<>();

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /** Constructor default wajib untuk JPA */
    public Manager() {}

    /**
     * Constructor untuk membuat Manager baru.
     *
     * @param name             nama lengkap
     * @param email            alamat email (unik)
     * @param password         password (akan di-hash di service layer)
     * @param companyPosition  jabatan manager
     */
    public Manager(String name, String email, String password, String companyPosition) {
        super(name, email, password);
        this.companyPosition = companyPosition;
    }

    // =========================================================================
    // IMPLEMENTASI METHOD ABSTRAK DARI User
    // =========================================================================

    /**
     * Mengembalikan URL redirect dashboard untuk Manager.
     * Method ini meng-override method abstrak dari {@link User}.
     *
     * @return URL "/manager/dashboard"
     */
    @Override
    public String getDashboardRedirectUrl() {
        return "/manager/dashboard";
    }

    /**
     * Mengembalikan set permission / hak akses yang dimiliki Manager.
     * Method ini meng-override method abstrak dari {@link User}.
     *
     * @return Set berisi permission MANAGE_BAND, SEARCH_MUSICIAN, SEND_OFFER
     */
    @Override
    public Set<String> getPermissions() {
        return Set.of("MANAGE_BAND", "SEARCH_MUSICIAN", "SEND_OFFER");
    }

    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================

    /**
     * Membuat band baru yang dikelola oleh manager ini.
     * Band baru akan otomatis ditambahkan ke daftar managedBands.
     *
     * @param bandName nama band yang akan dibuat
     * @return objek Band yang baru dibuat
     */
    public Band createBand(String bandName) {
        Band band = new Band(bandName, this);
        this.managedBands.add(band);
        return band;
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================

    public String getCompanyPosition() {
        return companyPosition;
    }

    public void setCompanyPosition(String companyPosition) {
        this.companyPosition = companyPosition;
    }

    public List<Band> getManagedBands() {
        return managedBands;
    }

    public void setManagedBands(List<Band> managedBands) {
        this.managedBands = managedBands;
    }
}