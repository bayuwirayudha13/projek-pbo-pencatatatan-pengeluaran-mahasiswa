
package gui;
import javax.swing.plaf.basic.BasicComboBoxUI;
import analisis.AnalisisKeuangan;
import model.Transaksi;
import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

/**
 * Panel ringkasan keuangan (Dashboard).
 * Dikerjakan oleh: ALHIYA
 *
 * Isi: kartu statistik, grafik arus kas (per tanggal atau per minggu
 * dalam satu bulan), donat kebutuhan vs keinginan, kategori teratas,
 * dan kalender yang tanggalnya bisa diklik.
 * Panel ini membungkus isinya dengan scroll sendiri.
 */
public class RingkasanPanel extends JPanel {

    private static final String[] NAMA_BULAN = {
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    };

    private static final Color ACCENT_SOFT = new Color(254, 236, 232);
    private static final Color GARIS_HALUS = new Color(241, 239, 252);

    private static final Color[] WARNA_KATEGORI = {
        UITheme.PRIMARY,
        UITheme.ACCENT,
        new Color(56, 189, 248),
        UITheme.WARNING,
        UITheme.PRIMARY_LIGHT
    };

    private static final int JUMLAH_KATEGORI = 5;
    private static final int JARAK_BARIS = 24;

    private ManajerMySaku manajer;
    private AnalisisKeuangan analisis;

    // Bulan dan mode yang ditampilkan di grafik
    private YearMonth bulanTampil = YearMonth.now();
    private boolean modePerMinggu = false;

    // Kartu statistik
    private LabelAngka angkaPemasukan;
    private LabelAngka angkaPengeluaran;
    private LabelAngka angkaSaldo;
    private LabelAngka angkaSisa;
    private UITheme.Badge badgeStatus;
    private JLabel labelTerpakai;
    private UITheme.RoundedProgressBar barAnggaran;

    // Grafik batang
    private UITheme.BarChart grafikBulanan;
    private JLabel subjudulGrafik;
    private JLabel labelBulan;

    // Donat
    private UITheme.DonutChart donut;
    private JLabel labelKebutuhan;
    private JLabel labelKeinginan;

    // Kategori dan kalender
    private JPanel panelKategori;
    private UITheme.MiniCalendar kalender;
    private JComboBox<String> filterPeriodeKategori;
    private String periodeKategori = "Bulanan";

