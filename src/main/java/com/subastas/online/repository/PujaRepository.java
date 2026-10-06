package com.subastas.online.repository;

import com.subastas.online.domain.Puja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PujaRepository extends JpaRepository<Puja, Long> {
    List<Puja> findBySubastaIdOrderByIdAsc(Long subastaId);
}
