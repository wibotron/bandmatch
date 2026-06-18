package com.bandmatch.bandmatch.repository;

import com.bandmatch.bandmatch.domain.band.Band;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BandRepository extends JpaRepository<Band, Long> {
}