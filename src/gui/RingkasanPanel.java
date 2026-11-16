package gui;

import service.ManajerMySaku;
import model.Transaksi;
import analisis.AnalisisKeuangan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Panel ringkasan keuangan (Dashboard).
 * Dikerjakan oleh: ALHIYA
 */
public class RingkasanPanel extends JPanel {

    private ManajerMySaku manajer;
    private AnalisisKeuangan analisis;

    // Kartu statistik
    private JLabel labelPemasukan;
    private JLabel labelPengeluaran;
    private JLabel labelSaldo;

    // Kartu anggaran
    private UITheme.Badge badgeStatus;
    private JLabel labelBatas;
    private JLabel labelTerpakai;
    private JLabel labelSisa;
    private UITheme.RoundedProgressBar barAnggaran;
    private JLabel labelPersenAnggaran;

    // Kartu prioritas
    private JLabel labelKebutuhan;
    private UITheme.RoundedProgressBar barKebutuhan;
    private JLabel labelKeinginan;
    private UITheme.RoundedProgressBar barKeinginan;

    public RingkasanPanel(ManajerMySaku manajer) {
        this.manajer = manajer;
        this.analisis = new AnalisisKeuangan();

        setOpaque(false);
        setLayout(new GridBagLayout());

        labelPemasukan = buatLabelNilai();
        labelPengeluaran = buatLabelNilai();
        labelSaldo = buatLabelNilai();

        // Baris 1: tiga kartu statistik
        JPanel barisStatistik = new JPanel(new GridLayout(1, 3, 4, 0));
        barisStatistik.setOpaque(false);

        barisStatistik.add(buatKartuStatistik(
            "Total Pemasukan",
            labelPemasukan,
            "Uang yang berhasil kamu kumpulkan",
            UITheme.IconType.INCOME,
            UITheme.SUCCESS_DARK,
            UITheme.SUCCESS_SOFT
        ));
        barisStatistik.add(buatKartuStatistik(
            "Total Pengeluaran",
            labelPengeluaran,
            "Total uang yang sudah digunakan",
            UITheme.IconType.EXPENSE,
            UITheme.DANGER_DARK,
            UITheme.DANGER_SOFT
        ));
        barisStatistik.add(buatKartuStatistik(
            "Saldo Akhir",
            labelSaldo,
            "Sisa uang dari seluruh transaksi",
            UITheme.IconType.BALANCE,
            UITheme.PRIMARY_DARK,
            UITheme.PRIMARY_SOFT
        ));

        // Baris 2: kartu anggaran (2 bagian) dan kartu prioritas (1 bagian)
        JComponent kartuAnggaran = buatKartuAnggaran();
        JComponent kartuPrioritas = buatKartuPrioritas();

        kartuAnggaran.setPreferredSize(new Dimension(
            200, kartuAnggaran.getPreferredSize().height
        ));
        kartuPrioritas.setPreferredSize(new Dimension(
            100, kartuPrioritas.getPreferredSize().height
        ));

        JPanel barisAnggaran = new JPanel(new GridBagLayout());
        barisAnggaran.setOpaque(false);

        GridBagConstraints kiri = new GridBagConstraints();
        kiri.gridx = 0;
        kiri.gridy = 0;
        kiri.weightx = 2;
        kiri.fill = GridBagConstraints.BOTH;
        barisAnggaran.add(kartuAnggaran, kiri);

        GridBagConstraints kanan = new GridBagConstraints();
        kanan.gridx = 1;
        kanan.gridy = 0;
        kanan.weightx = 1;
        kanan.fill = GridBagConstraints.BOTH;
        barisAnggaran.add(kartuPrioritas, kanan);

        // Susun dua baris
        GridBagConstraints atas = new GridBagConstraints();
        atas.gridx = 0;
        atas.gridy = 0;
        atas.weightx = 1;
        atas.fill = GridBagConstraints.HORIZONTAL;
        add(barisStatistik, atas);

        GridBagConstraints bawah = new GridBagConstraints();
        bawah.gridx = 0;
        bawah.gridy = 1;
        bawah.weightx = 1;
        bawah.fill = GridBagConstraints.HORIZONTAL;
        add(barisAnggaran, bawah);

        refreshRingkasan();
    }

