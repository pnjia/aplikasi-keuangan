# Aturan Saldo Normal Akuntansi

Dalam pembuatan sistem keuangan ini, wajib mematuhi pedoman Saldo Normal Akuntansi berikut ini agar tidak terjadi kesalahan peletakan nilai Debit dan Kredit di berbagai laporan keuangan (Buku Besar, Neraca Saldo, dsb):

| Kategori Akun            | Saldo Normal (Posisi Saldo Akhir) | Penambahan | Pengurangan |
| :----------------------- | :-------------------------------- | :--------- | :---------- |
| **Aset (Harta)**         | **DEBIT**                         | Debit      | Kredit      |
| **Liabilitas (Utang)**   | **KREDIT**                        | Kredit     | Debit       |
| **Ekuitas (Modal)**      | **KREDIT**                        | Kredit     | Debit       |
| **Pendapatan (Revenue)** | **KREDIT**                        | Kredit     | Debit       |
| **Beban (Expense)**      | **DEBIT**                         | Debit      | Kredit      |

**Aturan Implementasi pada Laporan:**

1. **Neraca Saldo (Trial Balance):** Sebuah baris akun **TIDAK BOLEH** memiliki saldo berjalan di kolom Debit dan Kredit secara bersamaan. Hitung saldo akhir (selisih total mutasi) dan letakkan hanya di salah satu sisi (Debit ATAU Kredit) sesuai dengan sifat posisi akhir saldo tersebut (lihat Saldo Normal). Jika saldo nol, letakkan angka 0 pada sisi Saldo Normal-nya.
2. **Saldo Berjalan (Running Balance):** Selalu dikalkulasikan dengan menjumlahkan mutasi searah Saldo Normal dan mengurangkan mutasi yang berlawanan arah dengan Saldo Normal.

_Referensi: Panduan Akuntansi Universal._
