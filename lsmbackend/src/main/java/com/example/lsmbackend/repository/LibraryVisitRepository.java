package com.example.lsmbackend.repository;

import com.example.lsmbackend.model.LibraryVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryVisitRepository extends JpaRepository<LibraryVisit, Long> {

    Optional<LibraryVisit> findFirstByMemberIdAndStatusOrderByEntryTimeDesc(String memberId, String status);

    List<LibraryVisit> findAllByOrderByEntryTimeDesc();

    List<LibraryVisit> findByMemberTypeOrderByEntryTimeDesc(String memberType);

    List<LibraryVisit> findByMemberIdOrderByEntryTimeDesc(String memberId);

    long countByStatus(String status);
}
