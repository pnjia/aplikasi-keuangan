package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.ContactRequestDTO;
import com.aplikasi.keuangan.dto.ContactResponseDTO;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import com.aplikasi.keuangan.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final UserRepository userRepository;
    private final CompanyRoleRepository companyRoleRepository;

    private UUID getCompanyIdFromCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CompanyRole> roles = companyRoleRepository.findByUserId(user.getId());
        if (roles.isEmpty()) {
            throw new RuntimeException("User does not belong to any company");
        }
        return roles.get(0).getCompanyId();
    }

    @PostMapping
    public ResponseEntity<ContactResponseDTO> createContact(@RequestBody ContactRequestDTO request) {
        request.setCompanyId(getCompanyIdFromCurrentUser());
        return new ResponseEntity<>(contactService.createContact(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ContactResponseDTO>> getContacts() {
        UUID companyId = getCompanyIdFromCurrentUser();
        return ResponseEntity.ok(contactService.getContactsByCompanyId(companyId));
    }
}
