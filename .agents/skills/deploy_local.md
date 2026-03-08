# Menjalankan Server Lokal (Tahap Pengembangan)

1. Tanyakan kepada Pilot apakah pangkalan data PostgreSQL lokal sudah siap dan berjalan.
2. Jalankan perintah kompilasi: `./mvnw clean package -DskipTests`.
3. Jalankan aplikasi menggunakan profil pengembangan: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.
4. Pantau log terminal untuk memastikan tabel pangkalan data berhasil diperbarui oleh Hibernate (DDL-Auto) dan tidak ada kegagalan injeksi dependensi.
