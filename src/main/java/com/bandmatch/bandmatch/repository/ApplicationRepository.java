package com.bandmatch.bandmatch.repository;

import com.bandmatch.bandmatch.domain.band.Band;
import com.bandmatch.bandmatch.domain.interaction.Application;
import com.bandmatch.bandmatch.domain.user.BandMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByTargetRecruitment_Band(Band band);
    List<Application> findByApplicantAndTargetRecruitment_Band(BandMember applicant, Band band);
}