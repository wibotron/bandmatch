package com.bandmatch.bandmatch.controller;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.band.Recruitment;
import com.bandmatch.bandmatch.domain.interaction.Offer;
import com.bandmatch.bandmatch.service.BandService;
import com.bandmatch.bandmatch.service.InteractionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller untuk semua fitur yang dapat diakses oleh Manager.
 * Menangani manajemen band, rekrutmen, offer, dan lamaran masuk.
 * Seluruh endpoint berada di bawah prefix "/manager".
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private BandService bandService;

    @Autowired
    private InteractionService interactionService;

    // =========================================================================
    // DASHBOARD
    // =========================================================================

    /**
     * Menampilkan halaman dashboard manager beserta daftar band yang dikelola.
     *
     * @param session session HTTP untuk mengambil data user yang sedang login
     * @param model   model untuk menyimpan atribut tampilan
     * @return nama template Thymeleaf atau redirect ke login jika session tidak valid
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        // Validasi session: pastikan user adalah manager yang sudah login
        if (managerId == null || !"manager".equals(role)) {
            return "redirect:/login";
        }

        model.addAttribute("bands", bandService.getBandsByManager(managerId));
        model.addAttribute("managerName", session.getAttribute("userName"));
        return "manager-dashboard";
    }

    // =========================================================================
    // MANAJEMEN BAND (CRUD)
    // =========================================================================

    /**
     * Menampilkan form untuk membuat band baru.
     *
     * @param model model untuk binding objek Band kosong
     * @return nama template Thymeleaf 'band-form'
     */
    @GetMapping("/band/create")
    public String showCreateForm(Model model) {
        model.addAttribute("band", new Band());
        return "band-form";
    }

    /**
     * Memproses pembuatan band baru dari data form.
     *
     * @param band    objek Band yang terikat dari form (dengan field-field yang diisi)
     * @param session session untuk mengambil ID manager yang sedang login
     * @return redirect ke dashboard manager
     */
    @PostMapping("/band/create")
    public String processCreate(@ModelAttribute("band") Band band,
                                HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        bandService.createBand(band, managerId);
        return "redirect:/manager/dashboard";
    }

    /**
     * Menampilkan form edit band dengan data yang sudah ada.
     *
     * @param bandId  ID band yang akan diedit
     * @param session session untuk mengambil ID manager
     * @param model   model untuk binding data band yang akan diedit
     * @return nama template Thymeleaf 'band-form'
     */
    @GetMapping("/band/edit/{bandId}")
    public String showEditForm(@PathVariable Long bandId,
                               HttpSession session,
                               Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        Band band = bandService.getBandByIdAndManager(bandId, managerId);
        model.addAttribute("band", band);
        return "band-form";
    }

    /**
     * Memproses update data band dari form edit.
     * Field yang diperbarui: nama, deskripsi, genre (multigenre), YouTube, dan Spotify.
     *
     * @param bandId      ID band yang sedang diedit
     * @param updatedBand objek Band yang berisi data baru dari form
     * @param session     session untuk validasi kepemilikan
     * @return redirect ke dashboard manager
     */
    @PostMapping("/band/edit/{bandId}")
    public String processEdit(@PathVariable Long bandId,
                              @ModelAttribute("band") Band updatedBand,
                              HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        Band existingBand = bandService.getBandByIdAndManager(bandId, managerId);

        // Perbarui field-field yang boleh diubah oleh manager
        existingBand.setName(updatedBand.getName());
        existingBand.setDescription(updatedBand.getDescription());
        existingBand.setGenres(updatedBand.getGenres()); // support multigenre
        existingBand.setYoutubeProfile(updatedBand.getYoutubeProfile());
        existingBand.setSpotifyProfile(updatedBand.getSpotifyProfile());

        bandService.updateBand(existingBand);
        return "redirect:/manager/dashboard";
    }

    /**
     * Menghapus band berdasarkan ID, dengan validasi kepemilikan manager.
     *
     * @param bandId  ID band yang akan dihapus
     * @param session session untuk mengambil ID manager
     * @return redirect ke dashboard manager
     */
    @GetMapping("/band/delete/{bandId}")
    public String processDelete(@PathVariable Long bandId,
                                HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        bandService.deleteBand(bandId, managerId);
        return "redirect:/manager/dashboard";
    }

    // =========================================================================
    // MANAJEMEN REKRUTMEN
    // =========================================================================

    /**
     * Menampilkan form untuk membuka rekrutmen baru pada sebuah band.
     *
     * @param bandId ID band tempat rekrutmen akan dibuka
     * @param model  model untuk binding objek Recruitment kosong dan bandId
     * @return nama template Thymeleaf 'recruitment-form'
     */
    @GetMapping("/band/{bandId}/recruitment/create")
    public String showRecruitmentForm(@PathVariable Long bandId, Model model) {
        model.addAttribute("bandId", bandId);
        model.addAttribute("recruitment", new Recruitment());
        return "recruitment-form";
    }

    /**
     * Memproses pembukaan rekrutmen baru.
     *
     * @param bandId             ID band tujuan
     * @param position           posisi yang dibutuhkan (dari dropdown / custom)
     * @param requiredInstrument instrumen spesifik yang dibutuhkan
     * @param deadline           batas akhir pendaftaran (format ISO datetime)
     * @param session            session untuk validasi kepemilikan band
     * @return redirect ke dashboard manager
     */
    @PostMapping("/band/{bandId}/recruitment/create")
    public String processOpenRecruitment(@PathVariable Long bandId,
                                         @RequestParam String position,
                                         @RequestParam String requiredInstrument,
                                         @RequestParam String deadline,
                                         HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        // Validasi kepemilikan band (jika tidak berhak, akan throw exception)
        Band band = bandService.getBandByIdAndManager(bandId, managerId);
        LocalDateTime deadlineParsed = LocalDateTime.parse(deadline);
        bandService.openRecruitment(bandId, position, requiredInstrument, deadlineParsed);
        return "redirect:/manager/dashboard";
    }

    // =========================================================================
    // INTERAKSI DENGAN MEMBER (OFFER)
    // =========================================================================

    /**
     * Menampilkan daftar semua member (musisi) yang terdaftar di sistem,
     * sehingga manager dapat mengirim tawaran (offer) kepada mereka.
     *
     * @param model model untuk menyimpan daftar member
     * @return nama template Thymeleaf 'member-list'
     */
    @GetMapping("/members")
    public String listMembers(Model model) {
        model.addAttribute("members", interactionService.getAllMembers());
        return "member-list";
    }

    /**
     * Menampilkan form untuk mengirim tawaran (offer) kepada seorang member.
     * Manager harus memilih band tujuan dari daftar band yang dikelolanya.
     *
     * @param memberId ID member penerima tawaran
     * @param session  session untuk mengambil ID manager dan daftar band yang dikelola
     * @param model    model untuk binding data offer dan daftar band tujuan
     * @return nama template Thymeleaf 'offer-form'
     */
    @GetMapping("/offer/send/{memberId}")
    public String showOfferForm(@PathVariable Long memberId, HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");

        // Ambil semua band yang dikelola manager ini untuk dipilih sebagai tujuan offer
        List<Band> managedBands = bandService.getBandsByManager(managerId);
        model.addAttribute("managedBands", managedBands);
        model.addAttribute("memberId", memberId);
        model.addAttribute("offer", new Offer());
        return "offer-form";
    }

    /**
     * Memproses pengiriman tawaran (offer) dari manager ke member tertentu.
     * Tawaran mencakup pesan, nominal gaji, deadline, dan band tujuan.
     *
     * @param memberId    ID member penerima
     * @param message     pesan tawaran
     * @param salary      nominal gaji yang ditawarkan
     * @param expiredDate batas waktu tawaran (format ISO datetime)
     * @param bandId      ID band tujuan (dipilih dari dropdown)
     * @param session     session untuk mengambil ID manager pengirim
     * @return redirect ke halaman daftar member dengan parameter sukses
     */
    @PostMapping("/offer/send/{memberId}")
    public String processSendOffer(@PathVariable Long memberId,
                                   @RequestParam String message,
                                   @RequestParam BigDecimal salary,
                                   @RequestParam String expiredDate,
                                   @RequestParam Long bandId,
                                   HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        LocalDateTime expiry = LocalDateTime.parse(expiredDate);
        interactionService.sendOffer(managerId, memberId, bandId, message, salary, expiry);
        return "redirect:/manager/members?success=true";
    }

    // =========================================================================
    // MANAJEMEN LAMARAN MASUK (APPLICATION)
    // =========================================================================

    /**
     * Menampilkan daftar lamaran (application) yang masuk ke band-band yang dikelola manager.
     *
     * @param session session untuk mengambil ID manager
     * @param model   model untuk menyimpan daftar application
     * @return nama template Thymeleaf 'manager-applications'
     */
    @GetMapping("/applications")
    public String viewApplications(HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        model.addAttribute("applications", interactionService.getApplicationsForManager(managerId));
        return "manager-applications";
    }

    /**
     * Memproses respon manager terhadap sebuah lamaran (application).
     * Jika diterima, member akan otomatis ditambahkan ke anggota band dan rekrutmen ditutup.
     * Jika ditolak, status lamaran berubah menjadi REJECTED.
     *
     * @param appId    ID lamaran yang akan direspon
     * @param accepted status keputusan (true = terima, false = tolak)
     * @param session  session untuk validasi kepemilikan band
     * @return redirect ke halaman daftar lamaran
     */
    @PostMapping("/application/{appId}/respond")
    public String respondApplication(@PathVariable Long appId,
                                     @RequestParam boolean accepted,
                                     HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        interactionService.respondApplication(appId, managerId, accepted);
        return "redirect:/manager/applications";
    }
}