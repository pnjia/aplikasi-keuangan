---
description: Eksekusi pengujian otomatis, pembuatan test plan, analisis bug, dan perbaikan (self-healing) menggunakan TestSprite MCP.
---

# Alur Kerja Otomatisasi TestSprite

Alur kerja ini dipicu ketika _User_ menggunakan perintah slash: `/testsprite`.
Ini digunakan untuk menginstruksikan Agen AI memanfaatkan kapabilitas server **TestSprite MCP** secara maksimal untuk menguji aplikasi secara pintar dan otomatis.

### Instruksi untuk Sistem AI Agent:

Ketika _User_ memanggil `/testsprite [target_uji]`, jalankan langkah-langkah berikut secara berurutan dan persisten:

1. **Identifikasi Target Uji:**
   - Jika pengguna memanggil dengan spesifik (contoh: `/testsprite AccountService.java` atau `/testsprite Endpoint Invoice`), fokuskan konteks pada target tersebut.
   - Jika argumen kosong (hanya `/testsprite`), tanyakan secara singkat kepada pengguna bagian mana yang ingin diuji saat ini.

2. **Pahami Konteks & Kepatuhan Proyek:**
   - Gunakan alat bantu baca seperti `view_file` pada modul yang dipilih.
   - Ingat kembali aturan proyek di `backend_rules.md` dan `security.md` (seperti wajib otentikasi JWT, respon harus DTO, _transactional_ yang ketat).

3. **Generasi Test Plan (Rencana Pengujian):**
   - Panggil perangkat/tool dari profil **TestSprite MCP** untuk membaca intensi kode.
   - Minta sistem TestSprite membuatkan skenario uji yang mencakup:
     - _Happy Path_ (Kondisi Ideal).
     - _Negative Cases_ / _Error Handling_ (Kesalahan validasi, format data gagal).
     - _Security / Auth_ (Uji akses JWT Token dan _Role_).
     - _Edge Cases_ (Batasan kritis logika).

4. **Eksekusi Pengujian:**
   - Berikan perintah pada server **TestSprite MCP** untuk mengeksekusi tes.
   - Tunggu hasil dari _run command_ atau pelaporan _cloud_ TestSprite tersebut kembali pada ruang interaksi MCP ke Agen AI.

5. **Diagnosis Laporan (Triase Cerdas):**
   - Analisis umpan balik struktur hasil pengujian.
   - Kelompokkan hasil _Passed_ (Lulus) dan _Failed_ (Gagal).
   - Jika tidak ada yang gagal, langsung beri ringkasan kesuksesan pengujian kepada pengguna beserta buktinya. Loncati ke langkah 7.

6. **Fase Perbaikan Otomatis (_Self-Healing_):**
   - Jika sistem mendeteksi tes yang gagal, secara otonom carilah akar permasalahan kegagalan (_bug_) pada _source code_ Java dalam proyek.
   - Modifikasi kode lokal menggunakan pemanggilan _tools edit code_ (`replace_file_content` / `multi_replace_file_content`).
   - Apabila perbaikan dilakukan, sampaikan secara transparan kepada pengguna apa saja yang baru saja diubah.
   - **WAJIB:** Kembali ke Langkah 4 untuk mengeksekusi ulang status kelulusan (memastikan modifikasi tidak menimbulkan _bug_ baru).

7. **Pelaporan Ringkasan Akhir:**
   - Berikan rangkuman bahasa natural dalam format _Markdown_ dan Tabel berisi total uji, metrik kelulusan, tingkat kepercayaan _(confidence level)_ perbaikan, serta kestabilan endpoint untuk pengguna.
