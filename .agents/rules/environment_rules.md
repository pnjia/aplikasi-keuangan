---
trigger: always_on
---

# Akses Kredensial dan Lingkungan

1. Larangan Akses Langsung: Kamu dilarang mencari, menebak, atau mencoba mengakses variabel lingkungan (environment variables) seperti kata sandi pangkalan data, kunci rahasia JWT, atau kredensial peladen.
2. Protokol Bertanya: Jika kamu membutuhkan nilai dari variabel lingkungan untuk menjalankan tes lokal atau menulis fail konfigurasi `application.yml`, kamu WAJIB berhenti bekerja dan meminta Pilot untuk memberikannya.
3. Contoh Respons Diharapkan: "Pilot, tolong berikan kata sandi untuk PostgreSQL dan kunci rahasia JWT agar saya bisa mengonfigurasi properti aplikasi."
