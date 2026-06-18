package com.bandmatch.bandmatch.service;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.band.Recruitment;
import com.bandmatch.bandmatch.domain.interaction.Application;
import com.bandmatch.bandmatch.domain.interaction.InteractionStatus;
import com.bandmatch.bandmatch.domain.interaction.Offer;
import com.bandmatch.bandmatch.domain.user.BandMember;
import com.bandmatch.bandmatch.domain.user.Manager;
import com.bandmatch.bandmatch.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service untuk menangani semua interaksi antara Manager dan BandMember,
 * termasuk pengiriman tawaran (Offer), lamaran (Application), dan respon
 * terhadap keduanya.
 * <p>
 * Service ini menggunakan interface {@link com.bandmatch.bandmatch.domain.interaction.MusicInteraction}
 * untuk memastikan bahwa Offer dan Application memiliki perilaku yang konsisten
 * (send, respond, getStatus, getTimestamp).
 * </p>
 * <p>
 * <strong>Catatan Polimorfisme:</strong>
 * Method {@link #sendOffer} dan {@link #applyRecruitment} memanggil
 * {@code offer.send()} dan {@code application.send()} yang merupakan
 * implementasi berbeda dari interface yang sama. Hal yang sama berlaku
 * untuk method {@code respond} pada Offer dan Application.
 * </p>
 */
@Service
public class InteractionService {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BandRepository bandRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private BandMemberRepository bandMemberRepository;

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    // =========================================================================
    // OFFER (TAWARAN DARI MANAGER KE MEMBER)
    // =========================================================================

    /**
     * Mengirim tawaran (offer) dari manager ke member untuk bergabung ke suatu band.
     * <p>
     * Validasi dilakukan untuk memastikan:
     * <ul>
     *   <li>Manager, member, dan band yang dirujuk ada di database</li>
     *   <li>Manager tersebut adalah pemilik dari band tujuan (berhak mengirim offer)</li>
     * </ul>
     * </p>
     * <p>
     * Method ini memanggil {@link Offer#send()} untuk mengatur status menjadi PENDING
     * atau EXPIRED berdasarkan expiredDate.
     * </p>
     *
     * @param managerId   ID manager pengirim
     * @param memberId    ID member penerima
     * @param bandId      ID band tujuan
     * @param message     pesan tawaran
     * @param salary      nominal gaji yang ditawarkan
     * @param expiredDate batas waktu tawaran
     * @return objek Offer yang telah disimpan
     * @throws RuntimeException jika manager/member/band tidak ditemukan,
     *                          atau manager tidak berhak mengelola band tersebut
     */
    @Transactional
    public Offer sendOffer(Long managerId, Long memberId, Long bandId, String message,
                           BigDecimal salary, LocalDateTime expiredDate) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));
        BandMember member = bandMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan!"));
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));

        // Validasi kepemilikan band
        if (!band.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Anda tidak berhak mengirim offer untuk band ini!");
        }

        Offer offer = new Offer(message, salary, expiredDate, manager, member, band);
        offer.send(); // set status PENDING atau EXPIRED

        return offerRepository.save(offer);
    }

    /**
     * Member merespon tawaran (offer) yang diterima.
     * <p>
     * Validasi dilakukan untuk memastikan member yang merespon adalah penerima offer.
     * Jika diterima (accepted = true), member akan otomatis ditambahkan ke band tujuan.
     * Jika ditolak, status berubah menjadi REJECTED.
     * </p>
     * <p>
     * Method ini memanggil {@link Offer#respond(boolean)} untuk mengubah status
     * menjadi ACCEPTED atau REJECTED.
     * </p>
     *
     * @param offerId  ID offer yang akan direspon
     * @param memberId ID member yang merespon (harus sama dengan receiver di offer)
     * @param accepted true jika menerima, false jika menolak
     * @return objek Offer yang telah diperbarui
     * @throws RuntimeException jika offer tidak ditemukan, atau member bukan penerima
     */
    @Transactional // <-- DITAMBAHKAN (sebelumnya tidak ada, padahal melakukan perubahan data)
    public Offer respondOffer(Long offerId, Long memberId, boolean accepted) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer tidak ditemukan!"));

        // Validasi: apakah member ini penerima offer?
        if (!offer.getReceiver().getId().equals(memberId)) {
            throw new RuntimeException("Anda tidak berhak merespon offer ini!");
        }

        // Ubah status melalui business method di entity
        offer.respond(accepted);

        // Jika diterima, tambahkan member ke band tujuan
        if (accepted) {
            Band band = offer.getBand();
            BandMember member = offer.getReceiver();

            // Cegah duplikasi anggota
            if (!band.getMembers().contains(member)) {
                band.getMembers().add(member);
                member.getBands().add(band);
                bandRepository.save(band);
                bandMemberRepository.save(member);
                System.out.println("✅ Member " + member.getName() + " bergabung ke band " + band.getName());
            } else {
                System.out.println("⚠️ Member sudah menjadi anggota band ini.");
            }
        }

        return offerRepository.save(offer);
    }

    // =========================================================================
    // APPLICATION (LAMARAN DARI MEMBER KE REKRUTMEN)
    // =========================================================================

    /**
     * Member mengajukan lamaran (application) ke sebuah rekrutmen.
     * <p>
     * Validasi dilakukan secara ketat untuk memastikan:
     * <ol>
     *   <li>Rekrutmen masih terbuka (isStillOpen())</li>
     *   <li>Member belum menjadi anggota band tersebut</li>
     *   <li>Member belum memiliki lamaran aktif (PENDING atau ACCEPTED) di band yang sama</li>
     * </ol>
     * </p>
     * <p>
     * Method ini memanggil {@link Application#send()} yang akan memvalidasi
     * status rekrutmen dan mengatur status lamaran menjadi PENDING.
     * </p>
     *
     * @param memberId          ID member pelamar
     * @param recruitmentId     ID rekrutmen yang dilamar
     * @param motivationLetter  surat motivasi dari member
     * @param availabilityNote  catatan ketersediaan member
     * @return objek Application yang telah disimpan
     * @throws RuntimeException jika member/rekrutmen tidak ditemukan,
     *                          atau salah satu validasi gagal
     */
    @Transactional
    public Application applyRecruitment(Long memberId, Long recruitmentId,
                                        String motivationLetter, String availabilityNote) {
        // 1. Ambil data member dan rekrutmen
        BandMember member = bandMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan!"));

        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new RuntimeException("Rekrutmen tidak ditemukan!"));

        Band band = recruitment.getBand();

        // 2. VALIDASI 1: Apakah rekrutmen masih aktif?
        if (!recruitment.isStillOpen()) {
            throw new RuntimeException("Rekrutmen ini sudah ditutup atau melewati deadline!");
        }

        // 3. VALIDASI 2: Apakah member sudah TERDAFTAR sebagai anggota band ini?
        boolean alreadyMember = band.getMembers().stream()
                .anyMatch(m -> m.getId().equals(memberId));
        if (alreadyMember) {
            throw new RuntimeException("Anda sudah menjadi anggota band ini!");
        }

        // 4. VALIDASI 3: Apakah member sudah punya lamaran AKTIF (PENDING atau ACCEPTED)
        //    untuk band yang sama?
        List<Application> existingApps = applicationRepository
                .findByApplicantAndTargetRecruitment_Band(member, band);

        boolean hasActiveApplication = existingApps.stream()
                .anyMatch(app -> app.getStatus() == InteractionStatus.PENDING
                        || app.getStatus() == InteractionStatus.ACCEPTED);

        if (hasActiveApplication) {
            throw new RuntimeException("Anda sudah memiliki lamaran aktif atau sudah diterima di band ini!");
        }

        // 5. Jika lolos semua validasi, buat dan kirim lamaran
        Application application = new Application(motivationLetter, availabilityNote, member, recruitment);
        application.send(); // akan set status menjadi PENDING (atau EXPIRED jika rekrutmen tutup)

        return applicationRepository.save(application);
    }

    // =========================================================================
    // MANAGER RESPON TERHADAP LAMARAN (APPLICATION)
    // =========================================================================

    /**
     * Manager merespon lamaran (application) yang masuk.
     * <p>
     * Validasi dilakukan untuk memastikan manager yang merespon adalah pemilik
     * band dari rekrutmen yang dilamar.
     * Jika diterima (accepted = true):
     * <ul>
     *   <li>Member ditambahkan ke daftar anggota band</li>
     *   <li>Rekrutmen ditutup (status CLOSED) agar tidak bisa dilamar lagi</li>
     * </ul>
     * Jika ditolak, status lamaran menjadi REJECTED.
     * </p>
     * <p>
     * Method ini memanggil {@link Application#respond(boolean)} untuk mengubah
     * status lamaran.
     * </p>
     *
     * @param applicationId ID lamaran yang akan direspon
     * @param managerId     ID manager yang merespon
     * @param accepted      true jika diterima, false jika ditolak
     * @return objek Application yang telah diperbarui
     * @throws RuntimeException jika application tidak ditemukan,
     *                          atau manager tidak berhak merespon
     */
    @Transactional
    public Application respondApplication(Long applicationId, Long managerId, boolean accepted) {
        // 1. Ambil data lamaran
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application tidak ditemukan!"));

        Band band = application.getTargetRecruitment().getBand();
        Recruitment recruitment = application.getTargetRecruitment();

        // 2. Cek hak akses manager
        if (!band.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Anda tidak berhak merespon lamaran ini!");
        }

        // 3. Ubah status lamaran (ACCEPTED / REJECTED)
        application.respond(accepted);

        // 4. JIKA DITERIMA
        if (accepted) {
            BandMember member = application.getApplicant();

            // --- A. Tambahkan member ke daftar anggota band ---
            band.getMembers().add(member);
            member.getBands().add(band);
            bandRepository.saveAndFlush(band);
            bandMemberRepository.save(member);

            // --- B. TUTUP REKRUTMEN (biar tidak bisa dilamar lagi) ---
            recruitment.setStatus("CLOSED");
            recruitmentRepository.save(recruitment);

            System.out.println("✅ Rekrutmen " + recruitment.getPosition() + " ditutup karena sudah terisi.");
        }

        // 5. Simpan lamaran
        return applicationRepository.save(application);
    }

    // =========================================================================
    // QUERY / READ-ONLY METHODS
    // =========================================================================

    /**
     * Mengambil semua member yang terdaftar di sistem.
     * Digunakan oleh manager untuk mencari musisi.
     *
     * @return List berisi semua BandMember
     */
    @Transactional(readOnly = true)
    public List<BandMember> getAllMembers() {
        return bandMemberRepository.findAll();
    }

    /**
     * Mengambil semua tawaran (offer) yang diterima oleh seorang member.
     *
     * @param memberId ID member
     * @return List offer yang diterima oleh member tersebut
     * @throws RuntimeException jika member tidak ditemukan
     */
    @Transactional(readOnly = true)
    public List<Offer> getOffersForMember(Long memberId) {
        BandMember member = bandMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan!"));
        return offerRepository.findByReceiver(member);
    }

    /**
     * Mengambil semua lamaran (application) yang masuk ke band-band yang dikelola
     * oleh seorang manager.
     *
     * @param managerId ID manager
     * @return List application dari semua band yang dikelola manager
     * @throws RuntimeException jika manager tidak ditemukan
     */
    @Transactional(readOnly = true)
    public List<Application> getApplicationsForManager(Long managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));

        List<Band> managedBands = manager.getManagedBands();

        // FlatMap untuk menggabungkan semua application dari setiap band
        return managedBands.stream()
                .flatMap(band -> applicationRepository.findByTargetRecruitment_Band(band).stream())
                .toList();
    }
}