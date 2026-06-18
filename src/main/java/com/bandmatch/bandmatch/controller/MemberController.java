package com.bandmatch.bandmatch.controller;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.portfolio.Portfolio;
import com.bandmatch.bandmatch.domain.user.BandMember;
import com.bandmatch.bandmatch.service.BandService;
import com.bandmatch.bandmatch.service.InteractionService;
import com.bandmatch.bandmatch.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk semua fitur yang dapat diakses oleh BandMember.
 * Menangani manajemen portofolio, eksplorasi band, dan interaksi (offer/application).
 * Seluruh endpoint berada di bawah prefix "/member".
 */
@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private BandService bandService;

    @Autowired
    private InteractionService interactionService;

    // =========================================================================
    // DASHBOARD
    // =========================================================================

    /**
     * Menampilkan halaman dashboard member beserta profil dan portofolio.
     *
     * @param session session HTTP untuk mengambil data user yang sedang login
     * @param model   model untuk menyimpan atribut tampilan
     * @return nama template Thymeleaf atau redirect ke login jika session tidak valid
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        // Validasi session: pastikan user adalah member yang sudah login
        if (memberId == null || !"member".equals(role)) {
            return "redirect:/login";
        }

        BandMember member = memberService.getMemberById(memberId);
        model.addAttribute("member", member);
        model.addAttribute("portfolio", member.getPortfolio());
        model.addAttribute("memberName", session.getAttribute("userName"));
        return "member-dashboard";
    }

    // =========================================================================
    // MANAJEMEN PORTOFOLIO
    // =========================================================================

    /**
     * Menampilkan form untuk mengedit portofolio member.
     *
     * @param session session untuk mengambil ID member yang sedang login
     * @param model   model untuk binding data portofolio
     * @return nama template Thymeleaf 'portfolio-form'
     */
    @GetMapping("/portfolio/edit")
    public String showEditPortfolio(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("userId");
        BandMember member = memberService.getMemberById(memberId);

        // Kirim portfolio ke form untuk binding
        model.addAttribute("portfolio", member.getPortfolio());
        return "portfolio-form";
    }

    /**
     * Memproses update data portofolio dari form.
     *
     * @param updatedPortfolio objek Portfolio yang berisi data baru dari form
     * @param session          session untuk mengambil ID member
     * @return redirect ke dashboard member
     */
    @PostMapping("/portfolio/edit")
    public String processEditPortfolio(@ModelAttribute("portfolio") Portfolio updatedPortfolio,
                                       HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        memberService.updatePortfolio(memberId, updatedPortfolio);
        return "redirect:/member/dashboard";
    }

    // =========================================================================
    // EKSPLORASI BAND & DETAIL BAND
    // =========================================================================

    /**
     * Menampilkan halaman eksplorasi band dengan fitur filter.
     * Filter dapat berdasarkan genre (multigenre) dan/atau posisi rekrutmen.
     * Genre yang tersedia diambil secara dinamis dari semua band yang terdaftar.
     *
     * @param genre    filter berdasarkan genre (opsional)
     * @param position filter berdasarkan posisi yang dibutuhkan (opsional)
     * @param model    model untuk menyimpan hasil filter dan opsi filter
     * @return nama template Thymeleaf 'explore-bands'
     */
    @GetMapping("/explore")
    public String exploreBands(@RequestParam(required = false) String genre,
                               @RequestParam(required = false) String position,
                               Model model) {
        // Cari band berdasarkan filter (genre dan/atau position)
        List<Band> bands = bandService.searchBands(genre, position);
        model.addAttribute("bands", bands);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("selectedPosition", position);

        // Ambil daftar genre unik dari semua band (menggunakan flatMap karena genres adalah List)
        List<String> genres = bandService.getAllBands().stream()
                .flatMap(band -> band.getGenres().stream())
                .filter(g -> g != null && !g.isEmpty())
                .distinct()
                .sorted()
                .toList();
        model.addAttribute("genres", genres);

        // Daftar posisi untuk dropdown filter (harus sinkron dengan recruitment-form)
        List<String> positions = List.of(
                "Lead Gitar", "Rhythm Gitar", "Bass Gitar", "Drummer",
                "Gitar Akustik", "Vokalis", "Vokalis 2", "Vokalis 3"
        );
        model.addAttribute("positions", positions);

        return "explore-bands";
    }

    /**
     * Menampilkan detail sebuah band beserta daftar rekrutmen aktif.
     * Member juga dapat melihat anggota band saat ini.
     *
     * @param bandId ID band yang akan ditampilkan
     * @param model  model untuk menyimpan data band dan rekrutmen
     * @return nama template Thymeleaf 'band-detail'
     */
    @GetMapping("/band/{bandId}")
    public String bandDetail(@PathVariable Long bandId, Model model) {
        Band band = memberService.getBandDetail(bandId);
        model.addAttribute("band", band);
        model.addAttribute("recruitments", band.getRecruitments());
        return "band-detail";
    }

    // =========================================================================
    // INTERAKSI DENGAN OFFER (TAWARAN MASUK)
    // =========================================================================

    /**
     * Menampilkan daftar tawaran (offer) yang masuk untuk member yang sedang login.
     *
     * @param session session untuk mengambil ID member
     * @param model   model untuk menyimpan daftar offer
     * @return nama template Thymeleaf 'member-offers'
     */
    @GetMapping("/offers")
    public String viewOffers(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("userId");
        model.addAttribute("offers", interactionService.getOffersForMember(memberId));
        return "member-offers";
    }

    /**
     * Memproses respon member terhadap sebuah tawaran (offer).
     * Jika diterima, member akan otomatis ditambahkan ke band tujuan.
     * Jika ditolak, status offer berubah menjadi REJECTED.
     *
     * @param offerId  ID offer yang akan direspon
     * @param accepted status keputusan (true = terima, false = tolak)
     * @param session  session untuk validasi kepemilikan offer
     * @return redirect ke halaman daftar offer
     */
    @PostMapping("/offer/{offerId}/respond")
    public String respondOffer(@PathVariable Long offerId,
                               @RequestParam boolean accepted,
                               HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        interactionService.respondOffer(offerId, memberId, accepted);
        return "redirect:/member/offers";
    }

    // =========================================================================
    // INTERAKSI DENGAN REKRUTMEN (APPLICATION / LAMARAN)
    // =========================================================================

    /**
     * Memproses pengajuan lamaran (application) oleh member ke sebuah rekrutmen.
     * Validasi dilakukan di service layer:
     * - Rekrutmen masih terbuka (status OPEN dan belum melewati deadline)
     * - Member belum menjadi anggota band tersebut
     * - Member belum memiliki lamaran aktif (PENDING/ACCEPTED) di band yang sama
     *
     * @param bandId            ID band tujuan (untuk redirect balik)
     * @param recruitmentId     ID rekrutmen yang dilamar
     * @param motivationLetter  surat motivasi dari member
     * @param availabilityNote  catatan ketersediaan member
     * @param session           session untuk mengambil ID member
     * @return redirect ke halaman detail band dengan parameter sukses
     */
    @PostMapping("/band/{bandId}/apply/{recruitmentId}")
    public String applyRecruitment(@PathVariable Long bandId,
                                   @PathVariable Long recruitmentId,
                                   @RequestParam String motivationLetter,
                                   @RequestParam String availabilityNote,
                                   HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        interactionService.applyRecruitment(memberId, recruitmentId, motivationLetter, availabilityNote);
        return "redirect:/member/band/" + bandId + "?applied=true";
    }
}