package com.bandmatch.bandmatch.service;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.portfolio.Portfolio;
import com.bandmatch.bandmatch.domain.user.BandMember;
import com.bandmatch.bandmatch.repository.BandMemberRepository;
import com.bandmatch.bandmatch.repository.BandRepository;
import com.bandmatch.bandmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BandMemberRepository bandMemberRepository;

    @Autowired
    private BandRepository bandRepository;

    // Ambil data member berdasarkan ID
    public BandMember getMemberById(Long memberId) {
        return (BandMember) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan!"));
    }

    // Update portofolio (Komposisi: update langsung ke objek Portfolio yang sudah ada)
    public BandMember updatePortfolio(Long memberId, Portfolio updatedPortfolio) {
        BandMember member = getMemberById(memberId);
        Portfolio existingPortfolio = member.getPortfolio();

        // Update field-field portofolio
        existingPortfolio.setBio(updatedPortfolio.getBio());
        existingPortfolio.setSkillSet(updatedPortfolio.getSkillSet());
        existingPortfolio.setYoutubeChannel(updatedPortfolio.getYoutubeChannel());
        existingPortfolio.setInstagramHandle(updatedPortfolio.getInstagramHandle());

        // Karena @OneToOne dengan cascade ALL, save member otomatis save portfolio
        return bandMemberRepository.save(member);
    }

    // Ambil semua band untuk eksplorasi (tanpa batasan manager)
    public List<Band> getAllBands() {
        return bandRepository.findAll();
    }

    // Ambil detail band by ID (untuk nanti lihat rekrutmen)
    public Band getBandDetail(Long bandId) {
        return bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band tidak ditemukan!"));
    }

}