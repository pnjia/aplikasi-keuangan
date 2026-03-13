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

- `GET /api/v1/companies/team` (Mengambil daftar anggota tim/karyawan dalam perusahaan - MANAJEMEN TIM)
  - **Akses**: OWNER, ADMIN, KASIR
  - **Security**: CompanyId diambil dari JWT - pengguna hanya bisa melihat tim dari perusahaan mereka sendiri
  - **Response (200 OK)**: Array dari TeamMemberDTO
    ```json
    [
      {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "fullName": "Budi Santoso",
        "email": "budi@perusahaan.com",
        "roleName": "ADMIN",
        "isActive": true
      },
      {
        "userId": "550e8400-e29b-41d4-a716-446655440001",
        "fullName": "Siti Nurhaliza",
        "email": "siti@perusahaan.com",
        "roleName": "KASIR",
        "isActive": false
      }
    ]
    ```
  - **Catatan**: Endpoint ini menggunakan custom JPQL @Query untuk melakukan JOIN antara users dan company_roles, mengembalikan data dalam bentuk DTO yang sudah diproyeksikan. Field `isActive` menunjukkan status aktif (true) atau nonaktif (false) dari anggota tim.

- `POST /api/v1/companies/team` (Tambah anggota tim baru ke perusahaan aktif)
  - **Akses**: OWNER, ADMIN
  - **Security**: CompanyId diambil dari JWT - user hanya dapat menambahkan anggota ke perusahaan aktif miliknya
  - **Request Body** (AddMemberRequest):
    ```json
    {
      "fullName": "Andi Pratama",
      "email": "andi@perusahaan.com",
      "password": "Andi@12345",
      "roleName": "KASIR"
    }
    ```
  - **Alur Bisnis**:
    1. Cek user berdasarkan email
    2. Jika belum ada, buat user baru dan simpan password ter-encode
    3. Jika sudah ada, gunakan user existing
    4. Validasi user belum menjadi anggota tim aktif perusahaan ini
    5. Simpan relasi `company_roles` dengan role yang dipilih
  - **Response Sukses (201 Created)**:
    ```json
    {
      "message": "Anggota tim berhasil ditambahkan"
    }
    ```
  - **Response Gagal - User Sudah Tergabung (400 Bad Request)**:
    ```json
    {
      "error": "User sudah menjadi bagian dari tim ini."
    }
    ```
  - **Response Gagal - Role Tidak Valid (400 Bad Request)**:
    ```json
    {
      "error": "Invalid role: MASTER. Must be one of: OWNER, ADMIN, KASIR"
    }
    ```

- `PUT /api/v1/companies/team/{userId}` (Edit Anggota Tim - Ubah Peran & Status Aktif - MANAJEMEN TIM LANJUTAN)
  - **Akses**: OWNER, ADMIN (hanya pemilik atau admin yang bisa mengedit anggota)
  - **Security**: CompanyId diambil dari JWT - pengguna hanya bisa mengedit anggota dari perusahaan mereka sendiri
  - **Request Body** (UpdateTeamMemberRequestDTO):
    ```json
    {
      "roleName": "ADMIN",
      "isActive": true
    }
    ```
  - **Response Sukses (200 OK)**:
    ```json
    {
      "message": "Team member updated successfully"
    }
    ```
  - **Response Gagal - Peran Tidak Valid (400 Bad Request)**:
    ```json
    {
      "error": "Invalid role: MASTER. Must be one of: OWNER, ADMIN, KASIR"
    }
    ```
  - **Response Gagal - Anggota Tidak Ditemukan (400 Bad Request)**:
    ```json
    {
      "error": "Team member not found in this company"
    }
    ```
  - **Response Gagal - Tidak Bisa Menurunkan Satu-satunya Pemilik (400 Bad Request)**:
    ```json
    {
      "error": "Cannot remove the only owner from the company"
    }
    ```
  - **Response Gagal - Tidak Bisa Menonaktifkan Satu-satunya Pemilik (400 Bad Request)**:
    ```json
    {
      "error": "Cannot deactivate the only owner in the company"
    }
    ```
  - **Catatan**: Endpoint yang disederhanakan menggabungkan perubahan role (peran) dan status aktif (isActive) dalam satu modal edit di frontend. Validasi mencegah:
    1. Menurunkan OWNER jika dia adalah satu-satunya OWNER
    2. Menonaktifkan (isActive = false) OWNER jika dia adalah satu-satunya OWNER
       Update menggunakan @Modifying @Query untuk efisiensi transaksi atomik.

