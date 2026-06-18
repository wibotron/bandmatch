package com.bandmatch.bandmatch.controller;

import com.bandmatch.bandmatch.domain.user.User;
import com.bandmatch.bandmatch.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller untuk mengelola autentikasi pengguna (registrasi, login, logout).
 * Semua endpoint diarahkan ke halaman berbasis Thymeleaf.
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Mengarahkan root URL ke halaman registrasi sebagai entry point aplikasi.
     *
     * @return redirect ke /register
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/register";
    }

    /**
     * Menampilkan formulir pendaftaran akun.
     * Form ini digunakan oleh Manager maupun BandMember.
     *
     * @return nama template Thymeleaf 'register'
     */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    /**
     * Memproses data pendaftaran dari formulir.
     * Berdasarkan role yang dipilih, akan membuat instance Manager atau BandMember.
     * Jika terjadi error (misal email sudah terdaftar), akan kembali ke halaman registrasi dengan pesan error.
     *
     * @param name                nama lengkap pengguna
     * @param email               alamat email (unik)
     * @param password            password (plain, akan di-hash oleh service)
     * @param role                peran pengguna: "manager" atau "member"
     * @param companyPosition     jabatan (hanya untuk manager, opsional)
     * @param stageName           nama panggung (hanya untuk member, opsional)
     * @param primaryInstrument   instrumen utama (hanya untuk member, opsional)
     * @param model               model untuk menyimpan atribut tampilan
     * @return                    nama template tujuan (sukses atau error)
     */
    @PostMapping("/register")
    public String processRegister(@RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String role,
                                  @RequestParam(required = false) String companyPosition,
                                  @RequestParam(required = false) String stageName,
                                  @RequestParam(required = false) String primaryInstrument,
                                  Model model) {
        try {
            User registeredUser;
            // Polimorfisme: memilih subclass berdasarkan role
            if ("manager".equals(role)) {
                registeredUser = authService.registerManager(name, email, password, companyPosition);
            } else {
                registeredUser = authService.registerBandMember(name, email, password, stageName, primaryInstrument);
            }

            // Tambahkan atribut ke model untuk ditampilkan di halaman sukses
            model.addAttribute("userName", registeredUser.getName());
            model.addAttribute("dashboardUrl", registeredUser.getDashboardRedirectUrl());
            model.addAttribute("role", role);
            return "register-success";

        } catch (RuntimeException e) {
            // Tangani error bisnis (misal: email duplikat)
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    /**
     * Menampilkan formulir login.
     *
     * @return nama template Thymeleaf 'login'
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    /**
     * Memproses kredensial login dan membuat session jika berhasil.
     * Menggunakan polimorfisme untuk mengarahkan user ke dashboard yang sesuai
     * berdasarkan peran (Manager atau BandMember).
     *
     * @param email    alamat email pengguna
     * @param password password yang dimasukkan (plain)
     * @param session  objek HttpSession untuk menyimpan data user yang login
     * @param model    model untuk menyampaikan pesan error jika login gagal
     * @return         redirect ke dashboard user atau kembali ke halaman login
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        try {
            // Melakukan autentikasi dan mendapatkan objek User (bisa Manager atau BandMember)
            User loggedInUser = authService.login(email, password);

            // Simpan informasi user ke dalam session untuk digunakan di seluruh aplikasi
            session.setAttribute("userId", loggedInUser.getId());
            // Cek permission untuk menentukan role (MANAGE_BAND hanya dimiliki oleh Manager)
            session.setAttribute("userRole", loggedInUser.getPermissions().contains("MANAGE_BAND") ? "manager" : "member");
            session.setAttribute("userName", loggedInUser.getName());

            // Redirect ke dashboard spesifik peran menggunakan method abstrak yang di-override
            return "redirect:" + loggedInUser.getDashboardRedirectUrl();

        } catch (RuntimeException e) {
            // Jika login gagal (email tidak ditemukan atau password salah), tampilkan error
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    /**
     * Menghapus session pengguna dan mengarahkan kembali ke halaman login.
     *
     * @param session session yang akan di-invalidate
     * @return redirect ke /login
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}