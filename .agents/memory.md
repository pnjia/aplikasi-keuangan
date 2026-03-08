# Keputusan Arsitektur Tetap

- Pangkalan Data: PostgreSQL.
- Kunci Utama (Primary Key): Wajib menggunakan UUID.
- Presisi Uang: Wajib menggunakan `NUMERIC(19, 4)` di PostgreSQL dan `BigDecimal` di Java untuk mencegah kesalahan pembulatan angka.
- Jejak Audit (Audit Trail): Setiap entitas wajib memiliki `created_at`, `updated_at`, `deleted_at`, `created_by`, dan `updated_by`.
- Kebijakan Penghapusan: Hanya menggunakan Soft Delete. Dilarang menggunakan perintah `DELETE FROM` di repositori.
