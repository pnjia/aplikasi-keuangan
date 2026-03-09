---
description: Menjalankan eksekusi pengujian API secara otomatis via curl dan melakukan Code Audit menggunakan persona QA
---

# Workflow Pengujian API & QC Otomatis

Gunakan workflow ini kapan pun pengguna meminta untuk menjalankan pengujian API secara otomatis (misalnya dengan command `/api-testing`). Workflow ini akan mengubah file `.http` menjadi pengujian sekuensial dan melibatkan evaluasi kualitas (QA).

1. **Baca Keterampilan Khusus (Skill)**
   Jalankan alat untuk membaca pedoman skill yang telah dibuat di `.agents/skills/auto_api_tester/SKILL.md` untuk memahami prosedur ekstraksi token, payload, dan peran sebagai QA.

2. **Periksa Kesiapan Server**
   Tanyakan kepada pengguna apakah server Spring Boot sudah berjalan di port 8080. Jika belum berjalan, tawarkan opsi untuk menjalankannya. Jika sudah berjalan, beri konfirmasi untuk memulai pengujian.

3. **Operasikan Pengujian `curl` secara Terurut**
   Gunakan alat eksekusi terminal untuk menjalankan API:
   - Baca `.http` auth, jalankan `curl` POST register & login menggunakan data dummy.
   - Ambil JWT token dari balikan terminal (stdout).
   - Baca `.http` yang lain (misal: company, contact, account) -> jalankan dengan menyisipkan JWT token di Header.
   - Operasikan pencatatan UUID untuk meneruskan ID pada request dependent (misalnya: pembuatan tagihan membutuhkan UUID Akun dan Kontak).

4. **Kumpulkan Status Code dan Output JSON**
   Bandingkan hasil yang dikembalikan oleh API server. Apabila hasilnya 200/201, tandai sebagai lulus pengujian. Jika terdapat _Bad Request_ (400), _Forbidden_ (403), atau _Internal Server Error_ (500), segera simpan catatannya.

5. **Aktivasi Persona QA Agent & Audit**
   Jika terjadi kegagalan sistem, baca struktur kode yang berkaitan dengan request tersebut (Controller, DTO, Service). Periksa dengan kaca mata `.agents/agents/qa_agent.md` dan pastikan keamanan, transaksi ganda (`@Transactional`), serta tipe `BigDecimal` sudah diimplementasikan dengan presisi.

6. **Hasilkan Laporan Artifact**
   Buat file laporan pengujian (`api_test_report.md` di folder artifact) yang memuat tabel titik akhir yang lulus, gagal, dan umpan balik QA untuk pengembang. Laporkan juga perbaikan _on-the-spot_ yang kamu temukan dan laksanakan selama inspeksi.
