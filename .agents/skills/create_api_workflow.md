# Langkah Standar Membuat API Baru

Setiap kali Pilot meminta pembuatan fitur API baru, ikuti urutan kerja ini:

1. Pembuatan DTO: Buat kelas RequestDTO dan ResponseDTO terlebih dahulu untuk menentukan bentuk data. Tambahkan anotasi validasi (seperti `@NotBlank`, `@NotNull`).
2. Antarmuka Repositori: Buat atau perbarui antarmuka di paket `repository` untuk operasi kueri pangkalan data yang dibutuhkan.
3. Logika Bisnis: Buat antarmuka dan implementasi di paket `service`. Masukkan semua logika perhitungan dan validasi di sini. Terapkan `@Transactional` jika ada manipulasi data.
4. Pengontrol API: Buat kelas di paket `controller`. Hubungkan titik akhir HTTP ke logika layanan. Kembalikan respons menggunakan `ResponseEntity`.
