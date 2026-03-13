package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.ContactRequestDTO;
import com.aplikasi.keuangan.dto.ContactResponseDTO;
import com.aplikasi.keuangan.entity.Contact;
import com.aplikasi.keuangan.repository.ContactRepository;
import com.aplikasi.keuangan.repository.InvoiceRepository;
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
    private final InvoiceRepository invoiceRepository;

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

    @Transactional(readOnly = true)
    public ContactResponseDTO getContactById(UUID id, UUID companyId) {
        Contact contact = contactRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Contact not found or does not belong to this company"));
        return mapToResponse(contact);
    }

    @Transactional
    public void deleteContact(UUID id, UUID companyId) {
        // Validasi bahwa kontak ada dan milik company ini
        Contact contact = contactRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Contact not found or does not belong to this company"));

        // Cek integritas data: apakah sudah ada invoice untuk kontak ini?
        if (invoiceRepository.existsByContactIdAndDeletedAtIsNull(id)) {
            throw new RuntimeException("Kontak tidak dapat dihapus karena sudah memiliki riwayat transaksi/tagihan.");
        }

        // Soft delete: set deletedAt timestamp
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
