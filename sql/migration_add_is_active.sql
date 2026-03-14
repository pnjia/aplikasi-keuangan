-- =============================================================================
-- MIGRATION SCRIPT: Tambah kolom is_active pada tabel accounts, contacts,
--                   dan company_roles
--
-- Tujuan   : Sinkronisasi skema PostgreSQL dengan entitas Hibernate terbaru.
-- Dibuat   : 2026-03-14
-- CARA PAKAI: Eksekusi di pgAdmin atau DBeaver dengan menjalankan seluruh file
--             ini sekaligus. Script ini AMAN (idempoten) — tidak akan gagal
--             jika kolom sudah ada, karena menggunakan pengecekan DO $$ block.
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. Tabel: accounts
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name  = 'accounts'
          AND column_name = 'is_active'
    ) THEN
        ALTER TABLE accounts
            ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

        RAISE NOTICE 'Kolom is_active berhasil ditambahkan ke tabel accounts.';
    ELSE
        RAISE NOTICE 'Kolom is_active sudah ada di tabel accounts, dilewati.';
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 2. Tabel: contacts
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name  = 'contacts'
          AND column_name = 'is_active'
    ) THEN
        ALTER TABLE contacts
            ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

        RAISE NOTICE 'Kolom is_active berhasil ditambahkan ke tabel contacts.';
    ELSE
        RAISE NOTICE 'Kolom is_active sudah ada di tabel contacts, dilewati.';
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 3. Tabel: company_roles
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name  = 'company_roles'
          AND column_name = 'is_active'
    ) THEN
        ALTER TABLE company_roles
            ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

        RAISE NOTICE 'Kolom is_active berhasil ditambahkan ke tabel company_roles.';
    ELSE
        RAISE NOTICE 'Kolom is_active sudah ada di tabel company_roles, dilewati.';
    END IF;
END $$;

COMMIT;

-- =============================================================================
-- VERIFIKASI (Jalankan terpisah setelah COMMIT di atas untuk memastikan):
-- =============================================================================
-- SELECT table_name, column_name, data_type, column_default, is_nullable
-- FROM information_schema.columns
-- WHERE table_name IN ('accounts', 'contacts', 'company_roles')
--   AND column_name = 'is_active'
-- ORDER BY table_name;
