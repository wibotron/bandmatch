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

    // ===== MANAGER: KIRIM OFFER =====
    @Transactional
    public Offer sendOffer(Long managerId, Long memberId, String message,
                           BigDecimal salary, LocalDateTime expiredDate) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));
        BandMember member = bandMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan!"));

        Offer offer = new Offer(message, salary, expiredDate, manager, member);

        // PANGGIL METHOD SEND() DARI INTERFACE!
        // Di sini polimorfisme terjadi: offer.send() akan mengecek expiredDate
        offer.send();

        return offerRepository.save(offer);
    }

    // ===== MEMBER: RESPON OFFER =====
    @Transactional
    public Offer respondOffer(Long offerId, Long memberId, boolean accepted) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer tidak ditemukan!"));

        // Validasi: apakah member ini penerima offer?
        if (!offer.getReceiver().getId().equals(memberId)) {
            throw new RuntimeException("Anda tidak berhak merespon offer ini!");
        }

        // PANGGIL METHOD RESPOND() DARI INTERFACE!
        // offer.respond() akan mengecek apakah status sudah EXPIRED
        offer.respond(accepted);

        return offerRepository.save(offer);
    }

    // ===== MEMBER: LAMAR REKRUTMEN (APPLY) =====
    // Ganti method applyRecruitment yang tadi dengan ini:
    @Autowired
    private RecruitmentRepository recruitmentRepository;

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

        // 5. Jika lolos semua validasi, buat lamaran baru
        Application application = new Application(motivationLetter, availabilityNote, member, recruitment);
        application.send(); // ini akan set status menjadi PENDING

        return applicationRepository.save(application);
    }

    // ===== MANAGER: RESPON APPLICATION =====
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

    // ===== AMBIL SEMUA MEMBER (untuk Manager cari musisi) =====
    public List<BandMember> getAllMembers() {
        return bandMemberRepository.findAll();
    }

    // ===== AMBIL SEMUA OFFER UNTUK MEMBER =====
    public List<Offer> getOffersForMember(Long memberId) {
        BandMember member = bandMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan!"));
        return offerRepository.findByReceiver(member);
    }

    // ===== AMBIL SEMUA APPLICATION UNTUK MANAGER =====
    public List<Application> getApplicationsForManager(Long managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan!"));

        // Ambil semua band yang dikelola manager
        List<Band> managedBands = manager.getManagedBands();

        // Kumpulkan semua application dari band-band tersebut
        return managedBands.stream()
                .flatMap(band -> applicationRepository.findByTargetRecruitment_Band(band).stream())
                .toList();
    }
}