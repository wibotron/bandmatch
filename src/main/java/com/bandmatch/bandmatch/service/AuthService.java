package com.bandmatch.bandmatch.service;

import com.bandmatch.bandmatch.domain.user.BandMember;
import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.domain.user.User;
import com.bandmatch.bandmatch.domain.portfolio.Portfolio;
import com.bandmatch.bandmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service untuk mengelola autentikasi pengguna (registrasi dan login).
 * <p>
 * Service ini bertanggung jawab untuk:
 * <ul>
 *   <li>Mendaftarkan pengguna baru (Manager atau BandMember)</li>
 *   <li>Melakukan autentikasi login dengan memanfaatkan polymorphism dari {@link User}</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Catatan Polimorfisme:</strong> Method {@link #login(String, String)}
 * mengembalikan tipe {@link User} (abstract class), tetapi objek yang dikembalikan
 * adalah instance konkret dari {@link Manager} atau {@link BandMember}. Hal ini
 * memungkinkan pemanggilan method {@link User#authenticate(String)} secara
 * polimorfik tanpa harus mengetahui tipe sebenarnya.
 * </p>
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // =========================================================================
    // REGISTRASI
    // =========================================================================

    /**
     * Mendaftarkan pengguna baru sebagai Manager.
     * <p>
     * Validasi dilakukan untuk memastikan email belum terdaftar sebelumnya.
     * Jika email sudah digunakan, akan dilempar {@link RuntimeException}.
     * </p>
     *
     * @param name             nama lengkap manager
     * @param email            alamat email (harus unik)
     * @param password         password (plain text, akan di-hash pada implementasi selanjutnya)
     * @param companyPosition  jabatan atau posisi manager di perusahaan/agensi
     * @return objek Manager yang telah disimpan di database
     * @throws RuntimeException jika email sudah terdaftar
     */
    public Manager registerManager(String name, String email, String password, String companyPosition) {
        // Cek apakah email sudah terdaftar di sistem
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }
        Manager manager = new Manager(name, email, password, companyPosition);
        return userRepository.save(manager);
    }

    /**
     * Mendaftarkan pengguna baru sebagai BandMember (musisi).
     * <p>
     * Validasi dilakukan untuk memastikan email belum terdaftar sebelumnya.
     * Portofolio (Portfolio) akan dibuat secara otomatis di dalam constructor
     * {@link BandMember#BandMember(String, String, String, String, String)}.
     * </p>
     *
     * @param name               nama lengkap member
     * @param email              alamat email (harus unik)
     * @param password           password (plain text, akan di-hash pada implementasi selanjutnya)
     * @param stageName          nama panggung (stage name)
     * @param primaryInstrument  instrumen utama yang dikuasai
     * @return objek BandMember yang telah disimpan di database
     * @throws RuntimeException jika email sudah terdaftar
     */
    public BandMember registerBandMember(String name, String email, String password,
                                         String stageName, String primaryInstrument) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }
        BandMember member = new BandMember(name, email, password, stageName, primaryInstrument);
        // Portfolio otomatis dibuat di constructor BandMember
        return userRepository.save(member);
    }

    // =========================================================================
    // AUTENTIKASI (LOGIN)
    // =========================================================================

    /**
     * Melakukan proses login dengan memvalidasi email dan password.
     * <p>
     * <strong>Implementasi Polimorfisme:</strong>
     * Method ini mengembalikan tipe {@link User} (abstract class), tetapi objek
     * yang dikembalikan sebenarnya adalah instance dari subclass konkret
     * ({@link Manager} atau {@link BandMember}). Pemanggilan method
     * {@link User#authenticate(String)} akan merujuk ke implementasi yang
     * diwarisi dari abstract class {@link User}, yang berlaku sama untuk
     * semua subclass.
     * </p>
     *
     * @param email    alamat email yang didaftarkan
     * @param password password yang dimasukkan (plain text)
     * @return objek User (bisa berupa Manager atau BandMember)
     * @throws RuntimeException jika email tidak ditemukan atau password salah
     */
    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        // Validasi 1: Apakah email terdaftar?
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email tidak ditemukan!");
        }

        User user = userOpt.get();

        // Validasi 2: Apakah password cocok?
        // Memanggil method authenticate yang ada di ABSTRACT CLASS User.
        // Polimorfisme: user bisa berupa Manager atau BandMember,
        // tetapi method authenticate() tetap berjalan karena diwarisi dari User.
        if (!user.authenticate(password)) {
            throw new RuntimeException("Password salah!");
        }

        // Mengembalikan User (abstract), tetapi objeknya konkret (Manager/BandMember)
        return user;
    }
}