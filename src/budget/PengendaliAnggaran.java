
package budget;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class PengendaliAnggaran {

    private final Map<LocalDate, Double> daftarAnggaran = new TreeMap<>();

    public double getBatasHarian(LocalDate tanggal) {
        return daftarAnggaran.getOrDefault(tanggal, 0.0);
    }

    public void setBatasHarian(LocalDate tanggal, double batasHarian) {
        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal wajib diisi.");
        }

        if (!Double.isFinite(batasHarian) || batasHarian <= 0) {
            throw new IllegalArgumentException(
                "Batas anggaran harian harus lebih besar dari 0."
            );
        }

        daftarAnggaran.put(tanggal, batasHarian);
    }

    public void hapusAnggaran(LocalDate tanggal) {
        if (tanggal != null) {
            daftarAnggaran.remove(tanggal);
        }
    }

    public double hitungSisa(LocalDate tanggal, double totalHariIni) {
        return getBatasHarian(tanggal) - totalHariIni;
    }

    public String cekStatus(LocalDate tanggal, double totalHariIni) {
        double batas = getBatasHarian(tanggal);

        if (batas <= 0) {
            return "BELUM DIATUR";
        }

        if (totalHariIni > batas) {
            return "MELAMPAUI";
        } else if (totalHariIni >= 0.8 * batas) {
            return "MENDEKATI";
        }

        return "AMAN";
    }

    public Map<LocalDate, Double> getSemuaAnggaran() {
        return Collections.unmodifiableMap(daftarAnggaran);
    }

    public void muatAnggaran(LocalDate tanggal, double batas) {
        if (tanggal != null && Double.isFinite(batas) && batas > 0) {
            daftarAnggaran.put(tanggal, batas);
        }
    }

public void hapusSemuaAnggaran() {
    daftarAnggaran.clear();
}
}
