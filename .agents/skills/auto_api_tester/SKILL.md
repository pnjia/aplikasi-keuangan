---
name: Auto API Tester & QA Auditor
description: Keterampilan agen untuk mengeksekusi pengujian API secara otomatis berdasarkan file .http dan melakukan audit kualitas menggunakan persona QA Agent.
---

# Auto API Tester & QA Auditor

Skill ini memberikan kemampuan kepada agen AI untuk membaca skenario pengujian API dari file `.http`, mengeksekusinya secara otomatis menggunakan antarmuka terminal (`curl`), dan segera setelahnya bertindak sebagai Quality Assurance (QA) Agent untuk mengevaluasi kualitas kode jika ditemukan masalah pada sistem.

## A. Persiapan Pengujian (Automated Testing)

1. **Identifikasi File**: Temukan semua file dengan ekstensi `.http` di dalam workspace (contoh: `test_auth.http`, `test_company.http`, dll).
2. **Pengecekan Server**: Pastikan server Spring Boot (biasanya di `localhost:8080`) sedang menyala. Jika agen tidak yakin, tanya Pilot/User sebelum memulai eksekusi.

## B. Eksekusi Pengujian Terurut

Agen WAJIB mengekstrak request dari file `.http` dan menerjemahkannya menjadi perintah `curl` yang dieksekusi melalui terminal.  
Aturan urutan eksekusi harus dijaga secara ketat karena ada dependensi antar-data:

1. **Auth (`test_auth.http`)**: Eksekusi Register -> Eksekusi Login -> **EKSTRAK TOKEN JWT** dari response JSON.
2. **Sisipkan Token**: Tanamkan token JWT tersebut ke header `Authorization: Bearer <token>` untuk semua request `curl` selanjutnya.
3. **Eksekusi Sekuensial**: Lanjutkan pengujian secara berurutan ke Master Data (Company, Contact, Account), Transaksi (Invoice), hingga Pelaporan (Reports).
4. **Ekstrak UUID Dinamis**: Setiap kali endpoint POST merespons dengan entitas baru, pastikan agen membaca UUID (`id`) dari response JSON dan memasukkannya ke payload request selanjutnya yang membutuhkan dependensi (misal `contactId` atau `accountId` untuk invoice).
5. **Validasi**: Agen harus memvalidasi balikan _HTTP Status Code_ (201 Created, 200 OK, 400 Bad Request, 403 Forbidden).

## C. Peran QA Agent (Quality Assurance & Code Reviewer)

Setelah pengujian selesai atau apabila ditemukan kode yang bermasalah (misal: 500 Internal Server Error, atau perhitungan uang yang tidak akurat), agen **WAJIB LANGSUNG MENGAMBIL PERAN SEBAGAI QA AGENT** sesuai dengan pedoman yang ada di `.agents/agents/qa_agent.md`.

Sebagai agen QA yang sangat teliti, jalankan inspeksi ketat ini:

1. **Keamanan Lapisan (Otorisasi)**: Periksa Controller terkait. Apakah endpoint sudah dilindungi oleh `@PreAuthorize` yang memadai? Apakah Filter JWT (`JwtAuthenticationFilter`) beroperasi mengamankan rute tersebut?
2. **Integritas Transaksi (ACID)**: Bongkar Service layer. Apakah ada proses yang melibatkan multitable (seperti Invoice + Journal entries) yang melupakan anotasi `@Transactional`?
3. **Presisi Finansial**: Periksa DTO dan Entitas. Apakah tipe data nominal keuangan menggunakan `Double` atau `Float`? (Segera laporkan pelanggaran ini, semua WAJIB menggunakan `BigDecimal`).
4. **Keamanan Penanganan Kesalahan**: Periksa penanganan error (`GlobalExceptionHandler`). Apakah pesan kesalahan memunculkan stack trace mentah ke HTTP response? (Jika iya, perbaiki agar tidak meretas struktur server).

## D. Pelaporan Hasil

Buat laporan dalam bentuk Markdown (Artifact) di akhir yang memuat:

1. Daftar Endpoint API yang lolos pengujian (Passed) beserta response time (estimasi).
2. Daftar Endpoint API yang gagal (Failed) lengkap dengan cURL command dan HTTP status code.
3. **Audit QA**: Ringkasan audit kode berdasarkan pemeriksaan poin C di atas dan rekomendasi untuk pengembang.
