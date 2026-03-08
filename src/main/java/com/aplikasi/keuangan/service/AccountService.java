package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.AccountRequestDTO;
import com.aplikasi.keuangan.dto.AccountResponseDTO;
import com.aplikasi.keuangan.entity.Account;
import com.aplikasi.keuangan.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponseDTO createAccount(AccountRequestDTO request) {
        Account parentAccount = null;
        if (request.getParentAccountId() != null) {
            parentAccount = accountRepository.findById(request.getParentAccountId())
                    .orElseThrow(() -> new RuntimeException("Parent Account not found"));
        }

        Account account = Account.builder()
                .companyId(request.getCompanyId())
                .parentAccount(parentAccount)
                .accountCode(request.getAccountCode())
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .build();

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccountsByCompanyId(UUID companyId) {
        return accountRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setDeletedAt(Instant.now());
        accountRepository.save(account);
    }

    private AccountResponseDTO mapToResponse(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .companyId(account.getCompanyId())
                .parentAccountId(account.getParentAccount() != null ? account.getParentAccount().getId() : null)
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .build();
    }
}