    public RingkasanPanel(ManajerMySaku manajer) {
        this.manajer = manajer;
        this.analisis = new AnalisisKeuangan();

        setOpaque(false);
        setLayout(new BorderLayout());

        IsiHalaman isi = new IsiHalaman();
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setOpaque(false);
        isi.setBorder(new EmptyBorder(0, 0, 8, 0));

        tambahBaris(isi, buatBarisStatistik(), JARAK_BARIS);
        tambahBaris(isi, buatBarisGrafik(), JARAK_BARIS);
        tambahBaris(isi, buatBarisBawah(), 0);

        JScrollPane scroll = new JScrollPane(
            isi,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getVerticalScrollBar().setUnitIncrement(24);

        add(scroll, BorderLayout.CENTER);

        refreshRingkasan();
    }

    // ===================== BARIS 1: STATISTIK =====================

    private JComponent buatBarisStatistik() {
        angkaPemasukan = new LabelAngka(UITheme.SUCCESS_DARK);
        angkaPengeluaran = new LabelAngka(UITheme.DANGER_DARK);
        angkaSaldo = new LabelAngka(UITheme.PRIMARY_DARK);
        angkaSisa = new LabelAngka(UITheme.PRIMARY_DARK);

        badgeStatus = new UITheme.Badge(
            "BELUM DIATUR", UITheme.TEXT_MUTED, UITheme.BORDER
        );
        badgeStatus.setBorder(new EmptyBorder(4, 8, 4, 8));

        labelTerpakai = UITheme.label(
            " ", UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );

        barAnggaran = new UITheme.RoundedProgressBar();

        JPanel baris = new JPanel(new GridLayout(1, 4, 16, 0));
        baris.setOpaque(false);

        baris.add(kartuStatistik(
            UITheme.IconType.INCOME,
            UITheme.SUCCESS_DARK, UITheme.SUCCESS_SOFT,
            null,
            angkaPemasukan,
            "Total Pemasukan",
            UITheme.label(
                "Seluruh uang masuk",
                UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
            ),
            null
        ));

        baris.add(kartuStatistik(
            UITheme.IconType.EXPENSE,
            UITheme.DANGER_DARK, UITheme.DANGER_SOFT,
            null,
            angkaPengeluaran,
            "Total Pengeluaran",
            UITheme.label(
                "Seluruh uang keluar",
                UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
            ),
            null
        ));

        baris.add(kartuStatistik(
            UITheme.IconType.BALANCE,
            UITheme.PRIMARY, UITheme.PRIMARY_SOFT,
            null,
            angkaSaldo,
            "Saldo Akhir",
            UITheme.label(
                "Pemasukan - pengeluaran",
                UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
            ),
            null
        ));

        baris.add(kartuStatistik(
            UITheme.IconType.BUDGET,
            UITheme.ACCENT, ACCENT_SOFT,
            badgeStatus,
            angkaSisa,
            "Sisa Anggaran Hari Ini",
            labelTerpakai,
            barAnggaran
        ));

        return baris;
    }

    private JComponent kartuStatistik(
        UITheme.IconType ikon,
        Color warna,
        Color latar,
        JComponent kananAtas,
        LabelAngka angka,
        String judul,
        JLabel subteks,
        JComponent tambahan
    ) {
        UITheme.RoundedPanel kartu = kartuDasar(18, 18);
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JPanel atas = new JPanel(new BorderLayout(8, 0));
        atas.setOpaque(false);
        atas.add(new IconBox(ikon, warna, latar), BorderLayout.WEST);
        if (kananAtas != null) {
            JPanel pembungkus = new JPanel(new GridBagLayout());
            pembungkus.setOpaque(false);
            pembungkus.add(kananAtas);
            atas.add(pembungkus, BorderLayout.EAST);
        }
        atas.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        angka.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelJudul = UITheme.label(
            judul, UITheme.font(Font.BOLD, 13), UITheme.TEXT_MUTED
        );
        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        subteks.setAlignmentX(Component.LEFT_ALIGNMENT);

        kartu.add(atas);
        kartu.add(Box.createVerticalStrut(14));
        kartu.add(angka);
        kartu.add(Box.createVerticalStrut(2));
        kartu.add(labelJudul);
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(subteks);

        if (tambahan != null) {
            tambahan.setAlignmentX(Component.LEFT_ALIGNMENT);
            kartu.add(Box.createVerticalStrut(10));
            kartu.add(tambahan);
        }

        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    // ===================== BARIS 2: GRAFIK DAN DONAT =====================

    private JComponent buatBarisGrafik() {
        return barisProporsional(
            new JComponent[]{buatKartuGrafik(), buatKartuDonut()},
            new int[]{3, 2},
            390
        );
    }

    private JComponent buatKartuGrafik() {
        UITheme.RoundedPanel kartu = kartuDasar(20, 22);
        kartu.setLayout(new BorderLayout(0, 8));

        // ---------- Judul ----------
        JLabel judul = UITheme.label(
            "Statistik Keuangan", UITheme.font(Font.BOLD, 16), UITheme.TEXT
        );
        subjudulGrafik = UITheme.label(
            " ", UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjudulGrafik.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel teks = new JPanel();
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.setOpaque(false);
        teks.add(judul);
        teks.add(Box.createVerticalStrut(3));
        teks.add(subjudulGrafik);

        // ---------- Toggle mode ----------
        ToggleKecil toggleMode = new ToggleKecil(
            new String[]{"Per Tanggal", "Per Minggu"},
            indeks -> {
                modePerMinggu = indeks == 1;
                perbaruiKeteranganGrafik();
                refreshGrafik(manajer.getSemuaTransaksi());
            }
        );

        // ---------- Pindah bulan ----------
        TombolPanah tombolKiri = new TombolPanah(true, "Bulan sebelumnya");
        TombolPanah tombolKanan = new TombolPanah(false, "Bulan berikutnya");
        tombolKiri.addActionListener(e -> gantiBulan(-1));
        tombolKanan.addActionListener(e -> gantiBulan(1));

        labelBulan = UITheme.label(
            " ", UITheme.font(Font.BOLD, 13), UITheme.PRIMARY
        );
        labelBulan.setHorizontalAlignment(SwingConstants.CENTER);
        labelBulan.setPreferredSize(new Dimension(130, 28));

        JPanel navigasi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        navigasi.setOpaque(false);
        navigasi.add(tombolKiri);
        navigasi.add(labelBulan);
        navigasi.add(tombolKanan);

        JPanel kontrol = new JPanel(new BorderLayout());
        kontrol.setOpaque(false);
        kontrol.add(toggleMode, BorderLayout.WEST);
        kontrol.add(navigasi, BorderLayout.EAST);

        JPanel kepala = new JPanel(new BorderLayout(0, 12));
        kepala.setOpaque(false);
        kepala.add(teks, BorderLayout.NORTH);
        kepala.add(kontrol, BorderLayout.CENTER);

        // ---------- Grafik ----------
        grafikBulanan = new UITheme.BarChart();
        grafikBulanan.setPesanKosong("Belum ada transaksi di bulan ini");

        perbaruiKeteranganGrafik();

        kartu.add(kepala, BorderLayout.NORTH);
        kartu.add(grafikBulanan, BorderLayout.CENTER);
        return kartu;
    }

    private JComponent buatKartuDonut() {
        UITheme.RoundedPanel kartu = kartuDasar(20, 22);
        kartu.setLayout(new BorderLayout(0, 8));

        donut = new UITheme.DonutChart();

        labelKebutuhan = UITheme.label(
            "-", UITheme.font(Font.BOLD, 13), UITheme.TEXT
        );
        labelKeinginan = UITheme.label(
            "-", UITheme.font(Font.BOLD, 13), UITheme.TEXT
        );

        JPanel legenda = new JPanel();
        legenda.setLayout(new BoxLayout(legenda, BoxLayout.Y_AXIS));
        legenda.setOpaque(false);
        legenda.add(buatBarisLegenda(
            UITheme.PRIMARY, "Kebutuhan", labelKebutuhan
        ));
        legenda.add(Box.createVerticalStrut(8));
        legenda.add(buatBarisLegenda(
            UITheme.ACCENT, "Keinginan", labelKeinginan
        ));

        JPanel tengah = new JPanel(new BorderLayout(0, 8));
        tengah.setOpaque(false);
        tengah.add(donut, BorderLayout.CENTER);
        tengah.add(legenda, BorderLayout.SOUTH);

        kartu.add(
            buatKepala(
                "Kebutuhan vs Keinginan", "Dari seluruh pengeluaranmu"
            ),
            BorderLayout.NORTH
        );
        kartu.add(tengah, BorderLayout.CENTER);
        return kartu;
    }

    private JComponent buatBarisLegenda(
        Color warna, String nama, JLabel labelNilai
    ) {
        JLabel labelNama = UITheme.label(
            nama, UITheme.font(Font.PLAIN, 13), UITheme.TEXT_MUTED
        );

        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        kiri.setOpaque(false);
        kiri.add(new Titik(warna));
        kiri.add(labelNama);

        JPanel baris = new JPanel(new BorderLayout());
        baris.setOpaque(false);
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        baris.add(kiri, BorderLayout.WEST);
        baris.add(labelNilai, BorderLayout.EAST);
        return baris;
    }

    // ===================== BARIS 3: KATEGORI DAN KALENDER =====================

    private JComponent buatBarisBawah() {
        return barisProporsional(
            new JComponent[]{buatKartuKategori(), buatKartuKalender()},
            new int[]{3, 2},
            340
        );
    }

private JComponent buatKartuKategori() {
    UITheme.RoundedPanel kartu = kartuDasar(20, 22);
    kartu.setLayout(new BorderLayout(0, 14));

    panelKategori = new JPanel();
    panelKategori.setLayout(
        new BoxLayout(panelKategori, BoxLayout.Y_AXIS)
    );
    panelKategori.setOpaque(false);

    JPanel pembungkus = new JPanel(new BorderLayout());
    pembungkus.setOpaque(false);
    pembungkus.add(panelKategori, BorderLayout.NORTH);

    // Header kategori
    JPanel kepala = new JPanel(new BorderLayout(8, 8));
    kepala.setOpaque(false);

    kepala.add(
        buatKepala(
            "Kategori Teratas",
            "Pengeluaran terbesar berdasarkan kategori"
        ),
        BorderLayout.CENTER
    );

    // Dropdown periode
    
filterPeriodeKategori = new JComboBox<>(
    new String[]{"Harian", "Mingguan", "Bulanan"}
);

filterPeriodeKategori.setSelectedItem(periodeKategori);
filterPeriodeKategori.setFont(UITheme.font(Font.PLAIN, 12));
filterPeriodeKategori.setForeground(new Color(55, 42, 110));
filterPeriodeKategori.setBackground(Color.WHITE);
filterPeriodeKategori.setPreferredSize(new Dimension(125, 36));
filterPeriodeKategori.setFocusable(false);
filterPeriodeKategori.setOpaque(true);

// Paksa menggunakan tampilan Basic Swing
filterPeriodeKategori.setUI(new BasicComboBoxUI() {
    @Override
    protected JButton createArrowButton() {
        JButton tombol = new JButton("⌄");

        tombol.setFont(new Font("SansSerif", Font.BOLD, 16));
        tombol.setForeground(new Color(105, 83, 204));
        tombol.setBackground(Color.WHITE);
        tombol.setBorder(BorderFactory.createEmptyBorder());
        tombol.setFocusPainted(false);
        tombol.setContentAreaFilled(false);
        tombol.setOpaque(false);

        return tombol;
    }
});

// Border custom
filterPeriodeKategori.setBorder(
    BorderFactory.createLineBorder(
        new Color(222, 215, 250), 1
    )
);

// Renderer pilihan
filterPeriodeKategori.setRenderer(
    new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
            super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus
            );

            setFont(UITheme.font(Font.PLAIN, 12));
            setBorder(
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
            );

            setBackground(
                isSelected
                    ? new Color(239, 235, 255)
                    : Color.WHITE
            );

            setForeground(
                isSelected
                    ? new Color(91, 67, 190)
                    : new Color(55, 42, 110)
            );

            return this;
        }
    }
);

filterPeriodeKategori.addActionListener(e -> {
    periodeKategori =
        (String) filterPeriodeKategori.getSelectedItem();

    refreshKategori(manajer.getSemuaTransaksi());
});


    kepala.add(filterPeriodeKategori, BorderLayout.EAST);

    kartu.add(kepala, BorderLayout.NORTH);
    kartu.add(pembungkus, BorderLayout.CENTER);

    return kartu;
}

