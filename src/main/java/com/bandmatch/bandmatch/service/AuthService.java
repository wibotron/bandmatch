package com.bandmatch.bandmatch.service;

import com.bandmatch.bandmatch.domain.user.BandMember;
import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.domain.user.User;
import com.bandmatch.bandmatch.domain.portfolio.Portfolio;
import com.bandmatch.bandmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // 1. REGISTER MANAGER
    public Manager registerManager(String name, String email, String password, String companyPosition) {
        // Cek apakah email sudah terdaftar
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }
        Manager manager = new Manager(name, email, password, companyPosition);
        return userRepository.save(manager);
    }

    // 2. REGISTER BAND MEMBER
    public BandMember registerBandMember(String name, String email, String password,
                                         String stageName, String primaryInstrument) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }
        BandMember member = new BandMember(name, email, password, stageName, primaryInstrument);
        // Portfolio otomatis dibuat di constructor BandMember
        return userRepository.save(member);
    }

    // 3. LOGIN (Ini menunjukkan POLYMORPHISM!)
    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email tidak ditemukan!");
        }

        User user = userOpt.get();

        // Memanggil method authenticate yang ada di ABSTRACT CLASS User
        // Di sini polimorfismenya: user bisa berupa Manager atau BandMember,
        // tapi method authenticate() tetap berjalan karena diwarisi dari User.
        if (!user.authenticate(password)) {
            throw new RuntimeException("Password salah!");
        }

        return user; // Mengembalikan User (abstract), tetapi objeknya konkret (Manager/BandMember)
    }
}