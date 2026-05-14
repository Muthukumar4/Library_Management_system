package com.example.lsmbackend.service;

import com.example.lsmbackend.dto.VisitRecordDto;
import com.example.lsmbackend.dto.VisitScanResponse;
import com.example.lsmbackend.model.LibraryVisit;
import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.repository.LibraryVisitRepository;
import com.example.lsmbackend.repository.Staffrepo;
import com.example.lsmbackend.repository.Studerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class VisitService {

    @Autowired
    private LibraryVisitRepository libraryVisitRepository;

    @Autowired
    private Studerepo studerepo;

    @Autowired
    private Staffrepo staffrepo;

    public VisitScanResponse scanVisit(String scannedId) {
        MemberDetails member = resolveMember(scannedId);
        String memberId = member.memberId();

        LibraryVisit openVisit = libraryVisitRepository
                .findFirstByMemberIdAndStatusOrderByEntryTimeDesc(memberId, "INSIDE")
                .orElse(null);

        if (openVisit == null) {
            LibraryVisit visit = new LibraryVisit();
            visit.setMemberId(memberId);
            visit.setMemberType(member.memberType());
            visit.setMemberName(member.name());
            visit.setDepartment(member.department());
            visit.setVisitDate(LocalDateTime.now().toLocalDate());
            visit.setEntryTime(LocalDateTime.now());
            visit.setStatus("INSIDE");

            LibraryVisit saved = libraryVisitRepository.save(visit);
            return new VisitScanResponse("ENTRY", "Entry recorded successfully", toDto(saved));
        }

        openVisit.setExitTime(LocalDateTime.now());
        openVisit.setStatus("EXITED");
        openVisit.setMemberName(member.name());
        openVisit.setDepartment(member.department());

        LibraryVisit saved = libraryVisitRepository.save(openVisit);
        return new VisitScanResponse("EXIT", "Exit recorded successfully", toDto(saved));
    }

    public List<VisitRecordDto> getAllVisits() {
        return libraryVisitRepository.findAllByOrderByEntryTimeDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<VisitRecordDto> getStudentVisits(String rollNumber) {
        return libraryVisitRepository.findByMemberIdOrderByEntryTimeDesc(normalize(rollNumber))
                .stream()
                .filter(visit -> "STUDENT".equalsIgnoreCase(visit.getMemberType()))
                .map(this::toDto)
                .toList();
    }

    public List<VisitRecordDto> getStaffViewVisits() {
        return libraryVisitRepository.findByMemberTypeOrderByEntryTimeDesc("STUDENT")
                .stream()
                .map(this::toDto)
                .toList();
    }

    public long getInsideCount() {
        return libraryVisitRepository.countByStatus("INSIDE");
    }

    private VisitRecordDto toDto(LibraryVisit visit) {
        return new VisitRecordDto(
                visit.getId(),
                visit.getMemberId(),
                visit.getMemberName(),
                normalizeMemberTypeLabel(visit.getMemberType()),
                visit.getDepartment(),
                visit.getEntryTime(),
                visit.getExitTime(),
                normalizeStatus(visit.getStatus()),
                visit.getVisitDate()
        );
    }

    private MemberDetails resolveMember(String scannedId) {
        String normalized = normalize(scannedId);
        if (normalized.isEmpty()) {
            throw new RuntimeException("Scanned ID is required");
        }

        Student student = studerepo.findByBarcode(normalized)
                .or(() -> studerepo.findByRollNumber(normalized))
                .orElse(null);
        if (student != null) {
            if (Boolean.FALSE.equals(student.getActive())) {
                throw new RuntimeException("Student account is inactive");
            }
            return new MemberDetails(
                    student.getRollNumber(),
                    "STUDENT",
                    safeValue(student.getName(), student.getRollNumber()),
                    safeValue(student.getDepartment(), "-")
            );
        }

        Staff staff = staffrepo.findByBarcode(normalized)
                .or(() -> staffrepo.findByStaffCode(normalized))
                .orElse(null);
        if (staff != null) {
            if (Boolean.FALSE.equals(staff.getActive())) {
                throw new RuntimeException("Staff account is inactive");
            }
            return new MemberDetails(
                    staff.getStaffCode(),
                    "STAFF",
                    safeValue(staff.getName(), staff.getStaffCode()),
                    safeValue(staff.getDepartment(), "-")
            );
        }

        throw new RuntimeException("Member not found for scanned ID");
    }

    private String normalize(String value) {
        return String.valueOf(value == null ? "" : value).trim();
    }

    private String safeValue(String value, String fallback) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? fallback : normalized;
    }

    private String normalizeStatus(String status) {
        return normalize(status).toUpperCase(Locale.ROOT);
    }

    private String normalizeMemberTypeLabel(String memberType) {
        return "STAFF".equalsIgnoreCase(memberType) ? "Staff" : "Student";
    }

    private record MemberDetails(String memberId, String memberType, String name, String department) {
    }
}