## C. Manajemen Kontak (Contacts)

- `POST /api/v1/contacts` (Membuat kontak baru - CUSTOMER atau VENDOR)
  - **Akses**: OWNER, ADMIN, KASIR
  - **Request Body**:
    ```json
    {
      "type": "CUSTOMER",
      "name": "CV Pelanggan Setia",
      "phone": "081234567890",
      "email": "pelanggan@setia.com"
    }
    ```
  - **Response (201 Created)**:
    ```json
    {
      "id": "5aac0ee6-8457-4848-a813-d8488540fc7d",
      "companyId": "company-uuid",
      "type": "CUSTOMER",
      "name": "CV Pelanggan Setia",
      "phone": "081234567890",
      "email": "pelanggan@setia.com"
    }
    ```

- `GET /api/v1/contacts` (Mengambil daftar kontak per perusahaan)
  - **Akses**: OWNER, ADMIN, KASIR
  - **Response (200 OK)**: Array dari ContactResponseDTO

- `GET /api/v1/contacts/{id}` (Mengambil detail kontak spesifik - UNTUK CONTACT DETAIL DRAWER)
  - **Akses**: OWNER, ADMIN, KASIR
  - **Security**: CompanyId divalidasi dari JWT - pengguna hanya bisa melihat kontak milik perusahaan mereka
  - **Response (200 OK)**:
    ```json
    {
      "id": "5aac0ee6-8457-4848-a813-d8488540fc7d",
      "companyId": "company-uuid",
      "type": "CUSTOMER",
      "name": "CV Pelanggan Setia",
      "phone": "081234567890",
      "email": "pelanggan@setia.com"
    }
    ```

- `DELETE /api/v1/contacts/{id}` (Menghapus kontak dengan validasi integritas data)
  - **Akses**: OWNER, ADMIN, KASIR
  - **Security**: CompanyId divalidasi dari JWT - pengguna hanya bisa menghapus kontak milik perusahaan mereka
  - **Validasi Data**: Penghapusan akan DITOLAK jika kontak sudah memiliki riwayat transaksi/tagihan (invoice yang tidak dihapus)
  - **Respons Sukses (200 OK)**:
    ```json
    {
      "message": "Kontak berhasil dihapus",
      "status": "success"
    }
    ```
  - **Respons Gagal - Kontak Tidak Ditemukan (404 Not Found)**:
    ```json
    {
      "error": "Contact not found or does not belong to this company"
    }
    ```
  - **Respons Gagal - Ada Transaksi (400 Bad Request)**:
    ```json
    {
      "error": "Kontak tidak dapat dihapus karena sudah memiliki riwayat transaksi/tagihan."
    }
    ```
  - **Catatan**: Soft delete - data kontak tidak benar-benar dihapus dari database, hanya di-mark dengan `deletedAt` timestamp

## D. Manajemen Akun (Chart of Accounts / COA)

- `POST /api/v1/accounts` (Membuat akun baru dalam bagan akun perusahaan)
  - **Akses**: OWNER, ADMIN
  - **Request Body**:
    ```json
    {
      "accountCode": "1-1000",
      "accountName": "Kas",
      "accountType": "ASSET",
      "parentAccountId": null
    }
    ```
  - **Response (201 Created)**: AccountResponseDTO dengan field id, companyId, accountCode, accountName, accountType

- `GET /api/v1/accounts` (Mengambil daftar akun per perusahaan)
  - **Akses**: OWNER, ADMIN, KASIR
  - **Response (200 OK)**: Array dari AccountResponseDTO
  - **Security**: Hanya menampilkan akun milik perusahaan user (filtered by companyId dari JWT)

