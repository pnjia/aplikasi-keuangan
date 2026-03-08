---
trigger: always_on
---

# Standar Struktur Folder (Package) Spring Boot

Kamu wajib meletakkan setiap kelas Java ke dalam direktori (package) yang tepat sesuai dengan arsitektur N-Tier berikut:

1. `controller`: Bertugas menerima request HTTP dari frontend, memanggil service, dan mengembalikan response (JSON).
2. `service`: Jantung dari aplikasi kita. Semua logika bisnis keuangan (seperti validasi saldo, kalkulasi pajak, pembuatan jurnal double entry) berada di sini.
3. `repository`: Tempat antarmuka (interface) Spring Data JPA untuk berkomunikasi langsung dengan database.
4. `entity`: Kumpulan class Java yang merepresentasikan tabel database kita.
5. `dto` (Data Transfer Object): Class khusus untuk membawa data masuk dan keluar. **Aturan Keamanan:** Jangan pernah mengekspos entity database langsung ke client. Gunakan DTO untuk menyembunyikan data sensitif.
6. `config`: Tempat konfigurasi aplikasi (misalnya pengaturan Spring Security, JWT Auth, dan CORS).
7. `exception`: Tempat kita menangani error secara global (Global Exception Handler) agar response error selalu seragam dan tidak membocorkan struktur stack trace server.

# Standar Penulisan Kode

1. Isolasi Entitas: Sesuai aturan direktori `dto` di atas, dilarang mengembalikan objek `Entity` langsung ke HTTP Response.
2. Anotasi Transaksional: Setiap metode di dalam paket `service` yang mengubah lebih dari satu tabel (contoh: pemrosesan tagihan dan pembuatan jurnal) wajib menggunakan anotasi `@Transactional`.
3. Pustaka Pihak Ketiga: Gunakan Lombok (`@Getter`, `@Setter`, `@Builder`) untuk meminimalkan kode repetitif.
4. Kueri Database: Utamakan Spring Data JPA (Derived Query Methods) di dalam direktori `repository`. Jika sangat kompleks, gunakan spesifikasi JPA (JPA Specification) atau kueri asli (Native Query) yang terstruktur.
