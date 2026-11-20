
package gui;

import analisis.AnalisisKeuangan;
import model.Transaksi;
import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Panel info di samping form transaksi: ringkasan hari ini
 * dan daftar transaksi terakhir.
 * Dikerjakan oleh: ALHIYA
 */
public class PanelInfoTransaksi extends JPanel {

    private static final int JUMLAH_TERAKHIR = 4;
    private static final Color GARIS_HALUS =
        new Color(241, 239, 252);

    private static final DateTimeFormatter FORMAT_TANGGAL =
        DateTimeFormatter.ofPattern(
            "d MMM yyyy",
            Locale.forLanguageTag("id-ID")
        );

    private ManajerMySaku manajer;
    private AnalisisKeuangan analisis = new AnalisisKeuangan();
    private Runnable onLihatSemua;

    // Kartu ringkasan
    private JLabel labelSaldo;
    private JLabel labelTerpakai;
    private JLabel labelSisa;
    private UITheme.Badge badgeStatus;
    private UITheme.RoundedProgressBar barAnggaran;
    private JLabel labelPersen;

    // Kartu transaksi terakhir
    private JPanel panelDaftar;
    private JLabel labelJumlah;

    public PanelInfoTransaksi(ManajerMySaku manajer) {
        this.manajer = manajer;

        setOpaque(false);
        setLayout(new BorderLayout(0, 40));

        add(buatKartuRingkasan(), BorderLayout.NORTH);
        add(buatKartuTerakhir(), BorderLayout.CENTER);

        refreshInfo();
    }

    /**
     * Aksi untuk tombol "Lihat Semua".
     */
    public void setOnLihatSemua(Runnable callback) {
        this.onLihatSemua = callback;
    }

    // ===================== KARTU RINGKASAN =====================

    private JComponent buatKartuRingkasan() {
        UITheme.RoundedPanel kartu = new UITheme.RoundedPanel(
            18, UITheme.CARD, false
        );

        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBorder(new EmptyBorder(20, 22, 20, 22));

        // Kepala: judul di kiri, badge status di kanan
        badgeStatus = new UITheme.Badge(
            "BELUM DIATUR",
            UITheme.TEXT_MUTED,
            UITheme.BORDER
        );

        JPanel pembungkusBadge = new JPanel(new GridBagLayout());
        pembungkusBadge.setOpaque(false);
        pembungkusBadge.add(badgeStatus);

        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);

        kepala.add(
            buatJudul(
                "Ringkasan Hari Ini",
                UITheme.label(
                    "Pantau dampaknya ke anggaranmu",
                    UITheme.font(Font.PLAIN, 12),
                    UITheme.TEXT_MUTED
                )
            ),
            BorderLayout.WEST
        );

        kepala.add(pembungkusBadge, BorderLayout.EAST);