- `DELETE /api/v1/accounts/{id}` (Menghapus akun dengan validasi integritas data keuangan - PEMBARUAN)
  - **Akses**: OWNER, ADMIN
  - **Security**: Validasi companyId dari JWT - pengguna hanya bisa menghapus akun milik perusahaan mereka sendiri
  - **VALIDASI KRITIS** (Data Integrity):
    1. Sistem melakukan cek ke `journal_lines` table
    2. Jika akun tersebut sudah memiliki riwayat transaksi (ada baris jurnal yang menggunakan akun ini), penghapusan akan DITOLAK
    3. Jika akun sudah bersih (tidak ada history), akan dihapus secara permanen dari database
  - **Response Sukses (200 OK)**:
    ```json
    {
      "message": "Akun berhasil dihapus"
    }
    ```
  - **Response Gagal - Akun Tidak Ditemukan (400 Bad Request)**:
    ```json
    {
      "error": "Account not found"
    }
    ```
  - **Response Gagal - Akun Bukan Milik Perusahaan (400 Bad Request)**:
    ```json
    {
      "error": "Account does not belong to this company"
    }
    ```
  - **Response Gagal - Akun Memiliki Riwayat Transaksi (400 Bad Request)** - KRUSIAL:
    ```json
    {
      "error": "Akun tidak dapat dihapus karena sudah memiliki riwayat transaksi. Silakan nonaktifkan akun ini jika sudah tidak digunakan."
    }
    ```
  - **Catatan**: Penghapusan bersifat permanen (hard delete) hanya jika akun benar-benar bersih. Ini berbeda dengan soft-delete yang digunakan untuk kontak/tim.

- `PATCH /api/v1/accounts/{id}/status` (Ubah Status Aktif/Nonaktif Akun - Soft-Disable - ALTERNATIF PENGHAPUSAN)
  - **Akses**: OWNER, ADMIN
  - **Security**: Validasi companyId dari JWT
  - **Purpose**: Solusi bagi akun yang sudah memiliki transaksi tapi ingin "dihilangkan" dari daftar pilihan input (dropdown di frontend)
  - **Request Body**:
    ```json
    {
      "isActive": false
    }
    ```
  - **Response Sukses (200 OK)**:
    ```json
    {
      "message": "Akun dinonaktifkan"
    }
    ```
    atau
    ```json
    {
      "message": "Akun diaktifkan"
    }
    ```
  - **Response Gagal (400 Bad Request)**:
    ```json
    {
      "error": "Field 'isActive' is required"
    }
    ```
  - **Catatan**:
    - Akun nonaktif (isActive = false) tidak akan muncul di dropdown input, tapi data historisnya tetap ada di laporan
    - Default value untuk akun baru: `isActive = true`
    - Gunakan endpoint ini jika akun sudah punya transaksi dan tidak bisa dihapus, tapi ingin disembunyikan dari user input

## E. Manajemen Transaksi (Invoice)

- `POST /api/v1/invoices` (Membuat tagihan baru beserta item di dalamnya)
- `GET /api/v1/invoices` (Mengambil daftar tagihan per perusahaan, wajib menggunakan paginasi)
  - **Query Parameters**:
    - `page` (default: 0) - Nomor halaman paginasi
    - `size` (default: 10) - Jumlah item per halaman
    - `contactId` (optional) - Filter tagihan berdasarkan kontak tertentu
  - **Contoh**: `/api/v1/invoices?page=0&size=10&contactId=5aac0ee6-8457-4848-a813-d8488540fc7d`
  - **Response (200 OK)**: Page<InvoiceResponseDTO> dengan filtering berdasarkan contactId jika disediakan
- `PUT /api/v1/invoices/{id}/pay` (Memproses pembayaran tagihan)
  - **ATURAN KRUSIAL:** Titik akhir ini tidak hanya mengubah status tagihan. Fungsi ini wajib memicu pembuatan jurnal secara otomatis (menambah baris ke tabel `journal_entries` dan `journal_lines`). Wajib gunakan `@Transactional`.

## E. Modul Pelaporan (Reports API)

Semua titik akhir di bawah ini wajib menggunakan filter parameter waktu (contoh parameter: `?startDate=2024-01-01&endDate=2024-01-31`).

- `GET /api/v1/reports/general-journal` (Mengambil Jurnal Umum)
- `GET /api/v1/reports/general-ledger/{accountId}` (Mengambil Buku Besar untuk satu akun tertentu)
- `GET /api/v1/reports/trial-balance` (Mengambil Neraca Saldo)
- `GET /api/v1/reports/profit-and-loss` (Mengambil ringkasan Laba Rugi)
- `GET /api/v1/reports/balance-sheet` (Mengambil struktur Neraca)
- `GET /api/v1/reports/cash-flow` (Mengambil Arus Kas, memfilter pergerakan khusus pada akun Kas atau Bank)

## E. Dashboard Aggregation API

- `GET /api/v1/dashboard/summary` (Menyediakan data agregasi untuk halaman dashboard seperti total saldo kas, pendapatan bulanan, pengeluaran bulanan, laba bersih, serta grafik trend pendapatan & pengeluaran bulanan dan expense by category. CompanyId didapatkan dari JWT token pengguna yang login)