    private JComponent buatKartuKalender() {
        UITheme.RoundedPanel kartu = kartuDasar(20, 22);
        kartu.setLayout(new BorderLayout(0, 8));

        kalender = new UITheme.MiniCalendar();
        kalender.setOnTanggalDipilih(this::tampilkanTransaksiTanggal);

        kartu.add(
            buatKepala(
                "Kalender", "Klik tanggal untuk melihat transaksinya"
            ),
            BorderLayout.NORTH
        );
        kartu.add(kalender, BorderLayout.CENTER);
        return kartu;
    }

    private JComponent buatBarisKategori(
        String nama, double nilai, double persen, Color warna
    ) {
        JLabel labelNama = UITheme.label(
            nama, UITheme.font(Font.BOLD, 13), UITheme.TEXT
        );
        JLabel labelNilai = UITheme.label(
            UITheme.formatRupiah(nilai) + " ("
                + String.format(Locale.US, "%.0f%%", persen) + ")",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        JPanel atas = new JPanel(new BorderLayout(8, 0));
        atas.setOpaque(false);
        atas.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(labelNama, BorderLayout.WEST);
        atas.add(labelNilai, BorderLayout.EAST);

        UITheme.RoundedProgressBar bar = new UITheme.RoundedProgressBar();
        bar.setWarna(warna);
        bar.setNilai((int) Math.round(persen));
        bar.setPreferredSize(new Dimension(100, 8));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel baris = new JPanel();
        baris.setLayout(new BoxLayout(baris, BoxLayout.Y_AXIS));
        baris.setOpaque(false);
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        baris.add(atas);
        baris.add(Box.createVerticalStrut(6));
        baris.add(bar);
        baris.setMaximumSize(new Dimension(
            Integer.MAX_VALUE, baris.getPreferredSize().height
        ));
        return baris;
    }

    // ===================== PEMBANTU TATA LETAK =====================

    private UITheme.RoundedPanel kartuDasar(int vertikal, int horizontal) {
        UITheme.RoundedPanel kartu = new UITheme.RoundedPanel(
            18, UITheme.CARD, false
        );
        kartu.setBorder(
            new EmptyBorder(vertikal, horizontal, vertikal, horizontal)
        );
        return kartu;
    }

    private JComponent buatKepala(String judul, String subjudul) {
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

    /**
     * Baris kartu dengan lebar sebanding bobot dan tinggi tetap.
     */
    private JPanel barisProporsional(
        JComponent[] kartu, int[] bobot, int tinggi
    ) {
        JPanel baris = new JPanel(new GridBagLayout());
        baris.setOpaque(false);

        for (int i = 0; i < kartu.length; i++) {
            kartu[i].setPreferredSize(new Dimension(bobot[i] * 100, tinggi));
            kartu[i].setMinimumSize(new Dimension(0, tinggi));

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i;
            c.gridy = 0;
            c.weightx = bobot[i];
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(0, i == 0 ? 0 : 16, 0, 0);
            baris.add(kartu[i], c);
        }
        return baris;
    }

    private void tambahBaris(JPanel isi, JComponent baris, int jarak) {
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        baris.setMaximumSize(new Dimension(
            Integer.MAX_VALUE, baris.getPreferredSize().height
        ));
        isi.add(baris);
        if (jarak > 0) {
            isi.add(Box.createVerticalStrut(jarak));
        }
    }

    // ===================== GRAFIK: BULAN DAN MODE =====================

private void gantiBulan(int selisih) {
    bulanTampil = bulanTampil.plusMonths(selisih);

    perbaruiKeteranganGrafik();
    refreshRingkasan();
}

    private void perbaruiKeteranganGrafik() {
        subjudulGrafik.setText(
            modePerMinggu
                ? "Pemasukan vs pengeluaran per minggu"
                : "Pemasukan vs pengeluaran per tanggal"
        );

        String nama = bulanTampil.getMonth().getDisplayName(
            TextStyle.FULL, Locale.forLanguageTag("id-ID")
        );
        labelBulan.setText(
            nama.substring(0, 1).toUpperCase(Locale.forLanguageTag("id-ID"))
                + nama.substring(1) + " " + bulanTampil.getYear()
        );
    }

    /**
     * Mengisi grafik dengan transaksi pada bulan yang dipilih.
     * Per tanggal: satu kelompok batang per hari.
     * Per minggu: blok 7 hari dari tanggal 1 (1-7, 8-14, 15-21, ...).
     */
    private void refreshGrafik(List<Transaksi> daftar) {

        int jumlahHari = bulanTampil.lengthOfMonth();
        int jumlahGrup = modePerMinggu ? (jumlahHari + 6) / 7 : jumlahHari;

        double[] masuk = new double[jumlahGrup];
        double[] keluar = new double[jumlahGrup];

        for (Transaksi transaksi : daftar) {
            LocalDate tanggal = transaksi.getTanggal();
            if (tanggal == null
                || !YearMonth.from(tanggal).equals(bulanTampil)) {
                continue;
            }

            int hari = tanggal.getDayOfMonth();
            int indeks = modePerMinggu ? (hari - 1) / 7 : hari - 1;
            double dampak = transaksi.hitungDampakSaldo();

            if (dampak > 0) {
                masuk[indeks] += dampak;
            } else {
                keluar[indeks] += Math.abs(dampak);
            }
        }

        String[] label = new String[jumlahGrup];
        String[] judulTip = new String[jumlahGrup];
        String namaBulan = NAMA_BULAN[bulanTampil.getMonthValue() - 1];

        for (int i = 0; i < jumlahGrup; i++) {
            if (modePerMinggu) {
                int awal = i * 7 + 1;
                int akhir = Math.min(jumlahHari, awal + 6);

                label[i] = awal == akhir
                    ? String.valueOf(awal) : awal + "-" + akhir;
                judulTip[i] = "Minggu " + (i + 1) + " ("
                    + label[i] + " " + namaBulan + ")";
            } else {
                label[i] = String.valueOf(i + 1);
                judulTip[i] = (i + 1) + " " + namaBulan
                    + " " + bulanTampil.getYear();
            }
        }

        grafikBulanan.setData(label, judulTip, masuk, keluar);
    }

    // ===================== DIALOG TRANSAKSI PER TANGGAL =====================

    /**
     * Menampilkan transaksi pada tanggal yang diklik di kalender.
     */
    private void tampilkanTransaksiTanggal(LocalDate tanggal) {

        List<Transaksi> hariItu = new ArrayList<>();
        double totalMasuk = 0;
        double totalKeluar = 0;

        for (Transaksi transaksi : manajer.getSemuaTransaksi()) {
            if (tanggal.equals(transaksi.getTanggal())) {
                hariItu.add(transaksi);

                double dampak = transaksi.hitungDampakSaldo();
                if (dampak > 0) {
                    totalMasuk += dampak;
                } else {
                    totalKeluar += Math.abs(dampak);
                }
            }
        }

        String teksTanggal = tanggal.format(
            DateTimeFormatter.ofPattern(
                "EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID")
            )
        );

        Window induk = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(
            induk, "Transaksi " + teksTanggal,
            Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setResizable(false);

        JPanel isi = new JPanel(new BorderLayout(0, 18));
        isi.setBackground(Color.WHITE);
        isi.setBorder(new EmptyBorder(24, 26, 22, 26));
        dialog.setContentPane(isi);

        // ---------- Bagian atas ----------
        JLabel judul = UITheme.label(
            teksTanggal, UITheme.font(Font.BOLD, 18), UITheme.TEXT
        );
        JLabel subjudul = UITheme.label(
            hariItu.isEmpty()
                ? "Tidak ada transaksi pada tanggal ini"
                : hariItu.size() + " transaksi",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjudul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel atas = new JPanel();
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        atas.setOpaque(false);
        atas.add(judul);
        atas.add(Box.createVerticalStrut(3));
        atas.add(subjudul);

        if (!hariItu.isEmpty()) {
            JPanel ringkas = new JPanel(new GridLayout(1, 2, 12, 0));
            ringkas.setOpaque(false);
            ringkas.add(buatChip(
                "Pemasukan", totalMasuk,
                UITheme.SUCCESS_DARK, UITheme.SUCCESS_SOFT
            ));
            ringkas.add(buatChip(
                "Pengeluaran", totalKeluar,
                UITheme.DANGER_DARK, UITheme.DANGER_SOFT
            ));
            ringkas.setAlignmentX(Component.LEFT_ALIGNMENT);
            ringkas.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, ringkas.getPreferredSize().height
            ));

            atas.add(Box.createVerticalStrut(16));
            atas.add(ringkas);
        }

        // ---------- Bagian tengah ----------
        JComponent tengah;

        if (hariItu.isEmpty()) {
            JLabel kosong = UITheme.label(
                "Belum ada catatan di tanggal ini",
                UITheme.font(Font.PLAIN, 13),
                UITheme.TEXT_MUTED
            );
            kosong.setIcon(new UITheme.VectorIcon(
                UITheme.IconType.CALENDAR, 40, new Color(203, 199, 235)
            ));
            kosong.setHorizontalTextPosition(SwingConstants.CENTER);
            kosong.setVerticalTextPosition(SwingConstants.BOTTOM);
            kosong.setIconTextGap(12);

            JPanel pusat = new JPanel(new GridBagLayout());
            pusat.setOpaque(false);
            pusat.setPreferredSize(new Dimension(440, 150));
            pusat.add(kosong);
            tengah = pusat;
        } else {
            JPanel daftarPanel = new JPanel();
            daftarPanel.setLayout(
                new BoxLayout(daftarPanel, BoxLayout.Y_AXIS)
            );
            daftarPanel.setBackground(Color.WHITE);

            int tinggiTotal = 0;
            for (int i = 0; i < hariItu.size(); i++) {
                JComponent baris = buatBarisHari(
                    hariItu.get(i), i == hariItu.size() - 1
                );
                int tinggiBaris = baris.getPreferredSize().height;
                baris.setMaximumSize(new Dimension(
                    Integer.MAX_VALUE, tinggiBaris
                ));
                tinggiTotal += tinggiBaris;
                daftarPanel.add(baris);
            }

            JScrollPane scroll = new JScrollPane(daftarPanel);
            UITheme.styleScrollPane(scroll);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.setPreferredSize(new Dimension(
                440, Math.min(300, tinggiTotal + 4)
            ));
            tengah = scroll;
        }

        // ---------- Tombol ----------
        UITheme.ModernButton tombolTutup = new UITheme.ModernButton(
            "Tutup", UITheme.PRIMARY, UITheme.ModernButton.FILLED
        );
        tombolTutup.setPreferredSize(new Dimension(100, 44));
        tombolTutup.addActionListener(e -> dialog.dispose());

        isi.add(atas, BorderLayout.NORTH);
        isi.add(tengah, BorderLayout.CENTER);
        isi.add(tombolTutup, BorderLayout.SOUTH);

        // Esc menutup dialog
        isi.registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        dialog.getRootPane().setDefaultButton(tombolTutup);

        dialog.pack();
        dialog.setLocationRelativeTo(induk);
        dialog.setVisible(true);
    }

    private JComponent buatChip(
        String judul, double nilai, Color warna, Color latar
    ) {
        UITheme.RoundedPanel chip = new UITheme.RoundedPanel(14, latar);
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel a = UITheme.label(
            judul, UITheme.font(Font.PLAIN, 12), warna
        );
        JLabel b = UITheme.label(
            UITheme.formatRupiah(nilai), UITheme.font(Font.BOLD, 16), warna
        );
        a.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        chip.add(a);
        chip.add(Box.createVerticalStrut(2));
        chip.add(b);
        return chip;
    }

    private JComponent buatBarisHari(Transaksi t, boolean terakhir) {
        boolean masuk = t.hitungDampakSaldo() > 0;

        String kategori = t.getKategori() == null ? "-" : t.getKategori();
        String catatan = t.getCatatan() == null ? "" : t.getCatatan().trim();
        if (catatan.length() > 30) {
            catatan = catatan.substring(0, 29) + "...";
        }
        String keterangan = catatan.isEmpty()
            ? t.getId() : t.getId() + " - " + catatan;

        JLabel labelKategori = UITheme.label(
            kategori, UITheme.font(Font.BOLD, 13), UITheme.TEXT
        );
        JLabel labelKeterangan = UITheme.label(
            keterangan, UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
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
            (masuk ? "+ " : "- ") + UITheme.formatRupiah(t.getNominal()),
            UITheme.font(Font.BOLD, 13),
            masuk ? UITheme.SUCCESS_DARK : UITheme.DANGER_DARK
        );

        JPanel baris = new JPanel(new BorderLayout(12, 0));
        baris.setBackground(Color.WHITE);
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        baris.setBorder(BorderFactory.createCompoundBorder(
            terakhir
                ? BorderFactory.createEmptyBorder()
                : BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS_HALUS),
            new EmptyBorder(10, 0, 10, 0)
        ));

        baris.add(new IkonBulat(masuk), BorderLayout.WEST);
        baris.add(teks, BorderLayout.CENTER);
        baris.add(labelNominal, BorderLayout.EAST);
        return baris;
    }