        // Saldo besar
        JLabel judulSaldo = UITheme.label(
            "Saldo akhir",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        labelSaldo = UITheme.label(
            "Rp0",
            UITheme.font(Font.BOLD, 30),
            UITheme.PRIMARY_DARK
        );

        // Dua baris rincian
        labelTerpakai = UITheme.label(
            "-",
            UITheme.font(Font.BOLD, 14),
            UITheme.TEXT
        );

        labelSisa = UITheme.label(
            "-",
            UITheme.font(Font.BOLD, 14),
            UITheme.TEXT
        );

        barAnggaran = new UITheme.RoundedProgressBar();

        labelPersen = UITheme.label(
            " ",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        tambah(kartu, kepala, 18);
        tambah(kartu, judulSaldo, 2);
        tambah(kartu, labelSaldo, 14);
        tambah(kartu, garis(), 0);
        tambah(
            kartu,
            buatBaris("Pengeluaran hari ini", labelTerpakai),
            0
        );
        tambah(
            kartu,
            buatBaris("Sisa anggaran", labelSisa),
            14
        );
        tambah(kartu, barAnggaran, 8);
        tambah(kartu, labelPersen, 0);

        return kartu;
    }

    // ===================== KARTU TRANSAKSI TERAKHIR =====================

    private JComponent buatKartuTerakhir() {
        UITheme.RoundedPanel kartu = new UITheme.RoundedPanel(
            18, UITheme.CARD, false
        );

        kartu.setLayout(new BorderLayout());
        kartu.setBorder(new EmptyBorder(20, 22, 12, 22));

        labelJumlah = UITheme.label(
            " ",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        UITheme.ModernButton tombolLihat = new UITheme.ModernButton(
            "Lihat Semua",
            UITheme.PRIMARY,
            UITheme.ModernButton.SOFT
        );

        tombolLihat.setFont(UITheme.font(Font.BOLD, 12));
        tombolLihat.setBorder(new EmptyBorder(7, 14, 7, 14));

        tombolLihat.addActionListener(e -> {
            if (onLihatSemua != null) {
                onLihatSemua.run();
            }
        });

        JPanel pembungkusTombol = new JPanel(new GridBagLayout());
        pembungkusTombol.setOpaque(false);
        pembungkusTombol.add(tombolLihat);

        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.setAlignmentX(Component.LEFT_ALIGNMENT);

        kepala.add(
            buatJudul("Transaksi Terakhir", labelJumlah),
            BorderLayout.WEST
        );

        kepala.add(pembungkusTombol, BorderLayout.EAST);

        JPanel atas = new JPanel();
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        atas.setOpaque(false);
        atas.add(kepala);
        atas.add(Box.createVerticalStrut(14));
        atas.add(garis());

        panelDaftar = new JPanel();
        panelDaftar.setLayout(
            new BoxLayout(panelDaftar, BoxLayout.Y_AXIS)
        );
        panelDaftar.setOpaque(false);

        JPanel pembungkusDaftar = new JPanel(new BorderLayout());
        pembungkusDaftar.setOpaque(false);
        pembungkusDaftar.add(panelDaftar, BorderLayout.NORTH);

        kartu.add(atas, BorderLayout.NORTH);
        kartu.add(pembungkusDaftar, BorderLayout.CENTER);

        return kartu;
    }

    // ===================== PEMBARUAN DATA =====================

    /**
     * Memperbarui seluruh isi panel dari data terbaru.
     */
    public void refreshInfo() {

        List<Transaksi> semua = manajer.getSemuaTransaksi();

        // ---------- Ringkasan saldo ----------
        double saldo = manajer.getSaldo();

        labelSaldo.setText(UITheme.formatRupiah(saldo));

        labelSaldo.setForeground(
            saldo < 0
                ? UITheme.DANGER_DARK
                : UITheme.PRIMARY_DARK
        );

        // ---------- Anggaran berdasarkan tanggal hari ini ----------
        LocalDate hariIni = LocalDate.now();

        double terpakai = analisis.hitungPengeluaranHarian(
            semua,
            hariIni
        );

        labelTerpakai.setText(UITheme.formatRupiah(terpakai));

        // Ambil batas anggaran khusus hari ini.
        double batas = manajer.getAnggaran()
            .getBatasHarian(hariIni);

        if (batas <= 0) {

            badgeStatus.setText("BELUM DIATUR");
            badgeStatus.setWarna(
                UITheme.TEXT_MUTED,
                UITheme.BORDER
            );

            labelSisa.setText("-");
            labelSisa.setForeground(UITheme.TEXT);

            barAnggaran.setNilai(0);
            barAnggaran.setWarna(UITheme.PRIMARY);

            labelPersen.setText(
                "Atur batas harian lewat tombol Atur Anggaran."
            );

        } else {

            // Hitung sisa dan status berdasarkan tanggal hari ini.
            double sisa = manajer.getAnggaran()
                .hitungSisa(hariIni, terpakai);

            String status = manajer.getAnggaran()
                .cekStatus(hariIni, terpakai);

            double persen = (terpakai / batas) * 100;
double persenProgress = Math.min(persen, 100);

            labelSisa.setText(UITheme.formatRupiah(sisa));

            labelSisa.setForeground(
                sisa < 0
                    ? UITheme.DANGER_DARK
                    : UITheme.TEXT
            );

            barAnggaran.setNilai(
    (int) Math.round(persenProgress)
);

            labelPersen.setText(
                String.format(
                    Locale.US,
                    "%.0f%% dari batas harian terpakai",
                    persen
                )
            );

            badgeStatus.setText(status);

            if ("MELAMPAUI".equals(status)) {

                badgeStatus.setWarna(
                    UITheme.DANGER_DARK,
                    UITheme.DANGER_SOFT
                );

                barAnggaran.setWarna(UITheme.DANGER);

            } else if ("MENDEKATI".equals(status)) {

                badgeStatus.setWarna(
                    UITheme.WARNING_DARK,
                    UITheme.WARNING_SOFT
                );

                barAnggaran.setWarna(UITheme.WARNING);

            } else {

                badgeStatus.setWarna(
                    UITheme.SUCCESS_DARK,
                    UITheme.SUCCESS_SOFT
                );

                barAnggaran.setWarna(UITheme.SUCCESS);
            }
        }

        // ---------- Transaksi terakhir ----------
        List<Transaksi> urut = new ArrayList<>(semua);

        Collections.reverse(urut);

        // Tanggal terbaru ditampilkan terlebih dahulu.
        urut.sort((a, b) -> {

            LocalDate ta = a.getTanggal();
            LocalDate tb = b.getTanggal();

            if (ta == null && tb == null) {
                return 0;
            }

            if (ta == null) {
                return 1;
            }

            if (tb == null) {
                return -1;
            }

            return tb.compareTo(ta);
        });

        panelDaftar.removeAll();

        if (urut.isEmpty()) {

            labelJumlah.setText("Belum ada transaksi");

            JLabel kosong = UITheme.label(
                "Catat transaksi pertamamu lewat form di samping.",
                UITheme.font(Font.PLAIN, 13),
                UITheme.TEXT_MUTED
            );

            kosong.setBorder(new EmptyBorder(28, 0, 28, 0));
            kosong.setAlignmentX(Component.LEFT_ALIGNMENT);

            panelDaftar.add(kosong);

        } else {

            int tampil = Math.min(
                JUMLAH_TERAKHIR,
                urut.size()
            );

            labelJumlah.setText(
                tampil + " transaksi terbaru"
            );

            for (int i = 0; i < tampil; i++) {
                panelDaftar.add(
                    buatBarisTransaksi(
                        urut.get(i),
                        i == tampil - 1
                    )
                );
            }
        }

        panelDaftar.revalidate();
        panelDaftar.repaint();

        revalidate();
        repaint();
    }

    // ===================== PEMBUAT KOMPONEN =====================

    private JComponent buatBarisTransaksi(
        Transaksi t,
        boolean terakhir
    ) {

        boolean masuk = t.hitungDampakSaldo() > 0;

        String kategori = t.getKategori() == null
            ? "-"
            : t.getKategori();

        String tanggal = t.getTanggal() == null
            ? "-"
            : t.getTanggal().format(FORMAT_TANGGAL);

        String catatan = t.getCatatan() == null
            ? ""
            : t.getCatatan().trim();

        if (catatan.length() > 26) {
            catatan = catatan.substring(0, 25) + "...";
        }

        String keterangan = catatan.isEmpty()
            ? tanggal
            : tanggal + " - " + catatan;

        JLabel labelKategori = UITheme.label(
            kategori,
            UITheme.font(Font.BOLD, 13),
            UITheme.TEXT
        );

        JLabel labelKeterangan = UITheme.label(
            keterangan,
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        labelKategori.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelKeterangan.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel teks = new JPanel();
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.setOpaque(false);

        teks.add(labelKategori);
        teks.add(Box.createVerticalStrut(2));
        teks.add(labelKeterangan);

        JLabel labelNominal = UITheme.label(
            (masuk ? "+ " : "- ")
                + UITheme.formatRupiah(t.getNominal()),
            UITheme.font(Font.BOLD, 13),
            masuk
                ? UITheme.SUCCESS_DARK
                : UITheme.DANGER_DARK
        );

        JPanel baris = new JPanel(new BorderLayout(12, 0));
        baris.setOpaque(false);
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);

        baris.setBorder(
            BorderFactory.createCompoundBorder(
                terakhir
                    ? BorderFactory.createEmptyBorder()
                    : BorderFactory.createMatteBorder(
                        0, 0, 1, 0, GARIS_HALUS
                    ),
                new EmptyBorder(10, 0, 10, 0)
            )
        );

        baris.add(
            new IkonBulat(masuk),
            BorderLayout.WEST
        );

        baris.add(teks, BorderLayout.CENTER);
        baris.add(labelNominal, BorderLayout.EAST);

        return baris;
    }

    private JComponent buatJudul(
        String judul,
        JLabel subjudul
    ) {

        JLabel a = UITheme.label(
            judul,
            UITheme.font(Font.BOLD, 16),
            UITheme.TEXT
        );

        a.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjudul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel kotak = new JPanel();
        kotak.setLayout(new BoxLayout(kotak, BoxLayout.Y_AXIS));
        kotak.setOpaque(false);

        kotak.add(a);
        kotak.add(Box.createVerticalStrut(3));
        kotak.add(subjudul);

        return kotak;
    }

    private JComponent buatBaris(
        String judul,
        JLabel labelNilai
    ) {

        JLabel labelJudul = UITheme.label(
            judul,
            UITheme.font(Font.PLAIN, 13),
            UITheme.TEXT_MUTED
        );

        JPanel baris = new JPanel(new BorderLayout());
        baris.setOpaque(false);

        baris.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                    0, 0, 1, 0, GARIS_HALUS
                ),
                new EmptyBorder(11, 0, 11, 0)
            )
        );

