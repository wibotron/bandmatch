package com.bandmatch.bandmatch.repository;

import com.bandmatch.bandmatch.domain.band.Band;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BandRepository extends JpaRepository<Band, Long> {

    // Filter berdasarkan genre dan/atau posisi rekrutmen
    @Query("SELECT DISTINCT b FROM Band b " +
            "LEFT JOIN b.genres g " +
            "LEFT JOIN b.recruitments r " +
            "WHERE (:genre IS NULL OR g = :genre) " +
            "AND (:position IS NULL OR r.position = :position)")
    List<Band> findBandsByGenreAndPosition(@Param("genre") String genre,
                                           @Param("position") String position);
}