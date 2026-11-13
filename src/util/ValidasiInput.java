package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Pengecekan input dari pengguna. Dikerjakan oleh: NEHA
 * Semua method melempar InputTidakValidException jika input salah.
 */
public class ValidasiInput {

    /** Harus angka dan lebih besar dari 0. */
    public static double validasiNominal(String teks) throws InputTidakValidException {
        if (teks == null || teks.trim().isEmpty()) {
            throw new InputTidakValidException("Nominal wajib diisi.");
        }

        try {
            double nominal = Double.parseDouble(teks.trim());
            if (!Double.isFinite(nominal) || nominal <= 0) {
                throw new InputTidakValidException("Nominal harus berupa angka lebih besar dari 0.");
            }
            return nominal;
        } catch (NumberFormatException e) {
            throw new InputTidakValidException("Nominal harus berupa angka lebih besar dari 0.");
        }
    }

    /** Format yyyy-MM-dd. */
    public static LocalDate validasiTanggal(String teks) throws InputTidakValidException {
        if (teks == null || teks.trim().isEmpty()) {
            throw new InputTidakValidException("Tanggal wajib diisi dengan format yyyy-MM-dd.");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);
            return LocalDate.parse(teks.trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new InputTidakValidException("Tanggal harus menggunakan format yyyy-MM-dd.");
        }
    }

    /** Kolom tidak boleh kosong. Mengembalikan isi yang sudah di-trim. */
    public static String validasiWajibIsi(String isi, String namaKolom) throws InputTidakValidException {
        String nama = namaKolom == null || namaKolom.trim().isEmpty()
                ? "Kolom"
                : namaKolom.trim();
        if (isi == null || isi.trim().isEmpty()) {
            throw new InputTidakValidException(nama + " wajib diisi.");
        }
        return isi.trim();
    }
}
