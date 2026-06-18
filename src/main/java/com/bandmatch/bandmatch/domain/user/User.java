package com.bandmatch.bandmatch.domain.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Kelas abstrak User yang menjadi dasar (base class) untuk seluruh pengguna
 * sistem BandMatch.
 * <p>
 * User mendefinisikan atribut-atribut dasar seperti identitas, kredensial,
 * dan waktu pembuatan akun. Kelas ini menggunakan strategi inheritance
 * {@link InheritanceType#JOINED}, sehingga setiap subclass (Manager, BandMember)
 * memiliki tabel terpisah yang terhubung melalui foreign key ke tabel users.
 * </p>
 * <p>
 * Method abstrak {@link #getDashboardRedirectUrl()} dan {@link #getPermissions()}
 * memungkinkan polimorfisme, di mana setiap subclass dapat menentukan perilaku
 * yang berbeda untuk dashboard dan hak akses.
 * </p>
 */
@Entity
@Table(name = "users") // Nama tabel diubah menjadi 'users' untuk menghindari reserved keyword di H2
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    // =========================================================================
    // FIELD / KOLOM DATABASE
    // =========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nama lengkap pengguna */
    @Column(nullable = false)
    private String name;

    /** Alamat email (unik dan wajib diisi) */
    @Column(unique = true, nullable = false)
    private String email;

    /** Password (akan di-hash menggunakan BCrypt pada implementasi selanjutnya) */
    @Column(nullable = false)
    private String password;

    /** Waktu pembuatan akun (diisi otomatis saat instance dibuat) */
    private LocalDateTime createdAt;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /** Constructor default wajib untuk JPA */
    public User() {}

    /**
     * Constructor untuk membuat user baru.
     * Waktu pembuatan akun (createdAt) akan diisi otomatis dengan waktu sekarang.
     *
     * @param name     nama lengkap pengguna
     * @param email    alamat email (harus unik)
     * @param password password (plain text, akan di-hash di service layer)
     */
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // =========================================================================
    // CONCRETE METHODS (diwarisi oleh semua subclass)
    // =========================================================================

    /**
     * Melakukan autentikasi sederhana dengan membandingkan password input
     * dengan password yang tersimpan di database.
     * <p>
     * <strong>Catatan:</strong> Saat ini masih menggunakan plain text comparison.
     * Akan di-upgrade menggunakan BCrypt password encoder pada implementasi
     * production untuk keamanan yang lebih baik.
     * </p>
     *
     * @param rawPassword password yang dimasukkan oleh user (plain text)
     * @return true jika password cocok, false jika tidak
     */
    public boolean authenticate(String rawPassword) {
        // Nanti upgrade ke BCrypt, sekarang plain dulu
        return this.password.equals(rawPassword);
    }

    // =========================================================================
    // ABSTRACT METHODS (Polymorphism)
    // =========================================================================

    /**
     * Mengembalikan URL redirect ke dashboard yang sesuai dengan peran (role) user.
     * <p>
     * Method ini bersifat abstrak dan harus diimplementasikan oleh setiap subclass.
     * - {@link Manager} akan mengembalikan "/manager/dashboard"
     * - {@link BandMember} akan mengembalikan "/member/dashboard"
     * </p>
     *
     * @return URL dashboard spesifik peran
     */
    public abstract String getDashboardRedirectUrl();

    /**
     * Mengembalikan set permission / hak akses yang dimiliki oleh user.
     * <p>
     * Method ini bersifat abstrak dan harus diimplementasikan oleh setiap subclass.
     * Permission digunakan untuk menentukan fitur-fitur apa yang dapat diakses
     * oleh user di dalam aplikasi.
     * </p>
     *
     * @return Set berisi string permission (misal: "MANAGE_BAND", "EXPLORE_BAND", dll)
     */
    public abstract Set<String> getPermissions();

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}