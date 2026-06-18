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

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private BandService bandService;
    @Autowired
    private InteractionService interactionService;

    // Dashboard Manager -> Menampilkan daftar band
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        // Validasi session
        if (managerId == null || !"manager".equals(role)) {
            return "redirect:/login";
        }

        model.addAttribute("bands", bandService.getBandsByManager(managerId));
        model.addAttribute("managerName", session.getAttribute("userName"));
        return "manager-dashboard";
    }

    // Tampilkan form tambah band
    @GetMapping("/band/create")
    public String showCreateForm(Model model) {
        model.addAttribute("band", new Band()); // binding object kosong
        return "band-form";
    }

    // Proses tambah band
    @PostMapping("/band/create")
    public String processCreate(@ModelAttribute("band") Band band,
                                HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        bandService.createBand(band, managerId); // Kirim seluruh objek band
        return "redirect:/manager/dashboard";
    }

    // Tampilkan form edit band
    @GetMapping("/band/edit/{bandId}")
    public String showEditForm(@PathVariable Long bandId,
                               HttpSession session,
                               Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        Band band = bandService.getBandByIdAndManager(bandId, managerId);
        model.addAttribute("band", band);
        return "band-form";
    }

    // Proses update band
    @PostMapping("/band/edit/{bandId}")
    public String processEdit(@PathVariable Long bandId,
                              @ModelAttribute("band") Band updatedBand,
                              HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        Band existingBand = bandService.getBandByIdAndManager(bandId, managerId);

        // Update field-field yang boleh diubah
        existingBand.setName(updatedBand.getName());
        existingBand.setDescription(updatedBand.getDescription());
        existingBand.setLocation(updatedBand.getLocation());
        existingBand.setSpotifyEmbedUrl(updatedBand.getSpotifyEmbedUrl());

        bandService.updateBand(existingBand);
        return "redirect:/manager/dashboard";
    }

    // Proses hapus band
    @GetMapping("/band/delete/{bandId}")
    public String processDelete(@PathVariable Long bandId,
                                HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        bandService.deleteBand(bandId, managerId);
        return "redirect:/manager/dashboard";
    }

    // Menampilkan daftar semua member (musisi) untuk dikirim offer
    @GetMapping("/members")
    public String listMembers(Model model) {
        model.addAttribute("members", interactionService.getAllMembers());
        return "member-list";
    }

    // Tampilkan form kirim offer
    @GetMapping("/offer/send/{memberId}")
    public String showOfferForm(@PathVariable Long memberId, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("offer", new Offer()); // binding object
        return "offer-form";
    }

    // Proses kirim offer
    @PostMapping("/offer/send/{memberId}")
    public String processSendOffer(@PathVariable Long memberId,
                                   @RequestParam String message,
                                   @RequestParam BigDecimal salary,
                                   @RequestParam String expiredDate, // format yyyy-MM-ddTHH:mm
                                   HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        LocalDateTime expiry = LocalDateTime.parse(expiredDate); // ISO format

        interactionService.sendOffer(managerId, memberId, message, salary, expiry);
        return "redirect:/manager/members?success=true";
    }

    // Menampilkan daftar lamaran masuk untuk manager
    @GetMapping("/applications")
    public String viewApplications(HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        model.addAttribute("applications", interactionService.getApplicationsForManager(managerId));
        return "manager-applications";
    }

    // Proses respon terhadap lamaran
    @PostMapping("/application/{appId}/respond")
    public String respondApplication(@PathVariable Long appId,
                                     @RequestParam boolean accepted,
                                     HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        interactionService.respondApplication(appId, managerId, accepted);
        return "redirect:/manager/applications";
    }


    // Tampilkan form buka rekrutmen
    @GetMapping("/band/{bandId}/recruitment/create")
    public String showRecruitmentForm(@PathVariable Long bandId, Model model) {
        model.addAttribute("bandId", bandId);
        model.addAttribute("recruitment", new Recruitment());
        return "recruitment-form";
    }

    // Proses buka rekrutmen
    @PostMapping("/band/{bandId}/recruitment/create")
    public String processOpenRecruitment(@PathVariable Long bandId,
                                         @RequestParam String position,
                                         @RequestParam String requiredInstrument,
                                         @RequestParam String deadline, // Format: yyyy-MM-ddTHH:mm
                                         HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");

        // Validasi kepemilikan band (saya sarankan buat method di Service, tapi kita handle simpel)
        // Ambil band dan cek manager-nya
        Band band = bandService.getBandByIdAndManager(bandId, managerId);

        LocalDateTime deadlineParsed = LocalDateTime.parse(deadline);
        bandService.openRecruitment(bandId, position, requiredInstrument, deadlineParsed);

        return "redirect:/manager/dashboard";
    }

}