
package analisis;

import model.Transaksi;
import model.Pengeluaran;

import java.time.LocalDate;
import java.util.List;

/**
 * Perhitungan untuk panel ringkasan.
 * Dikerjakan oleh: ALHIYA
 */
public class AnalisisKeuangan {

    /**
     * Menghitung total pengeluaran pada tanggal tertentu.
     */
    public double hitungPengeluaranHarian(
            List<Transaksi> daftar, LocalDate tanggal) {

        double total = 0;

        for (Transaksi transaksi : daftar) {
            if (transaksi instanceof Pengeluaran
                    && transaksi.getTanggal().equals(tanggal)) {

                total += transaksi.getNominal();
            }
        }

        return total;
    }

    /**
     * Menghitung total pengeluaran berdasarkan prioritas.
     * Prioritas: Kebutuhan atau Keinginan.
     */
    public double hitungTotalPerPrioritas(
            List<Transaksi> daftar, String prioritas) {

        double total = 0;

        for (Transaksi transaksi : daftar) {
            if (transaksi instanceof Pengeluaran) {
                Pengeluaran pengeluaran = (Pengeluaran) transaksi;

                if (pengeluaran.getPrioritas()
                        .equalsIgnoreCase(prioritas)) {

                    total += pengeluaran.getNominal();
                }
            }
        }

        return total;
    }

    /**
     * Menghitung persentase pengeluaran berdasarkan prioritas.
     */
    public double hitungPersentasePrioritas(
            List<Transaksi> daftar, String prioritas) {

        double totalPengeluaran = 0;

        for (Transaksi transaksi : daftar) {
            if (transaksi instanceof Pengeluaran) {
                totalPengeluaran += transaksi.getNominal();
            }
        }

        if (totalPengeluaran == 0) {
            return 0;
        }

        double totalPrioritas =
                hitungTotalPerPrioritas(daftar, prioritas);

        return (totalPrioritas / totalPengeluaran) * 100;
    }
}
