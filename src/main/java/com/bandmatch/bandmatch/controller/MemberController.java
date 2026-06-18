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

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private BandService bandService;
    @Autowired
    private InteractionService interactionService;

    // Dashboard Member -> Menampilkan profil & portofolio
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (memberId == null || !"member".equals(role)) {
            return "redirect:/login";
        }

        BandMember member = memberService.getMemberById(memberId);
        model.addAttribute("member", member);
        model.addAttribute("portfolio", member.getPortfolio());
        model.addAttribute("memberName", session.getAttribute("userName"));
        return "member-dashboard";
    }

    // Tampilkan form edit portofolio
    @GetMapping("/portfolio/edit")
    public String showEditPortfolio(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("userId");
        BandMember member = memberService.getMemberById(memberId);

        // Kirim portfolio ke form (binding)
        model.addAttribute("portfolio", member.getPortfolio());
        return "portfolio-form";
    }

    // Proses update portofolio
    @PostMapping("/portfolio/edit")
    public String processEditPortfolio(@ModelAttribute("portfolio") Portfolio updatedPortfolio,
                                       HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        memberService.updatePortfolio(memberId, updatedPortfolio);
        return "redirect:/member/dashboard";
    }

    // HALAMAN EKSPLORASI BAND
    @GetMapping("/explore")
    public String exploreBands(@RequestParam(required = false) String genre,
                               @RequestParam(required = false) String position,
                               Model model) {
        List<Band> bands = bandService.searchBands(genre, position);
        model.addAttribute("bands", bands);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("selectedPosition", position);

        // ===== PERBAIKAN: ambil genre dari LIST (flatMap) =====
        List<String> genres = bandService.getAllBands().stream()
                .flatMap(band -> band.getGenres().stream())  // <-- pakai flatMap
                .filter(g -> g != null && !g.isEmpty())
                .distinct()
                .sorted()
                .toList();
        model.addAttribute("genres", genres);

        List<String> positions = List.of("Lead Gitar", "Rhythm Gitar", "Bass Gitar", "Drummer",
                "Gitar Akustik", "Vokalis", "Vokalis 2", "Vokalis 3");
        model.addAttribute("positions", positions);

        return "explore-bands";
    }

    // Detail band (nanti untuk lihat rekrutmen & apply)
    @GetMapping("/band/{bandId}")
    public String bandDetail(@PathVariable Long bandId, Model model) {
        Band band = memberService.getBandDetail(bandId);
        model.addAttribute("band", band);
        model.addAttribute("recruitments", band.getRecruitments()); // rekrutmen terbuka
        return "band-detail";
    }

    // Menampilkan daftar offer masuk untuk member
    @GetMapping("/offers")
    public String viewOffers(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("userId");
        model.addAttribute("offers", interactionService.getOffersForMember(memberId));
        return "member-offers";
    }

    // Proses respon terhadap offer
    @PostMapping("/offer/{offerId}/respond")
    public String respondOffer(@PathVariable Long offerId,
                               @RequestParam boolean accepted,
                               HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        interactionService.respondOffer(offerId, memberId, accepted);
        return "redirect:/member/offers";
    }

    // Proses apply ke rekrutmen (dari halaman detail band)
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