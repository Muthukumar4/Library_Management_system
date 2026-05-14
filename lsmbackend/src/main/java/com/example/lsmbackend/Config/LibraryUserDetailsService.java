package com.example.lsmbackend.Config;

import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.repository.Staffrepo;
import com.example.lsmbackend.repository.Studerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LibraryUserDetailsService implements UserDetailsService {

    @Autowired
    private Staffrepo staffrepo;

    @Autowired
    private Studerepo studerepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username == null ? "" : username.trim();

        if (normalizedUsername.isEmpty()) {
            throw new UsernameNotFoundException("Username is required");
        }

        if (adminUsername.equalsIgnoreCase(normalizedUsername)) {
            return User.withUsername(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles("ADMIN")
                    .build();
        }

        Optional<Staff> staffOptional = staffrepo.findByStaffCodeOrEmail(normalizedUsername, normalizedUsername);
        if (staffOptional.isPresent()) {
            Staff staff = staffOptional.get();
            return User.withUsername(staff.getStaffCode())
                    .password(staff.getPassword())
                    .roles("STAFF")
                    .disabled(!Boolean.TRUE.equals(staff.getActive()))
                    .build();
        }

        Optional<Student> studentOptional = studerepo.findByRollNumber(normalizedUsername);
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            return User.withUsername(student.getRollNumber())
                    .password(student.getPassword())
                    .roles("STUDENT")
                    .disabled(!Boolean.TRUE.equals(student.getActive()))
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }
}
