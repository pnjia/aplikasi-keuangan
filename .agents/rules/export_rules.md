# Aturan Ekspor Laporan Keuangan

1. Format Mata Uang: Semua nilai uang (nominal/saldo/debit/kredit) yang dirender di dalam ekspor dokumen laporan keuangan (baik itu format PDF maupun Excel) WAJIB diformat ke dalam penulisan Rupiah standar Indonesia yang mudah dibaca oleh manusia atau non-teknikal.
2. Contoh Penulisan: `Rp10.000` atau `Rp10.000,00`. Tidak boleh dibiarkan menjadi tampilan mentah berupa angka panjang seperti `10000.0000` atau tanpa pemisah ribuan.
3. Untuk file PDF: Terjemahkan ke dalam bentuk _String_ menggunakan pemformatan _Locale_ id-ID sebelum dimasukkan ke dalam sel tabel.
4. Untuk file Excel (XLSX): Jika memungkinkan, terapkan `CellStyle` dengan Data Format khusus Rupiah pada sel angka tersebut, atau konversi menjadi Teks Berformat agar nominal langsung mudah dibaca ketika file dibuka.
