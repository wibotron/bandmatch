package com.bandmatch.bandmatch.repository;

import com.bandmatch.bandmatch.domain.user.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
}