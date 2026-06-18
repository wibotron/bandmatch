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

/**
 * Service untuk mengelola operasi bisnis terkait Band dan Recruitment.
 * <p>
 * Service ini menangani:
 * <ul>
 *   <li>CRUD Band (Create, Read, Update, Delete) dengan validasi kepemilikan manager</li>
 *   <li>Manajemen rekrutmen (membuka rekrutmen baru)</li>
 *   <li>Pencarian band berdasarkan genre dan posisi (untuk eksplorasi member)</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Catatan:</strong> Service ini memanfaatkan {@link Band} sebagai
 * Rich Domain Model, di mana logika bisnis seperti {@link Band#openRecruitment}
 * berada di dalam entity.
 * </p>
 */
@Service
public class BandService {

    @Autowired
    private BandRepository bandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    // =========================================================================
    // MANAJEMEN REKRUTMEN
    // =========================================================================

    /**
     * Membuka rekrutmen baru untuk sebuah band.
     * <p>
     * Method ini memanfaatkan business method {@link Band#openRecruitment}
     * yang ada di entity Band (Rich Domain Model), sehingga logika pembuatan
     * rekrutmen berada di dalam entity, bukan di service.
     * </p>
     *
     * @param bandId             ID band tempat rekrutmen akan dibuka
     * @param position           posisi yang dibutuhkan (misal: "Lead Gitar")
     * @param requiredInstrument instrumen spesifik yang dibutuhkan
     * @param deadline           batas akhir pendaftaran
     * @return objek Recruitment yang telah disimpan
     * @throws RuntimeException jika band tidak ditemukan
     */
    public Recruitment openRecruitment(Long bandId, String position, String requiredInstrument, LocalDateTime deadline) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));
        Recruitment recruitment = band.openRecruitment(position, requiredInstrument, deadline);
        return recruitmentRepository.save(recruitment);
    }

    // =========================================================================
    // CRUD BAND
    // =========================================================================

    /**
     * Membuat band baru yang dikelola oleh manager tertentu.
     * <p>
     * Data band diambil dari objek {@link Band} yang diterima dari form.
     * Field-field yang disimpan: nama, deskripsi, genre (multigenre),
     * YouTube profile, dan Spotify profile.
     * </p>
     *
     * @param bandData  objek Band berisi data dari form (belum memiliki ID)
     * @param managerId ID manager yang akan menjadi pemilik band
     * @return objek Band yang telah disimpan di database (dengan ID terisi)
     * @throws RuntimeException jika manager tidak ditemukan
     */
    public Band createBand(Band bandData, Long managerId) {
        Manager manager = (Manager) userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));

        Band band = new Band(bandData.getName(), manager);
        band.setDescription(bandData.getDescription());
        band.setGenres(bandData.getGenres());          // Mendukung multigenre
        band.setYoutubeProfile(bandData.getYoutubeProfile());
        band.setSpotifyProfile(bandData.getSpotifyProfile());

        return bandRepository.save(band);
    }

    /**
     * Mengambil daftar semua band yang dikelola oleh seorang manager.
     *
     * @param managerId ID manager
     * @return List berisi band-band yang dikelola, atau List kosong jika tidak ada
     * @throws RuntimeException jika manager tidak ditemukan
     */
    public List<Band> getBandsByManager(Long managerId) {
        Manager manager = (Manager) userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));
        return manager.getManagedBands();
    }

    /**
     * Mengambil data band berdasarkan ID, sekaligus melakukan validasi
     * kepemilikan oleh manager tertentu.
     * <p>
     * Method ini digunakan untuk operasi edit dan delete, untuk memastikan
     * bahwa manager yang sedang login memang berhak mengelola band tersebut.
     * </p>
     *
     * @param bandId    ID band yang akan diambil
     * @param managerId ID manager yang sedang login
     * @return objek Band jika ditemukan dan dikelola oleh manager tersebut
     * @throws RuntimeException jika band tidak ditemukan atau manager tidak berhak
     */
    public Band getBandByIdAndManager(Long bandId, Long managerId) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));
        if (!band.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Anda tidak berhak mengelola band ini!");
        }
        return band;
    }

    /**
     * Memperbarui data band yang sudah ada.
     * <p>
     * Field yang diperbarui: nama, deskripsi, genre (multigenre),
     * YouTube profile, Spotify profile.
     * </p>
     * <p>
     * Method ini mengasumsikan bahwa objek {@code updatedBand} sudah memiliki
     * ID yang valid dan telah melalui validasi kepemilikan sebelumnya.
     * </p>
     *
     * @param updatedBand objek Band berisi data baru (dengan ID yang sama)
     * @return objek Band yang telah diperbarui
     * @throws RuntimeException jika band tidak ditemukan
     */
    public Band updateBand(Band updatedBand) {
        Band existing = bandRepository.findById(updatedBand.getId())
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));

        // Perbarui field-field yang boleh diubah
        existing.setName(updatedBand.getName());
        existing.setDescription(updatedBand.getDescription());
        existing.setGenres(updatedBand.getGenres());          // Mendukung multigenre
        existing.setYoutubeProfile(updatedBand.getYoutubeProfile());
        existing.setSpotifyProfile(updatedBand.getSpotifyProfile());

        return bandRepository.save(existing);
    }

    /**
     * Menghapus band berdasarkan ID, dengan validasi kepemilikan manager.
     * <p>
     * Method ini akan memanggil {@link #getBandByIdAndManager} terlebih dahulu
     * untuk memastikan manager berhak menghapus band tersebut.
     * </p>
     *
     * @param bandId    ID band yang akan dihapus
     * @param managerId ID manager yang sedang login
     * @throws RuntimeException jika band tidak ditemukan atau manager tidak berhak
     */
    public void deleteBand(Long bandId, Long managerId) {
        Band band = getBandByIdAndManager(bandId, managerId);
        bandRepository.delete(band);
    }

    // =========================================================================
    // PENCARIAN / FILTER
    // =========================================================================

    /**
     * Mencari band berdasarkan genre dan/atau posisi rekrutmen.
     * <p>
     * Jika kedua parameter null atau kosong, maka semua band akan dikembalikan.
     * </p>
     * <p>
     * <strong>Catatan:</strong> Parameter genre adalah single String untuk filter,
     * meskipun band mendukung multigenre. Query akan mencari band yang memiliki
     * genre tersebut di dalam daftar {@link Band#getGenres()}.
     * </p>
     *
     * @param genre    filter genre (opsional, case-sensitive sesuai data)
     * @param position filter posisi rekrutmen (opsional)
     * @return List band yang sesuai dengan filter, atau semua band jika filter kosong
     */
    public List<Band> searchBands(String genre, String position) {
        if ((genre == null || genre.isEmpty()) && (position == null || position.isEmpty())) {
            return bandRepository.findAll();
        }
        return bandRepository.findBandsByGenreAndPosition(
                genre != null && !genre.isEmpty() ? genre : null,
                position != null && !position.isEmpty() ? position : null
        );
    }

    /**
     * Mengambil semua band yang terdaftar di sistem.
     * <p>
     * Method ini digunakan untuk keperluan eksplorasi band (untuk member)
     * dan untuk mengambil daftar genre unik di {@code MemberController}.
     * </p>
     *
     * @return List berisi semua band
     */
    public List<Band> getAllBands() {
        return bandRepository.findAll();
    }
}