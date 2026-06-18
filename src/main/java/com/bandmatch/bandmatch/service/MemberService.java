package com.bandmatch.bandmatch.service;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.portfolio.Portfolio;
import com.bandmatch.bandmatch.domain.user.BandMember;
import com.bandmatch.bandmatch.repository.BandMemberRepository;
import com.bandmatch.bandmatch.repository.BandRepository;
import com.bandmatch.bandmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service untuk mengelola operasi bisnis yang terkait dengan BandMember (musisi).
 * <p>
 * Service ini menangani:
 * <ul>
 *   <li>Pengambilan data member berdasarkan ID</li>
 *   <li>Manajemen portofolio (update portofolio member)</li>
 *   <li>Eksplorasi band untuk member (melihat semua band dan detail band)</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Catatan Komposisi:</strong>
 * Relasi {@link BandMember} dengan {@link Portfolio} adalah komposisi
 * ({@code @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)}).
 * Saat member di-update, portofolio ikut tersimpan secara otomatis karena
 * cascade yang didefinisikan di entity.
 * </p>
 */
@Service
public class MemberService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BandMemberRepository bandMemberRepository;

    @Autowired
    private BandRepository bandRepository;

    // =========================================================================
    // MANAJEMEN MEMBER
    // =========================================================================

    /**
     * Mengambil data member berdasarkan ID.
     * <p>
     * Method ini melakukan casting dari {@code User} ke {@code BandMember}
     * karena repository menggunakan {@code UserRepository} yang mengembalikan
     * tipe {@code User} (abstract class).
     * </p>
     *
     * @param memberId ID member yang akan diambil
     * @return objek BandMember yang ditemukan
     * @throws RuntimeException jika member tidak ditemukan
     */
    @Transactional(readOnly = true)
    public BandMember getMemberById(Long memberId) {
        return (BandMember) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan!"));
    }

    // =========================================================================
    // MANAJEMEN PORTOFOLIO
    // =========================================================================

    /**
     * Memperbarui portofolio seorang member.
     * <p>
     * <strong>Implementasi Komposisi:</strong>
     * Portofolio adalah bagian dari {@link BandMember} dengan relasi
     * {@code @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)}.
     * Oleh karena itu, menyimpan member secara otomatis juga menyimpan
     * perubahan pada portofolio (tanpa perlu repository terpisah untuk Portfolio).
     * </p>
     *
     * @param memberId         ID member yang portofolionya akan diupdate
     * @param updatedPortfolio objek Portfolio berisi data baru dari form
     * @return objek BandMember yang telah diperbarui
     * @throws RuntimeException jika member tidak ditemukan
     */
    @Transactional
    public BandMember updatePortfolio(Long memberId, Portfolio updatedPortfolio) {
        // 1. Ambil member berdasarkan ID
        BandMember member = getMemberById(memberId);

        // 2. Ambil portofolio yang sudah ada (tidak dibuat baru, hanya diupdate)
        Portfolio existingPortfolio = member.getPortfolio();

        // 3. Update field-field portofolio dengan data baru
        existingPortfolio.setBio(updatedPortfolio.getBio());
        existingPortfolio.setSkillSet(updatedPortfolio.getSkillSet());
        existingPortfolio.setYoutubeChannel(updatedPortfolio.getYoutubeChannel());
        existingPortfolio.setInstagramHandle(updatedPortfolio.getInstagramHandle());

        // 4. Simpan member (cascade ALL akan otomatis menyimpan portofolio juga)
        return bandMemberRepository.save(member);
    }

    // =========================================================================
    // EKSPLORASI BAND (UNTUK MEMBER)
    // =========================================================================

    /**
     * Mengambil semua band yang terdaftar di sistem.
     * <p>
     * Method ini digunakan oleh member untuk melihat semua band yang tersedia
     * di halaman eksplorasi, tanpa batasan kepemilikan (tidak seperti
     * {@code getBandsByManager} di {@link BandService}).
     * </p>
     *
     * @return List berisi semua band yang terdaftar
     */
    @Transactional(readOnly = true)
    public List<Band> getAllBands() {
        return bandRepository.findAll();
    }

    /**
     * Mengambil detail sebuah band berdasarkan ID.
     * <p>
     * Method ini digunakan oleh member untuk melihat halaman detail band,
     * termasuk daftar anggota dan rekrutmen aktif.
     * </p>
     *
     * @param bandId ID band yang akan diambil
     * @return objek Band yang ditemukan
     * @throws RuntimeException jika band tidak ditemukan
     */
    @Transactional(readOnly = true)
    public Band getBandDetail(Long bandId) {
        return bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));
    }
}