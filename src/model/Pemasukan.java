package model;

import java.time.LocalDate;

/** Dikerjakan oleh: BAYU */
public class Pemasukan extends Transaksi {
    private String sumber;

    public Pemasukan(String id, LocalDate tanggal, double nominal, String kategori, String catatan, String sumber) {
        super(id, tanggal, nominal, kategori, catatan);
        this.sumber = sumber;
    }

    public String getSumber() { return sumber; }
    public void setSumber(String sumber) { this.sumber = sumber; }

    @Override
    public double hitungDampakSaldo() {
        return getNominal(); // Mengembalikan +nominal
    }

    @Override
    public String getTipeTransaksi() {
        return "Pemasukan";
    }
}