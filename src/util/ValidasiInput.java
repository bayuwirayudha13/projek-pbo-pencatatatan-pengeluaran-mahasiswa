package util;

import java.time.LocalDate;

/**
 * Pengecekan input dari pengguna. Dikerjakan oleh: NEHA
 * Semua method melempar InputTidakValidException jika input salah.
 */
public class ValidasiInput {

    /** Harus angka dan lebih besar dari 0. */
    public static double validasiNominal(String teks) throws InputTidakValidException {
        return 0; // TODO (Neha)
    }

    /** Format yyyy-MM-dd. */
    public static LocalDate validasiTanggal(String teks) throws InputTidakValidException {
        return LocalDate.now(); // TODO (Neha)
    }

    /** Kolom tidak boleh kosong. Mengembalikan isi yang sudah di-trim. */
    public static String validasiWajibIsi(String isi, String namaKolom) throws InputTidakValidException {
        return isi; // TODO (Neha)
    }
}