    // ===================== PEMBUAT KOMPONEN =====================

    private JLabel buatLabelNilai() {
        return UITheme.label(
            "Rp0", UITheme.font(Font.BOLD, 26), UITheme.TEXT
        );
    }

    private UITheme.RoundedPanel buatKartuDasar() {
        UITheme.RoundedPanel kartu = new UITheme.RoundedPanel(
            18, UITheme.CARD, true
        );
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBorder(new EmptyBorder(20, 22, 20, 22));
        return kartu;
    }

    private UITheme.RoundedPanel buatKartuStatistik(
        String judul,
        JLabel labelNilai,
        String deskripsi,
        UITheme.IconType ikon,
        Color warnaIkon,
        Color latarIkon
    ) {
        UITheme.RoundedPanel kartu = buatKartuDasar();

        JLabel labelJudul = UITheme.label(
            judul, UITheme.font(Font.BOLD, 13), UITheme.TEXT_MUTED
        );

        JPanel kepala = new JPanel(new BorderLayout(12, 0));
        kepala.setOpaque(false);
        kepala.setAlignmentX(Component.LEFT_ALIGNMENT);
        kepala.add(new IconBox(ikon, warnaIkon, latarIkon),
            BorderLayout.WEST);
        kepala.add(labelJudul, BorderLayout.CENTER);

        labelNilai.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelDeskripsi = UITheme.label(
            deskripsi, UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );
        labelDeskripsi.setAlignmentX(Component.LEFT_ALIGNMENT);

        kartu.add(kepala);
        kartu.add(Box.createVerticalStrut(16));
        kartu.add(labelNilai);
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(labelDeskripsi);
        return kartu;
    }

    private JComponent buatKartuAnggaran() {
        UITheme.RoundedPanel kartu = buatKartuDasar();

        // Kepala: judul di kiri, badge status di kanan
        badgeStatus = new UITheme.Badge(
            "BELUM DIATUR", UITheme.TEXT_MUTED, UITheme.BORDER
        );

        JPanel pembungkusBadge = new JPanel(new GridBagLayout());
        pembungkusBadge.setOpaque(false);
        pembungkusBadge.add(badgeStatus);

        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.setAlignmentX(Component.LEFT_ALIGNMENT);
        kepala.add(
            buatJudulKartu(
                "Anggaran Harian", "Pemakaian anggaran hari ini"
            ),
            BorderLayout.WEST
        );
        kepala.add(pembungkusBadge, BorderLayout.EAST);

        // Tiga angka: batas, terpakai, sisa
        labelBatas = UITheme.label(
            "-", UITheme.font(Font.BOLD, 18), UITheme.TEXT
        );
        labelTerpakai = UITheme.label(
            "-", UITheme.font(Font.BOLD, 18), UITheme.TEXT
        );
        labelSisa = UITheme.label(
            "-", UITheme.font(Font.BOLD, 18), UITheme.TEXT
        );

        JPanel angka = new JPanel(new GridLayout(1, 3, 12, 0));
        angka.setOpaque(false);
        angka.setAlignmentX(Component.LEFT_ALIGNMENT);
        angka.add(buatBlokAngka("Batas harian", labelBatas));
        angka.add(buatBlokAngka("Terpakai", labelTerpakai));
        angka.add(buatBlokAngka("Sisa anggaran", labelSisa));

        barAnggaran = new UITheme.RoundedProgressBar();
        barAnggaran.setAlignmentX(Component.LEFT_ALIGNMENT);

        labelPersenAnggaran = UITheme.label(
            "", UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );
        labelPersenAnggaran.setAlignmentX(Component.LEFT_ALIGNMENT);

        kartu.add(kepala);
        kartu.add(Box.createVerticalStrut(20));
        kartu.add(angka);
        kartu.add(Box.createVerticalStrut(20));
        kartu.add(barAnggaran);
        kartu.add(Box.createVerticalStrut(8));
        kartu.add(labelPersenAnggaran);
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    private JComponent buatKartuPrioritas() {
        UITheme.RoundedPanel kartu = buatKartuDasar();

        JComponent judul = buatJudulKartu(
            "Kebutuhan vs Keinginan", "Perbandingan pengeluaranmu"
        );
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);

        labelKebutuhan = UITheme.label(
            "", UITheme.font(Font.BOLD, 13), UITheme.TEXT
        );
        labelKeinginan = UITheme.label(
            "", UITheme.font(Font.BOLD, 13), UITheme.TEXT
        );

        barKebutuhan = new UITheme.RoundedProgressBar();
        barKebutuhan.setWarna(UITheme.PRIMARY);
        barKeinginan = new UITheme.RoundedProgressBar();
        barKeinginan.setWarna(UITheme.WARNING);

        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(20));
        kartu.add(buatBarisPrioritas(
            "Kebutuhan", labelKebutuhan, barKebutuhan
        ));
        kartu.add(Box.createVerticalStrut(18));
        kartu.add(buatBarisPrioritas(
            "Keinginan", labelKeinginan, barKeinginan
        ));
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    private JComponent buatJudulKartu(String judul, String subjudul) {
        JLabel a = UITheme.label(
            judul, UITheme.font(Font.BOLD, 16), UITheme.TEXT
        );
        JLabel b = UITheme.label(
            subjudul, UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );
        a.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel kotak = new JPanel();
        kotak.setLayout(new BoxLayout(kotak, BoxLayout.Y_AXIS));
        kotak.setOpaque(false);
        kotak.add(a);
        kotak.add(Box.createVerticalStrut(3));
        kotak.add(b);
        return kotak;
    }

