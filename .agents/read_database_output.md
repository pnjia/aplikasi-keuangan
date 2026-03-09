# Skill: Membaca Output Database Langsung (Database Reader)

**Tujuan:** Mengotomatiskan pembacaan data dari PostgreSQL menggunakan terminal agar Pilot tidak perlu melakukan _copy-paste_ hasil kueri secara manual.

**Alat yang Diizinkan:** `terminal_execution`

**Aturan & Langkah Eksekusi:**

1. **Gunakan `psql`:** Saat Pilot memintamu mengecek isi pangkalan data, gunakan perintah terminal `psql` dengan flag `-c` untuk mengeksekusi kueri SQL.
2. **Format Perintah:** Konstruksi perintah standar yang harus kamu gunakan adalah:
   `psql -U <username> -d <nama_database> -c "<kueri_sql>"`
   _(Contoh: `psql -U postgres -d akuntansiku_db -c "SELECT _ FROM users LIMIT 5;"`)\*
3. **Minta Kredensial:** Jika kamu belum mengetahui _username_ atau _password_ PostgreSQL di _environment_ lokal Pilot, TANYAKAN terlebih dahulu sebelum mengeksekusi perintah.
4. **Batas Aman (Read-Only):** Skill ini HANYA BOLEH digunakan untuk perintah `SELECT`. Dilarang keras mengeksekusi perintah `INSERT`, `UPDATE`, `DELETE`, `DROP`, atau `TRUNCATE` melalui terminal. Perubahan data hanya boleh terjadi melalui aplikasi Spring Boot.
5. **Analisis Output:** Setelah terminal mengembalikan hasil kueri berwujud tabel teks, baca dan analisis data tersebut untuk menjawab kebutuhan Pilot.