    // ===================== PEMBARUAN DATA =====================

    /**
     * Memperbarui seluruh informasi keuangan.
     */
public void refreshRingkasan() {
    List<Transaksi> semuaTransaksi = manajer.getSemuaTransaksi();

    // Transaksi untuk statistik bulanan dan grafik
    List<Transaksi> daftar = semuaTransaksi.stream()
        .filter(transaksi -> transaksi.getTanggal() != null
            && YearMonth.from(transaksi.getTanggal()).equals(bulanTampil))
        .collect(Collectors.toList());

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

    angkaPemasukan.setNilai(
        UITheme.formatRupiah(totalPemasukan),
        UITheme.SUCCESS_DARK
    );

    angkaPengeluaran.setNilai(
        UITheme.formatRupiah(totalPengeluaran),
        UITheme.DANGER_DARK
    );

    angkaSaldo.setNilai(
        UITheme.formatRupiah(saldo),
        saldo < 0 ? UITheme.DANGER_DARK : UITheme.PRIMARY_DARK
    );

    // Anggaran menggunakan seluruh transaksi untuk tanggal hari ini
    refreshAnggaran(semuaTransaksi);

    // Bagian lain tetap mengikuti bulan yang dipilih
    refreshGrafik(daftar);
    refreshPrioritas(daftar);
    refreshKategori(daftar);

    // Kalender menggunakan seluruh transaksi
    refreshKalender(semuaTransaksi);

    revalidate();
    repaint();
}