    private JComponent buatBlokAngka(String judul, JLabel labelNilai) {
        JLabel labelJudul = UITheme.label(
            judul, UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );
        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelNilai.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel blok = new JPanel();
        blok.setLayout(new BoxLayout(blok, BoxLayout.Y_AXIS));
        blok.setOpaque(false);
        blok.add(labelJudul);
        blok.add(Box.createVerticalStrut(4));
        blok.add(labelNilai);
        return blok;
    }

    private JComponent buatBarisPrioritas(
        String nama,
        JLabel labelNilai,
        UITheme.RoundedProgressBar bar
    ) {
        JLabel labelNama = UITheme.label(
            nama, UITheme.font(Font.PLAIN, 13), UITheme.TEXT_MUTED
        );

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(labelNama, BorderLayout.WEST);
        atas.add(labelNilai, BorderLayout.EAST);

        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel baris = new JPanel();
        baris.setLayout(new BoxLayout(baris, BoxLayout.Y_AXIS));
        baris.setOpaque(false);
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        baris.add(atas);
        baris.add(Box.createVerticalStrut(8));
        baris.add(bar);
        return baris;
    }

    // ===================== PEMBARUAN DATA =====================

    /**
     * Memperbarui seluruh informasi keuangan.
     */
    public void refreshRingkasan() {

        List<Transaksi> daftar = manajer.getSemuaTransaksi();

        double totalPemasukan = 0;
        double totalPengeluaran = 0;
        double saldo = 0;

        for (Transaksi transaksi : daftar) {

            double dampak = transaksi.hitungDampakSaldo();
            saldo += dampak;

            if (dampak > 0) {
                totalPemasukan += dampak;
            } else {
                totalPengeluaran += Math.abs(dampak);
            }
        }

        labelPemasukan.setText(UITheme.formatRupiah(totalPemasukan));
        labelPemasukan.setForeground(UITheme.SUCCESS_DARK);

        labelPengeluaran.setText(UITheme.formatRupiah(totalPengeluaran));
        labelPengeluaran.setForeground(UITheme.DANGER_DARK);

        labelSaldo.setText(UITheme.formatRupiah(saldo));
        labelSaldo.setForeground(
            saldo < 0 ? UITheme.DANGER_DARK : UITheme.PRIMARY_DARK
        );

        refreshAnggaran(daftar);
        refreshPrioritas(daftar);

        revalidate();
        repaint();
    }

