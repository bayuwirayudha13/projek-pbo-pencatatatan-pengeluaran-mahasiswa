package model;

import java.time.LocalDate;

/**
 * Kelas induk abstrak untuk Pemasukan dan Pengeluaran.
 * Dikerjakan oleh: BAYU
 */
public abstract class Transaksi {
    private String id;
    private LocalDate tanggal;
    private double nominal;
    private String kategori;
    private String catatan;

    public Transaksi(String id, LocalDate tanggal, double nominal, String kategori, String catatan) {
        this.id = id;
        this.tanggal = tanggal;
        setNominal(nominal); // Memanggil setNominal agar langsung tervalidasi > 0
        this.kategori = kategori;
        this.catatan = catatan;
    }

    public String getId() { return id; }
    public LocalDate getTanggal() { return tanggal; }
    public double getNominal() { return nominal; }
    public String getKategori() { return kategori; }
    public String getCatatan() { return catatan; }

    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    
    public void setNominal(double nominal) {
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal transaksi harus lebih besar dari 0.");
        }
        this.nominal = nominal;
    }
    
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    /** Pemasukan mengembalikan +nominal, Pengeluaran mengembalikan -nominal. */
    public abstract double hitungDampakSaldo();

    /** Mengembalikan "Pemasukan" atau "Pengeluaran". */
    public abstract String getTipeTransaksi();
}