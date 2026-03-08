package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.ContactRequestDTO;
import com.aplikasi.keuangan.dto.ContactResponseDTO;
import com.aplikasi.keuangan.entity.Contact;
import com.aplikasi.keuangan.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {
    
    private final ContactRepository contactRepository;

    @Transactional
    public ContactResponseDTO createContact(ContactRequestDTO request) {
        Contact contact = Contact.builder()
                .companyId(request.getCompanyId())
                .type(request.getType())
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        Contact saved = contactRepository.save(contact);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContactResponseDTO> getContactsByCompanyId(UUID companyId) {
        return contactRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteContact(UUID id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        contact.setDeletedAt(Instant.now());
        contactRepository.save(contact);
    }

    private ContactResponseDTO mapToResponse(Contact contact) {
        return ContactResponseDTO.builder()
                .id(contact.getId())
                .companyId(contact.getCompanyId())
                .type(contact.getType())
                .name(contact.getName())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .build();
    }
}
