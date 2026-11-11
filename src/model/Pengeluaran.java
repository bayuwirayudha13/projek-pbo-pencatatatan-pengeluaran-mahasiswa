package model;

import java.time.LocalDate;

/** Dikerjakan oleh: BAYU */
public class Pengeluaran extends Transaksi {
    /** Nilai yang diizinkan: "Kebutuhan" atau "Keinginan". */
    private String prioritas;

    public Pengeluaran(String id, LocalDate tanggal, double nominal, String kategori, String catatan, String prioritas) {
        super(id, tanggal, nominal, kategori, catatan);
        this.prioritas = prioritas;
    }

    public String getPrioritas() { return prioritas; }
    public void setPrioritas(String prioritas) { this.prioritas = prioritas; }

    @Override
    public double hitungDampakSaldo() {
        return 0; // TODO (Bayu): return -getNominal()
    }

    @Override
    public String getTipeTransaksi() {
        return "Pengeluaran";
    }
}
