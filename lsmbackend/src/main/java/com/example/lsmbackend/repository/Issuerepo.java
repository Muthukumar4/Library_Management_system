package com.example.lsmbackend.repository;

import com.example.lsmbackend.model.Issuebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Issuerepo extends JpaRepository<Issuebook, Long> {

    List<Issuebook> findByMemberId(String memberId);

    List<Issuebook> findByStatus(String status);

    Long countByStatus(String status);

    List<Issuebook> findByMemberIdAndMemberType(String memberId, String memberType);
    List<Issuebook> findByBookIdAndStatusIgnoreCase(Long bookId, String status);

}
