# Prosedur Pengujian Titik Akhir API

1. Pastikan kode dapat dikompilasi tanpa kesalahan menggunakan Maven.
2. Buat skrip cURL atau fail `.http` untuk mensimulasikan permintaan dari klien.
3. Sertakan token JWT yang valid di bagian Header `Authorization: Bearer <token>`.
4. Validasi respons JSON untuk memastikan struktur sesuai dengan dokumen API. Pastikan status HTTP 200/201 untuk keberhasilan, dan 400/403/404 untuk penanganan kesalahan yang dikontrol.
