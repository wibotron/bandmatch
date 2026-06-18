package com.bandmatch.bandmatch.service;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.band.Recruitment;
import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.repository.BandRepository;
import com.bandmatch.bandmatch.repository.RecruitmentRepository;
import com.bandmatch.bandmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BandService {

    @Autowired
    private BandRepository bandRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private RecruitmentRepository recruitmentRepository;

    public Recruitment openRecruitment(Long bandId, String position, String requiredInstrument, LocalDateTime deadline) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));

        // Panggil method domain dari Band (Rich Domain Model)
        Recruitment recruitment = band.openRecruitment(position, requiredInstrument, deadline);

        // Simpan recruitment (cascade sudah ada, tapi lebih aman di-save langsung)
        return recruitmentRepository.save(recruitment);
    }

    // CREATE
    public Band createBand(Band bandData, Long managerId) {
        Manager manager = (Manager) userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));

        // Buat band baru dengan data dari form
        Band band = new Band(bandData.getName(), manager);
        band.setDescription(bandData.getDescription());
        band.setLocation(bandData.getLocation());
        band.setSpotifyEmbedUrl(bandData.getSpotifyEmbedUrl());

        return bandRepository.save(band);
    }

    // READ (Semua band milik manager tertentu)
    public List<Band> getBandsByManager(Long managerId) {
        Manager manager = (Manager) userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));
        return manager.getManagedBands(); // Relasi dari entity Manager
    }

    // READ (Cari band by ID, sekalian validasi kepemilikan)
    public Band getBandByIdAndManager(Long bandId, Long managerId) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));

        // Cek apakah manager ini benar-benar pemilik band (security sederhana)
        if (!band.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Anda tidak berhak mengelola band ini!");
        }
        return band;
    }

    // UPDATE
    public Band updateBand(Band updatedBand) {
        // updatedBand sudah berisi data dari form, kita simpan
        // Pastikan id-nya ada
        return bandRepository.save(updatedBand);
    }

    // DELETE
    public void deleteBand(Long bandId, Long managerId) {
        Band band = getBandByIdAndManager(bandId, managerId);
        bandRepository.delete(band);
    }
}