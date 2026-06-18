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

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private BandService bandService;
    @Autowired
    private InteractionService interactionService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");
        if (managerId == null || !"manager".equals(role)) {
            return "redirect:/login";
        }
        model.addAttribute("bands", bandService.getBandsByManager(managerId));
        model.addAttribute("managerName", session.getAttribute("userName"));
        return "manager-dashboard";
    }

    @GetMapping("/band/create")
    public String showCreateForm(Model model) {
        model.addAttribute("band", new Band());
        return "band-form";
    }

    @PostMapping("/band/create")
    public String processCreate(@ModelAttribute("band") Band band,
                                HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        bandService.createBand(band, managerId);
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/band/edit/{bandId}")
    public String showEditForm(@PathVariable Long bandId,
                               HttpSession session,
                               Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        Band band = bandService.getBandByIdAndManager(bandId, managerId);
        model.addAttribute("band", band);
        return "band-form";
    }

    // ===== PERBAIKAN DI SINI =====
    @PostMapping("/band/edit/{bandId}")
    public String processEdit(@PathVariable Long bandId,
                              @ModelAttribute("band") Band updatedBand,
                              HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        Band existingBand = bandService.getBandByIdAndManager(bandId, managerId);

        existingBand.setName(updatedBand.getName());
        existingBand.setDescription(updatedBand.getDescription());
        existingBand.setGenres(updatedBand.getGenres());           // <-- PAKAI SETGENRES
        existingBand.setYoutubeProfile(updatedBand.getYoutubeProfile());
        existingBand.setSpotifyProfile(updatedBand.getSpotifyProfile());

        bandService.updateBand(existingBand);
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/band/delete/{bandId}")
    public String processDelete(@PathVariable Long bandId,
                                HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        bandService.deleteBand(bandId, managerId);
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/members")
    public String listMembers(Model model) {
        model.addAttribute("members", interactionService.getAllMembers());
        return "member-list";
    }

    @GetMapping("/offer/send/{memberId}")
    public String showOfferForm(@PathVariable Long memberId, HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");

        // Ambil semua band yang dikelola manager ini
        List<Band> managedBands = bandService.getBandsByManager(managerId);
        model.addAttribute("managedBands", managedBands);
        model.addAttribute("memberId", memberId);
        model.addAttribute("offer", new Offer());
        return "offer-form";
    }

    @PostMapping("/offer/send/{memberId}")
    public String processSendOffer(@PathVariable Long memberId,
                                   @RequestParam String message,
                                   @RequestParam BigDecimal salary,
                                   @RequestParam String expiredDate,
                                   @RequestParam Long bandId, // <-- TAMBAHKAN INI
                                   HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        LocalDateTime expiry = LocalDateTime.parse(expiredDate);
        interactionService.sendOffer(managerId, memberId, bandId, message, salary, expiry);
        return "redirect:/manager/members?success=true";
    }

    @GetMapping("/applications")
    public String viewApplications(HttpSession session, Model model) {
        Long managerId = (Long) session.getAttribute("userId");
        model.addAttribute("applications", interactionService.getApplicationsForManager(managerId));
        return "manager-applications";
    }

    @PostMapping("/application/{appId}/respond")
    public String respondApplication(@PathVariable Long appId,
                                     @RequestParam boolean accepted,
                                     HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        interactionService.respondApplication(appId, managerId, accepted);
        return "redirect:/manager/applications";
    }

    @GetMapping("/band/{bandId}/recruitment/create")
    public String showRecruitmentForm(@PathVariable Long bandId, Model model) {
        model.addAttribute("bandId", bandId);
        model.addAttribute("recruitment", new Recruitment());
        return "recruitment-form";
    }

    @PostMapping("/band/{bandId}/recruitment/create")
    public String processOpenRecruitment(@PathVariable Long bandId,
                                         @RequestParam String position,
                                         @RequestParam String requiredInstrument,
                                         @RequestParam String deadline,
                                         HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        Band band = bandService.getBandByIdAndManager(bandId, managerId);
        LocalDateTime deadlineParsed = LocalDateTime.parse(deadline);
        bandService.openRecruitment(bandId, position, requiredInstrument, deadlineParsed);
        return "redirect:/manager/dashboard";
    }
}