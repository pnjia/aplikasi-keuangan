# Spesifikasi Mutlak API dan Relasi Pangkalan Data

Kamu wajib mematuhi rancangan titik akhir (endpoint) di bawah ini. Dilarang keras membuat titik akhir baru yang tidak ada dalam daftar ini kecuali atas perintah langsung dari Pilot.

## A. Autentikasi & Pengguna

- `POST /api/v1/auth/register` (Mendaftarkan pengguna baru)
- `POST /api/v1/auth/login` (Masuk pengguna, mengembalikan token JWT)
- `POST /api/v1/auth/logout` (Keluar pengguna, membersihkan memori autentikasi)
  - **Header Wajib**: `Authorization: Bearer <Token_JWT>`
  - **Sukses Respon (200 OK)**:
    ```json
    {
      "timestamp": "2024-03-11T00:00:00Z",
      "status": 200,
      "message": "Logout berhasil"
    }
    ```

## B. Manajemen Perusahaan (Tenant)

- `POST /api/v1/companies` (Membuat profil perusahaan baru)
- `GET /api/v1/companies` (Melihat daftar perusahaan tempat pengguna tersebut memiliki akses)

## C. Manajemen Transaksi (Invoice)

- `POST /api/v1/invoices` (Membuat tagihan baru beserta item di dalamnya)
- `GET /api/v1/invoices` (Mengambil daftar tagihan per perusahaan, wajib menggunakan paginasi)
- `PUT /api/v1/invoices/{id}/pay` (Memproses pembayaran tagihan)
  - **ATURAN KRUSIAL:** Titik akhir ini tidak hanya mengubah status tagihan. Fungsi ini wajib memicu pembuatan jurnal secara otomatis (menambah baris ke tabel `journal_entries` dan `journal_lines`). Wajib gunakan `@Transactional`.

## D. Modul Pelaporan (Reports API)

Semua titik akhir di bawah ini wajib menggunakan filter parameter waktu (contoh parameter: `?startDate=2024-01-01&endDate=2024-01-31`).

- `GET /api/v1/reports/general-journal` (Mengambil Jurnal Umum)
- `GET /api/v1/reports/general-ledger/{accountId}` (Mengambil Buku Besar untuk satu akun tertentu)
- `GET /api/v1/reports/trial-balance` (Mengambil Neraca Saldo)
- `GET /api/v1/reports/profit-and-loss` (Mengambil ringkasan Laba Rugi)
- `GET /api/v1/reports/balance-sheet` (Mengambil struktur Neraca)
- `GET /api/v1/reports/cash-flow` (Mengambil Arus Kas, memfilter pergerakan khusus pada akun Kas atau Bank)

## E. Dashboard Aggregation API

- `GET /api/v1/dashboard/summary` (Menyediakan data agregasi untuk halaman dashboard seperti total saldo kas, pendapatan bulanan, pengeluaran bulanan, laba bersih, serta grafik trend pendapatan & pengeluaran bulanan dan expense by category. CompanyId didapatkan dari JWT token pengguna yang login)