   private void refreshAnggaran(List<Transaksi> semuaTransaksi) {
    LocalDate tanggalHariIni = LocalDate.now();

    double batas = manajer.getAnggaran()
        .getBatasHarian(tanggalHariIni);

    double terpakai = analisis.hitungPengeluaranHarian(
        semuaTransaksi,
        tanggalHariIni
    );

    if (batas <= 0) {
        badgeStatus.setText("BELUM DIATUR");
        badgeStatus.setWarna(UITheme.TEXT_MUTED, UITheme.BORDER);

        angkaSisa.setNilai("-", UITheme.TEXT_MUTED);
        labelTerpakai.setText("Belum ada batas harian");
        labelTerpakai.setToolTipText(
            "Atur lewat tombol Atur Anggaran di sidebar"
        );

        barAnggaran.setNilai(0);
        barAnggaran.setWarna(UITheme.PRIMARY);
        return;
    }

    double sisa = manajer.getAnggaran()
        .hitungSisa(tanggalHariIni, terpakai);

    String status = manajer.getAnggaran()
        .cekStatus(tanggalHariIni, terpakai);

    double persen = (terpakai / batas) * 100;

    angkaSisa.setNilai(
        UITheme.formatRupiah(sisa),
        sisa < 0 ? UITheme.DANGER_DARK : UITheme.PRIMARY_DARK
    );

    String ketTerpakai = "Terpakai "
        + UITheme.formatRupiah(terpakai)
        + " dari "
        + UITheme.formatRupiah(batas);

    labelTerpakai.setText(ketTerpakai);
    labelTerpakai.setToolTipText(ketTerpakai);

    barAnggaran.setNilai((int) Math.round(persen));
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
    private void refreshPrioritas(List<Transaksi> daftar) {

        double totalKebutuhan =
            analisis.hitungTotalPerPrioritas(daftar, "Kebutuhan");
        double persenKebutuhan =
            analisis.hitungPersentasePrioritas(daftar, "Kebutuhan");

        double totalKeinginan =
            analisis.hitungTotalPerPrioritas(daftar, "Keinginan");
        double persenKeinginan =
            analisis.hitungPersentasePrioritas(daftar, "Keinginan");

        donut.setData(
            new double[]{totalKebutuhan, totalKeinginan},
            new Color[]{UITheme.PRIMARY, UITheme.ACCENT}
        );

        if (totalKebutuhan + totalKeinginan > 0) {
            donut.setTeksTengah(
                String.format(Locale.US, "%.0f%%", persenKebutuhan),
                "Kebutuhan"
            );
        } else {
            donut.setTeksTengah("-", "Belum ada data");
        }

        labelKebutuhan.setText(
            UITheme.formatRupiah(totalKebutuhan) + " ("
                + String.format(Locale.US, "%.0f%%", persenKebutuhan) + ")"
        );
        labelKeinginan.setText(
            UITheme.formatRupiah(totalKeinginan) + " ("
                + String.format(Locale.US, "%.0f%%", persenKeinginan) + ")"
        );
    }

private void refreshKategori(List<Transaksi> daftar) {
    Map<String, Double> perKategori = new HashMap<>();
    double total = 0;

    LocalDate tanggalAwal;
    LocalDate tanggalAkhir;

    // Menentukan rentang tanggal berdasarkan periode kategori
    switch (periodeKategori) {
        case "Harian":
            tanggalAwal = bulanTampil.atDay(1);
            tanggalAkhir = bulanTampil.atEndOfMonth();
            break;

        case "Mingguan":
            tanggalAwal = bulanTampil.atDay(1);
            tanggalAkhir = bulanTampil.atEndOfMonth();
            break;

        default:
            tanggalAwal = bulanTampil.atDay(1);
            tanggalAkhir = bulanTampil.atEndOfMonth();
            break;
    }

    // Menghitung pengeluaran berdasarkan periode
    for (Transaksi transaksi : daftar) {
        LocalDate tanggal = transaksi.getTanggal();

        if (tanggal == null
                || tanggal.isBefore(tanggalAwal)
                || tanggal.isAfter(tanggalAkhir)) {
            continue;
        }

        double dampak = transaksi.hitungDampakSaldo();

        // Hanya menghitung transaksi pengeluaran
        if (dampak >= 0) {
            continue;
        }

        String kategori = transaksi.getKategori();

        if (kategori == null || kategori.trim().isEmpty()) {
            kategori = "Tanpa kategori";
        }

        double nilai = Math.abs(dampak);

        total += nilai;
        perKategori.merge(
            kategori.trim(), nilai, Double::sum
        );
    }

    // Mengurutkan kategori berdasarkan pengeluaran terbesar
    List<Map.Entry<String, Double>> urut =
        new ArrayList<>(perKategori.entrySet());

    urut.sort(
        (a, b) -> Double.compare(b.getValue(), a.getValue())
    );

    panelKategori.removeAll();

    if (urut.isEmpty()) {
        JLabel kosong = UITheme.label(
            "Belum ada pengeluaran pada periode ini.",
            UITheme.font(Font.PLAIN, 13),
            UITheme.TEXT_MUTED
        );

        kosong.setBorder(new EmptyBorder(30, 0, 0, 0));
        kosong.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelKategori.add(kosong);

    } else {
        int tampil = Math.min(JUMLAH_KATEGORI, urut.size());

        for (int i = 0; i < tampil; i++) {
            Map.Entry<String, Double> entri = urut.get(i);

            double persen = total > 0
                ? entri.getValue() / total * 100
                : 0;

            panelKategori.add(buatBarisKategori(
                entri.getKey(),
                entri.getValue(),
                persen,
                WARNA_KATEGORI[i % WARNA_KATEGORI.length]
            ));

            if (i < tampil - 1) {
                panelKategori.add(Box.createVerticalStrut(12));
            }
        }
    }

    panelKategori.revalidate();
    panelKategori.repaint();
}

    //
private void refreshKalender(List<Transaksi> daftar) {
    Set<LocalDate> tanggal = new HashSet<>();

    for (Transaksi transaksi : daftar) {
        if (transaksi.getTanggal() != null) {
            tanggal.add(transaksi.getTanggal());
        }
    }

    kalender.setTanggalBertanda(tanggal);
    kalender.setBulan(bulanTampil);
}

    // ===================== KOMPONEN KECIL =====================

    /**
     * Isi halaman yang mengikuti lebar viewport dan bisa di-scroll
     * ke bawah.
     */
    private static class IsiHalaman extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(
            Rectangle area, int orientation, int arah
        ) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(
            Rectangle area, int orientation, int arah
        ) {
            return Math.max(40, area.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * Angka besar yang mengecil otomatis jika kartu terlalu sempit.
     */
    private static class LabelAngka extends JComponent {

        private String teks = "Rp0";
        private Color warna;

        LabelAngka(Color warna) {
            this.warna = warna;
            setOpaque(false);
            setPreferredSize(new Dimension(10, 34));
            setMinimumSize(new Dimension(10, 34));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        }

        void setNilai(String teks, Color warna) {
            this.teks = teks;
            this.warna = warna;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.antialias(g2);

            int ukuran = 26;
            FontMetrics fm = null;
            for (; ukuran > 11; ukuran--) {
                fm = g2.getFontMetrics(UITheme.font(Font.BOLD, ukuran));
                if (fm.stringWidth(teks) <= getWidth()) {
                    break;
                }
            }

            g2.setFont(UITheme.font(Font.BOLD, ukuran));
            fm = g2.getFontMetrics();
            g2.setColor(warna);
            g2.drawString(
                teks, 0,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent()
            );

            g2.dispose();
        }
    }

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

    /**
     * Lingkaran berlatar lembut dengan panah: turun hijau untuk
     * pemasukan, naik merah untuk pengeluaran.
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

            g2.setColor(masuk ? UITheme.SUCCESS_SOFT : UITheme.DANGER_SOFT);
            g2.fillOval(0, 0, getWidth(), getHeight());

            new UITheme.VectorIcon(
                masuk ? UITheme.IconType.INCOME : UITheme.IconType.EXPENSE,
                18,
                masuk ? UITheme.SUCCESS_DARK : UITheme.DANGER_DARK
            ).paintIcon(
                this, g2,
                (getWidth() - 18) / 2, (getHeight() - 18) / 2
            );

            g2.dispose();
        }
    }

    /**
     * Titik warna kecil untuk legenda.
     */
    private static class Titik extends JComponent {

        private final Color warna;

        Titik(Color warna) {
            this.warna = warna;
            Dimension ukuran = new Dimension(10, 10);
            setPreferredSize(ukuran);
            setMinimumSize(ukuran);
            setMaximumSize(ukuran);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.antialias(g2);
            g2.setColor(warna);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /**
     * Tombol panah kecil untuk pindah bulan.
     */
    private static class TombolPanah extends JButton {

        private final boolean kiri;
        private boolean hover = false;

        TombolPanah(boolean kiri, String namaAksesibilitas) {
            this.kiri = kiri;

            getAccessibleContext().setAccessibleName(namaAksesibilitas);
            setPreferredSize(new Dimension(28, 28));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.antialias(g2);

            g2.setColor(hover
                ? UITheme.campur(UITheme.PRIMARY_SOFT, UITheme.PRIMARY, 0.18)
                : UITheme.PRIMARY_SOFT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g2.setColor(UITheme.PRIMARY);
            g2.setStroke(new BasicStroke(
                2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
            ));
            if (kiri) {
                g2.drawLine(cx + 2, cy - 5, cx - 3, cy);
                g2.drawLine(cx - 3, cy, cx + 2, cy + 5);
            } else {
                g2.drawLine(cx - 2, cy - 5, cx + 3, cy);
                g2.drawLine(cx + 3, cy, cx - 2, cy + 5);
            }

            g2.dispose();
        }
    }

    /**
     * Toggle dua pilihan berbentuk kapsul (Per Tanggal / Per Minggu).
     */
    private static class ToggleKecil extends JComponent {

        private final String[] pilihan;
        private int terpilih = 0;

        ToggleKecil(String[] pilihan, IntConsumer saatBerubah) {
            this.pilihan = pilihan;

            setPreferredSize(new Dimension(210, 34));
            setMinimumSize(new Dimension(160, 34));
            setMaximumSize(new Dimension(300, 34));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int indeks = Math.min(
                        pilihan.length - 1,
                        Math.max(0, (int) (e.getX() * pilihan.length
                            / (double) getWidth()))
                    );
                    if (indeks != terpilih) {
                        terpilih = indeks;
                        repaint();
                        saatBerubah.accept(indeks);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.antialias(g2);

            int w = getWidth();
            int h = getHeight();
            int pad = 3;
            int lebar = (w - pad * 2) / pilihan.length;

            g2.setColor(UITheme.PRIMARY_SOFT);
            g2.fillRoundRect(0, 0, w, h, 14, 14);

            g2.setPaint(new GradientPaint(
                0, 0, UITheme.PRIMARY, w, 0, UITheme.PRIMARY_LIGHT
            ));
            g2.fillRoundRect(
                pad + terpilih * lebar, pad, lebar, h - pad * 2, 11, 11
            );

            g2.setFont(UITheme.font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < pilihan.length; i++) {
                g2.setColor(i == terpilih
                    ? Color.WHITE : UITheme.TEXT_MUTED);
                g2.drawString(
                    pilihan[i],
                    pad + i * lebar
                        + (lebar - fm.stringWidth(pilihan[i])) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent()
                );
            }

            g2.dispose();
        }
    }
}