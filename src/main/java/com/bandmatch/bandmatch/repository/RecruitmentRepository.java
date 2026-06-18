package com.bandmatch.bandmatch.repository;

import com.bandmatch.bandmatch.domain.band.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
}