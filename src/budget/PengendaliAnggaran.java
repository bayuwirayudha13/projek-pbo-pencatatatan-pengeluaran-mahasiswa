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
        setBatasHarian(batasHarian); // Memanggil setter agar tervalidasi > 0
    }

    public double getBatasHarian() { return batasHarian; }

    public void setBatasHarian(double batasHarian) {
        if (batasHarian <= 0) {
            throw new IllegalArgumentException("Batas anggaran harian harus lebih besar dari 0.");
        }
        this.batasHarian = batasHarian;
    }

    /** Sisa anggaran = batas - total pengeluaran hari ini. */
    public double hitungSisa(double totalHariIni) {
        return batasHarian - totalHariIni;
    }

    /**
     * Mengembalikan "AMAN", "MENDEKATI" (total >= 80% batas), atau "MELAMPAUI" (total > 100% batas).
     */
    public String cekStatus(double totalHariIni) {
        if (batasHarian <= 0) {
            return "AMAN"; // Jika batas harian belum diatur (masih 0), status dianggap AMAN
        }
        if (totalHariIni > batasHarian) {
            return "MELAMPAUI";
        } else if (totalHariIni >= (0.8 * batasHarian)) {
            return "MENDEKATI";
        } else {
            return "AMAN";
        }
    }
}