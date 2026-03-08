# Skema Database Mutlak (Entity Relationship Diagram)

Kamu WAJIB mengikuti rancangan struktur tabel, nama kolom, dan tipe data di bawah ini saat membuat kelas Entity JPA. Dilarang mengubah tipe data (terutama untuk uang) atau menghapus kolom audit.

## 0. Kolom Wajib Audit (Base Entity)

Setiap tabel master dan transaksi (kecuali tabel `users` dan _junction table_ seperti `company_roles`) wajib mewarisi lima kolom standar ini untuk keperluan audit dan keamanan data (Soft Delete):

- `created_at` (TIMESTAMPTZ di DB / ZonedDateTime di Java)
- `updated_at` (TIMESTAMPTZ, Nullable)
- `deleted_at` (TIMESTAMPTZ, Nullable untuk fitur Soft Delete)
- `created_by` (UUID, FK ke `users.id`)
- `updated_by` (UUID, FK ke `users.id`, Nullable)

## 1. Entitas Pengguna & Perusahaan (Tenant)

**Tabel: `users`** (Menyimpan data otentikasi)

- `id` (PK, UUID)
- `email` (VARCHAR, Unique, Indexed)
- `password_hash` (VARCHAR, Terenkripsi dengan Bcrypt)
- `full_name` (VARCHAR)
- `created_at` (TIMESTAMPTZ)
- `updated_at` (TIMESTAMPTZ, Nullable)

**Tabel: `companies`** (Menyimpan profil bisnis)

- `id` (PK, UUID)
- `owner_id` (UUID, FK ke `users.id`, Indexed)
- `name` (VARCHAR)
- `address` (TEXT)
- `tax_number` (VARCHAR, Nullable)
- _(Termasuk 5 Kolom Wajib Audit)_

**Tabel: `company_roles`** (Hak akses karyawan per perusahaan)

- `id` (PK, UUID)
- `company_id` (UUID, FK ke `companies.id`, Indexed)
- `user_id` (UUID, FK ke `users.id`, Indexed)
- `role_name` (VARCHAR / Enum: OWNER, ADMIN, KASIR)
- `created_at` (TIMESTAMPTZ)

## 2. Entitas Master Data (Kontak & Kategori)

**Tabel: `contacts`** (Klien, Pelanggan, atau Vendor)

- `id` (PK, UUID)
- `company_id` (UUID, FK ke `companies.id`, Indexed)
- `type` (VARCHAR / Enum: CUSTOMER, VENDOR)
- `name` (VARCHAR)
- `phone` (VARCHAR, Nullable)
- `email` (VARCHAR, Nullable)
- _(Termasuk 5 Kolom Wajib Audit)_

**Tabel: `accounts`** (Bagan Akun / Chart of Accounts)

- `id` (PK, UUID)
- `company_id` (UUID, FK ke `companies.id`, Indexed)
- `parent_account_id` (UUID, FK ke `accounts.id`, Nullable, relasi hierarki mandiri)
- `account_code` (VARCHAR, contoh: "1000" atau "1000.1")
- `account_name` (VARCHAR, contoh: "Kas Utama")
- `account_type` (VARCHAR / Enum: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE)
- _(Termasuk 5 Kolom Wajib Audit)_

## 3. Entitas Transaksi & Dokumen Keuangan

**Tabel: `invoices`** (Dokumen tagihan utama)

- `id` (PK, UUID)
- `company_id` (UUID, FK ke `companies.id`, Indexed)
- `contact_id` (UUID, FK ke `contacts.id`, Indexed)
- `invoice_number` (VARCHAR, Unique per company)
- `issue_date` (TIMESTAMPTZ)
- `due_date` (TIMESTAMPTZ)
- `total_amount` (Wajib: NUMERIC(19, 4) di DB / BigDecimal di Java)
- `status` (VARCHAR / Enum: DRAFT, SENT, PAID, CANCELLED)
- _(Termasuk 5 Kolom Wajib Audit)_

**Tabel: `invoice_items`** (Detail barang/jasa di dalam tagihan)

- `id` (PK, UUID)
- `invoice_id` (UUID, FK ke `invoices.id`, Indexed)
- `description` (TEXT)
- `quantity` (Wajib: NUMERIC(19, 4) / BigDecimal)
- `unit_price` (Wajib: NUMERIC(19, 4) / BigDecimal)
- `subtotal` (Wajib: NUMERIC(19, 4) / BigDecimal)
- _(Termasuk 5 Kolom Wajib Audit)_

**Tabel: `journal_entries`** (Referensi transaksi akuntansi)

- `id` (PK, UUID)
- `company_id` (UUID, FK ke `companies.id`, Indexed)
- `transaction_date` (TIMESTAMPTZ, Indexed untuk laporan)
- `reference_number` (VARCHAR, nomor kuitansi/invoice)
- `description` (TEXT)
- _(Termasuk 5 Kolom Wajib Audit)_

**Tabel: `journal_lines`** (Pencatatan ganda / Double Entry)

- `id` (PK, UUID)
- `journal_entry_id` (UUID, FK ke `journal_entries.id`, Indexed)
- `account_id` (UUID, FK ke `accounts.id`, Indexed)
- `debit_amount` (Wajib: NUMERIC(19, 4) / BigDecimal, default 0)
- `credit_amount` (Wajib: NUMERIC(19, 4) / BigDecimal, default 0)
- _(Termasuk 5 Kolom Wajib Audit)_
