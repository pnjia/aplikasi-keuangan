-- =============================================================================
-- DIAGNOSTIC SCRIPT: Investigasi Struktur Tabel accounts & Kolom BaseEntity
-- Tujuan   : Menemukan root cause error "Gagal Memuat Database" pada
--            GET /api/v1/accounts dengan memeriksa skema PostgreSQL secara
--            menyeluruh.
-- CARA PAKAI: Jalankan setiap blok secara terpisah di pgAdmin/DBeaver.
-- =============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- CEK 1: Lihat SEMUA kolom pada tabel accounts beserta tipe data dan default
--         Ini adalah pemeriksaan utama. Bandingkan hasilnya dengan entity Java.
-- ─────────────────────────────────────────────────────────────────────────────
SELECT
    column_name,
    data_type,
    udt_name,               -- tipe asli PostgreSQL (misal: uuid, timestamptz)
    is_nullable,
    column_default,
    character_maximum_length
FROM information_schema.columns
WHERE table_schema = 'public'       -- ganti jika schema Anda bukan 'public'
  AND table_name   = 'accounts'
ORDER BY ordinal_position;


-- ─────────────────────────────────────────────────────────────────────────────
-- CEK 2: Verifikasi cepat — apakah SEMUA kolom wajib ada sekaligus?
--         Akan menampilkan kolom-kolom yang ADA dari daftar yang diharapkan.
--         Jika ada baris yang hilang dari hasil, berarti kolom itu belum ada.
-- ─────────────────────────────────────────────────────────────────────────────
SELECT column_name
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name   = 'accounts'
  AND column_name IN (
      -- Kolom dari BaseEntity
      'created_at',
      'updated_at',
      'deleted_at',
      'created_by',
      'updated_by',
      -- Kolom dari entity Account itu sendiri
      'id',
      'company_id',
      'parent_account_id',
      'account_code',
      'account_name',
      'account_type',
      'is_active'
  )
ORDER BY column_name;


-- ─────────────────────────────────────────────────────────────────────────────
-- CEK 3 (Jika ada kolom hilang): Script ALTER TABLE yang aman (idempoten)
--         Uncomment dan jalankan hanya kolom yang TIDAK muncul di hasil CEK 2.
-- ─────────────────────────────────────────────────────────────────────────────

/*
BEGIN;

-- Uncomment baris yang dibutuhkan sesuai kolom yang hilang:

-- ALTER TABLE accounts ADD COLUMN IF NOT EXISTS is_active        BOOLEAN                  NOT NULL DEFAULT TRUE;
-- ALTER TABLE accounts ADD COLUMN IF NOT EXISTS created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
-- ALTER TABLE accounts ADD COLUMN IF NOT EXISTS updated_at       TIMESTAMP WITH TIME ZONE;
-- ALTER TABLE accounts ADD COLUMN IF NOT EXISTS deleted_at       TIMESTAMP WITH TIME ZONE;
-- ALTER TABLE accounts ADD COLUMN IF NOT EXISTS created_by       UUID;
-- ALTER TABLE accounts ADD COLUMN IF NOT EXISTS updated_by       UUID;

COMMIT;
*/


-- ─────────────────────────────────────────────────────────────────────────────
-- CEK 4: Periksa tabel lain yang juga di-query saat startup Hibernate
--         (contacts dan company_roles, karena keduanya juga punya BaseEntity)
-- ─────────────────────────────────────────────────────────────────────────────
SELECT
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name   IN ('accounts', 'contacts', 'company_roles')
  AND column_name  IN ('created_at', 'updated_at', 'deleted_at',
                       'created_by', 'updated_by', 'is_active')
ORDER BY table_name, column_name;
