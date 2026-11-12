package model;

import java.time.LocalDate;

/** Dikerjakan oleh: BAYU */
public class Pengeluaran extends Transaksi {
    /** Nilai yang diizinkan: "Kebutuhan" atau "Keinginan". */
    private String prioritas;

    public Pengeluaran(String id, LocalDate tanggal, double nominal, String kategori, String catatan, String prioritas) {
        super(id, tanggal, nominal, kategori, catatan);
        setPrioritas(prioritas);
    }

    public String getPrioritas() { return prioritas; }
    
    public void setPrioritas(String prioritas) {
        if (prioritas == null || (!prioritas.equalsIgnoreCase("Kebutuhan") && !prioritas.equalsIgnoreCase("Keinginan"))) {
            throw new IllegalArgumentException("Prioritas harus 'Kebutuhan' atau 'Keinginan'.");
        }
        this.prioritas = prioritas;
    }

    @Override
    public double hitungDampakSaldo() {
        return -getNominal(); // Mengembalikan -nominal
    }

    @Override
    public String getTipeTransaksi() {
        return "Pengeluaran";
    }
}