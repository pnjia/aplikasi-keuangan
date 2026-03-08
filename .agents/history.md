# Riwayat Pengembangan Aplikasi Keuangan

## 8 Maret 2026

### Fase 1: Pemahaman Konteks dan Aturan

- Membaca dan memahami dokumen pedoman arsitektur N-Tier, aturan keamanan mutlak, struktur folder, dan konsep pangkalan data multi-tenant (PostgreSQL, UUID, _Soft Delete_).

### Fase 2: Struktur Dasar & Keamanan Awal

- Pembuatan `BaseEntity` untuk fondasi jejak audit (`createdAt`, `updatedAt`, `deletedAt`, `createdBy`, `updatedBy`) menggunakan `ZonedDateTime` terpusat.
- Pembuatan sistem penanganan eksepsi global (`GlobalExceptionHandler`) dan `ErrorResponseDTO` untuk mengelola error secara generik dan proaktif tanpa membocorkan _stack trace_ (Logika HTTP 500).

### Fase 3: Modul Autentikasi & Multi-Tenant

- Pembuatan entitas dan repositori tabel `User`, `Company`, dan `CompanyRole`.
- Penambahan pustaka `jjwt` di konfigurasi Maven (`pom.xml`).
- Konfigurasi integrasi keamanan Spring Security dengan mematikan CSRF, menetapkan _Session Stateless_, dan sandi _BCrypt_ di `SecurityConfig`.
- Pembuatan utilitas JWT Token (`JwtUtil`).
- Konstruksi struktur _Data Transfer Object_ (DTO) untuk permintaan _login_, registrasi, dan data perusahaan terkait.
- Implementasi fungsional RESTfull API dalam `AuthService`, `AuthController`, `CompanyService`, dan `CompanyController`.

### Fase 4: Modul Master Data (Kontak & Bagan Akun)

- Desain arsitektur `Contact` (Pelanggan/Vendor) dengan enum tipe dinamis.
- Desain arsitektur Bagan Akun _Self-Referencing_ `Account` untuk pembentukan hierarki yang fleksibel.
- Pembuatan DTO dan fungsionalitas CRUD di _Service_ dengan metode penghapusan eksklusif memanipulasi riwayat tanggal `deletedAt` saja (_Soft-Delete_).
- Penambahan filter `findByCompanyIdAndDeletedAtIsNull` otomatis di layer Repositori.
- Implementasi lapis keamanan _Request Context_ di level pengontrol `ContactController` dan `AccountController` untuk memisahkan data akses multi-tenant berdasarkan identitas klaim JWT.