        baris.add(labelJudul, BorderLayout.WEST);
        baris.add(labelNilai, BorderLayout.EAST);

        return baris;
    }

    private JComponent garis() {
        JPanel garis = new JPanel();

        garis.setBackground(GARIS_HALUS);
        garis.setPreferredSize(new Dimension(1, 1));
        garis.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 1)
        );

        return garis;
    }

    private void tambah(
        JPanel induk,
        JComponent komponen,
        int jarak
    ) {

        komponen.setAlignmentX(Component.LEFT_ALIGNMENT);
        induk.add(komponen);

        if (jarak > 0) {
            induk.add(Box.createVerticalStrut(jarak));
        }
    }

    // ===================== KOMPONEN KECIL =====================

    /**
     * Lingkaran berlatar lembut dengan panah:
     * hijau untuk pemasukan dan merah untuk pengeluaran.
     */
    private static class IkonBulat extends JComponent {

        private final boolean masuk;

        IkonBulat(boolean masuk) {
            this.masuk = masuk;

            Dimension ukuran = new Dimension(40, 40);

            setPreferredSize(ukuran);
            setMinimumSize(ukuran);
            setMaximumSize(ukuran);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            UITheme.antialias(g2);

            g2.setColor(
                masuk
                    ? UITheme.SUCCESS_SOFT
                    : UITheme.DANGER_SOFT
            );

            g2.fillOval(
                0,
                0,
                getWidth(),
                getHeight()
            );

            new UITheme.VectorIcon(
                masuk
                    ? UITheme.IconType.INCOME
                    : UITheme.IconType.EXPENSE,
                18,
                masuk
                    ? UITheme.SUCCESS_DARK
                    : UITheme.DANGER_DARK
            ).paintIcon(
                this,
                g2,
                (getWidth() - 18) / 2,
                (getHeight() - 18) / 2
            );

            g2.dispose();
        }
    }
}
