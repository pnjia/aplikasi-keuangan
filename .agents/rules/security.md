---
trigger: always_on
---

# Aturan Keamanan Sistem

1. Otentikasi: Gunakan JSON Web Token (JWT) stateless.
2. Kata Sandi: Dilarang menyimpan kata sandi dalam bentuk teks biasa (plaintext). Wajib mengenkripsi dengan BCrypt.
3. Penanganan Kesalahan: Wajib menggunakan `@ControllerAdvice` dan `@ExceptionHandler`. Dilarang mengekspos tumpukan kesalahan (stack trace) ke klien.
4. Otorisasi Rute API: Lindungi titik akhir (endpoint) menggunakan anotasi `@PreAuthorize` berdasarkan peran pengguna (Owner, Admin, atau Kasir).
