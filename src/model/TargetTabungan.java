
package model;

import java.time.LocalDate;

public class TargetTabungan {

    private String id;
    private String nama;
    private double nominalTarget;
    private double nominalTerkumpul;
    private LocalDate tanggalTarget;

    public TargetTabungan(
            String id,
            String nama,
            double nominalTarget,
            double nominalTerkumpul,
            LocalDate tanggalTarget) {

        setId(id);
        setNama(nama);
        setNominalTarget(nominalTarget);
        setNominalTerkumpul(nominalTerkumpul);
        this.tanggalTarget = tanggalTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID target wajib diisi.");
        }
        this.id = id.trim();
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama tabungan wajib diisi.");
        }
        this.nama = nama.trim();
    }

    public double getNominalTarget() {
        return nominalTarget;
    }

    public void setNominalTarget(double nominalTarget) {
        if (!Double.isFinite(nominalTarget) || nominalTarget <= 0) {
            throw new IllegalArgumentException("Target harus lebih dari 0.");
        }
        this.nominalTarget = nominalTarget;
    }

    public double getNominalTerkumpul() {
        return nominalTerkumpul;
    }

    public void setNominalTerkumpul(double nominalTerkumpul) {
        if (!Double.isFinite(nominalTerkumpul) || nominalTerkumpul < 0) {
            throw new IllegalArgumentException("Jumlah tabungan tidak valid.");
        }
        this.nominalTerkumpul = nominalTerkumpul;
    }

    public LocalDate getTanggalTarget() {
        return tanggalTarget;
    }

    public void setTanggalTarget(LocalDate tanggalTarget) {
        this.tanggalTarget = tanggalTarget;
    }

    public double getSisaTabungan() {
        return Math.max(0, nominalTarget - nominalTerkumpul);
    }

    public double getPersentase() {
        return Math.min(100, (nominalTerkumpul / nominalTarget) * 100);
    }

    public boolean isTercapai() {
        return nominalTerkumpul >= nominalTarget;
    }

    public void tambahSetoran(double nominal) {
        if (!Double.isFinite(nominal) || nominal <= 0) {
            throw new IllegalArgumentException("Setoran harus lebih dari 0.");
        }
        nominalTerkumpul += nominal;
    }
}