    private void refreshAnggaran(List<Transaksi> daftar) {

        double batas = manajer.getAnggaran().getBatasHarian();

        if (batas <= 0) {
            badgeStatus.setText("BELUM DIATUR");
            badgeStatus.setWarna(UITheme.TEXT_MUTED, UITheme.BORDER);

            labelBatas.setText("-");
            labelTerpakai.setText("-");
            labelSisa.setText("-");
            labelSisa.setForeground(UITheme.TEXT);

            barAnggaran.setNilai(0);
            barAnggaran.setWarna(UITheme.PRIMARY);
            labelPersenAnggaran.setText(
                "Atur batas harian lewat tombol Atur Anggaran."
            );
            return;
        }

        double pengeluaranHariIni =
            analisis.hitungPengeluaranHarian(daftar, LocalDate.now());

        double sisa =
            manajer.getAnggaran().hitungSisa(pengeluaranHariIni);

        String status =
            manajer.getAnggaran().cekStatus(pengeluaranHariIni);

        double persentase = (pengeluaranHariIni / batas) * 100;

        labelBatas.setText(UITheme.formatRupiah(batas));
        labelTerpakai.setText(UITheme.formatRupiah(pengeluaranHariIni));
        labelSisa.setText(UITheme.formatRupiah(sisa));
        labelSisa.setForeground(
            sisa < 0 ? UITheme.DANGER_DARK : UITheme.TEXT
        );

        barAnggaran.setNilai((int) Math.round(persentase));
        labelPersenAnggaran.setText(
            String.format(Locale.US, "%.0f%% dari batas harian terpakai",
                persentase)
        );

        badgeStatus.setText(status);

        if ("MELAMPAUI".equals(status)) {
            badgeStatus.setWarna(UITheme.DANGER_DARK, UITheme.DANGER_SOFT);
            barAnggaran.setWarna(UITheme.DANGER);
        } else if ("MENDEKATI".equals(status)) {
            badgeStatus.setWarna(UITheme.WARNING_DARK, UITheme.WARNING_SOFT);
            barAnggaran.setWarna(UITheme.WARNING);
        } else {
            badgeStatus.setWarna(UITheme.SUCCESS_DARK, UITheme.SUCCESS_SOFT);
            barAnggaran.setWarna(UITheme.SUCCESS);
        }
    }

    private void refreshPrioritas(List<Transaksi> daftar) {

        double totalKebutuhan =
            analisis.hitungTotalPerPrioritas(daftar, "Kebutuhan");
        double persenKebutuhan =
            analisis.hitungPersentasePrioritas(daftar, "Kebutuhan");

        double totalKeinginan =
            analisis.hitungTotalPerPrioritas(daftar, "Keinginan");
        double persenKeinginan =
            analisis.hitungPersentasePrioritas(daftar, "Keinginan");

        labelKebutuhan.setText(
            UITheme.formatRupiah(totalKebutuhan) + " - "
            + String.format(Locale.US, "%.0f%%", persenKebutuhan)
        );
        barKebutuhan.setNilai((int) Math.round(persenKebutuhan));

        labelKeinginan.setText(
            UITheme.formatRupiah(totalKeinginan) + " - "
            + String.format(Locale.US, "%.0f%%", persenKeinginan)
        );
        barKeinginan.setNilai((int) Math.round(persenKeinginan));
    }

    // ===================== KOMPONEN KECIL =====================

    /**
     * Kotak membulat berlatar lembut berisi ikon.
     */
    private static class IconBox extends JComponent {

        private final UITheme.IconType ikon;
        private final Color warna;
        private final Color latar;

        IconBox(UITheme.IconType ikon, Color warna, Color latar) {
            this.ikon = ikon;
            this.warna = warna;
            this.latar = latar;

            Dimension ukuran = new Dimension(44, 44);
            setPreferredSize(ukuran);
            setMinimumSize(ukuran);
            setMaximumSize(ukuran);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.antialias(g2);

            g2.setColor(latar);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

            new UITheme.VectorIcon(ikon, 22, warna).paintIcon(
                this, g2,
                (getWidth() - 22) / 2, (getHeight() - 22) / 2
            );

            g2.dispose();
        }
    }
}