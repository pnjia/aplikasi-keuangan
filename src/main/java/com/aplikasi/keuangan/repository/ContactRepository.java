package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.Contact;
import com.aplikasi.keuangan.entity.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {
    List<Contact> findByCompanyIdAndDeletedAtIsNull(UUID companyId);
    List<Contact> findByCompanyIdAndTypeAndDeletedAtIsNull(UUID companyId, ContactType type);
    Optional<Contact> findByIdAndCompanyId(UUID id, UUID companyId);
}
