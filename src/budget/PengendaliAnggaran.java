package budget;

/**
 * Mengatur batas pengeluaran harian dan memeriksa statusnya.
 * Dikerjakan oleh: BAYU
 */
public class PengendaliAnggaran {
    private double batasHarian;

    public PengendaliAnggaran() {
        this.batasHarian = 0;
    }

    public PengendaliAnggaran(double batasHarian) {
        this.batasHarian = batasHarian;
    }

    public double getBatasHarian() { return batasHarian; }

    public void setBatasHarian(double batasHarian) {
        this.batasHarian = batasHarian; // TODO (Bayu): lempar IllegalArgumentException jika <= 0
    }

    /** Sisa anggaran = batas - total pengeluaran hari ini. */
    public double hitungSisa(double totalHariIni) {
        return 0; // TODO (Bayu)
    }

    /**
     * Mengembalikan "AMAN", "MENDEKATI" (total >= 80% batas), atau "MELAMPAUI" (total > 100% batas).
     */
    public String cekStatus(double totalHariIni) {
        return "AMAN"; // TODO (Bayu)
    }
}
