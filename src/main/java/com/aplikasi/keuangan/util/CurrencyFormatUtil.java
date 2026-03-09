package com.aplikasi.keuangan.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatUtil {

    private static final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    /**
     * Mengonversi BigDecimal menjadi string bertuliskan format Rupiah.
     * Contoh: 10000 -> "Rp10.000,00" atau disesuaikan separator loka-nya.
     */
    public static String toRupiah(BigDecimal amount) {
        if (amount == null) return "Rp0,00";
        // Untuk Java 17, Locale("id", "ID") kerap mengembalikan "Rp10.000,00" (dengan spasi atau tidak)
        // Jika butuh tanpa spasi dan koma ekstra, kita bisa melakukan replace
        String formatted = rupiahFormat.format(amount);
        // Bisa juga membuang angka desimal ,00 di belakang jika perlu:
        // formatted = formatted.replace(",00", "");
        return formatted;
    }

    /**
     * Membuang dua digit desimal ,00 menjadi Rp10.000 saja seperti permintaan.
     */
    public static String toRupiahKompak(BigDecimal amount) {
        if (amount == null) return "Rp0";
        String formatted = rupiahFormat.format(amount);
        // Locale.ID akan mengembalikan format seperti Rp 10.000,00
        // Kita menghapus spasi awal jika ada, dan membuang desimalnya
        return formatted.replace(" ", "").replace(",00", "");
    }
}
