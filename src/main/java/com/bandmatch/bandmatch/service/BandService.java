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
        Recruitment recruitment = band.openRecruitment(position, requiredInstrument, deadline);
        return recruitmentRepository.save(recruitment);
    }

    // CREATE (sudah pakai genres list)
    public Band createBand(Band bandData, Long managerId) {
        Manager manager = (Manager) userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));

        Band band = new Band(bandData.getName(), manager);
        band.setDescription(bandData.getDescription());
        band.setGenres(bandData.getGenres());               // <-- MULTIGENRE
        band.setYoutubeProfile(bandData.getYoutubeProfile());
        band.setSpotifyProfile(bandData.getSpotifyProfile());

        return bandRepository.save(band);
    }

    // READ
    public List<Band> getBandsByManager(Long managerId) {
        Manager manager = (Manager) userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));
        return manager.getManagedBands();
    }

    public Band getBandByIdAndManager(Long bandId, Long managerId) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));
        if (!band.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Anda tidak berhak mengelola band ini!");
        }
        return band;
    }

    // UPDATE (sudah pakai genres list)
    public Band updateBand(Band updatedBand) {
        Band existing = bandRepository.findById(updatedBand.getId())
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));

        existing.setName(updatedBand.getName());
        existing.setDescription(updatedBand.getDescription());
        existing.setGenres(updatedBand.getGenres());           // <-- MULTIGENRE
        existing.setYoutubeProfile(updatedBand.getYoutubeProfile());
        existing.setSpotifyProfile(updatedBand.getSpotifyProfile());

        return bandRepository.save(existing);
    }

    // DELETE
    public void deleteBand(Long bandId, Long managerId) {
        Band band = getBandByIdAndManager(bandId, managerId);
        bandRepository.delete(band);
    }

    // FILTER (tetap pakai satu genre, cari band yang mengandung genre tersebut)
    public List<Band> searchBands(String genre, String position) {
        if ((genre == null || genre.isEmpty()) && (position == null || position.isEmpty())) {
            return bandRepository.findAll();
        }
        // Karena repository query menggunakan :genre, kita lewatkan parameter
        return bandRepository.findBandsByGenreAndPosition(
                genre != null && !genre.isEmpty() ? genre : null,
                position != null && !position.isEmpty() ? position : null
        );
    }

    public List<Band> getAllBands() {
        return bandRepository.findAll();
    }
}