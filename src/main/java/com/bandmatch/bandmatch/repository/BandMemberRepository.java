package com.bandmatch.bandmatch.repository;

import com.bandmatch.bandmatch.domain.user.BandMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BandMemberRepository extends JpaRepository<BandMember, Long> {
}