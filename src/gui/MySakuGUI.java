
package gui;

import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Jendela utama aplikasi MySaku.
 * Dikerjakan oleh: ALHIYA
 *
 * Layout:
 * Sidebar navigasi di kiri, header dan halaman di kanan.
 *
 * Halaman:
 * 1. Dashboard
 * 2. Transaksi Baru
 * 3. Riwayat
 * 4. Sampah
 * 5. Tabungan
 */
public class MySakuGUI extends JFrame {

    // ===================== KONSTANTA HALAMAN =====================

    private static final String HALAMAN_DASHBOARD = "dashboard";
    private static final String HALAMAN_FORM = "form";
    private static final String HALAMAN_RIWAYAT = "riwayat";
    private static final String HALAMAN_SAMPAH = "sampah";
    private static final String HALAMAN_TABUNGAN = "tabungan";

    private static final int LEBAR_SIDEBAR = 250;

    // ===================== DATA DAN PANEL =====================

    private ManajerMySaku manajer;

    private FormTransaksiPanel formPanel;
    private TabelTransaksiPanel tabelPanel;
    private SampahPanel sampahPanel;
    private RingkasanPanel ringkasanPanel;
    private PanelInfoTransaksi infoPanel;
    private TabunganPanel tabunganPanel;

    private CardLayout cardLayout;
    private JPanel areaHalaman;

    private JLabel labelJudul;
    private JLabel labelSubjudul;

    private ArrayList<NavItem> daftarNav = new ArrayList<>();

    // ===================== CONSTRUCTOR =====================

    public MySakuGUI(ManajerMySaku manajer) {
        this.manajer = manajer;

        setTitle("MySaku - Pencatat Keuangan Mahasiswa");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 0));

        // Membuat seluruh panel aplikasi
        formPanel = new FormTransaksiPanel(manajer);
        tabelPanel = new TabelTransaksiPanel(manajer);
        sampahPanel = new SampahPanel(manajer);
        ringkasanPanel = new RingkasanPanel(manajer);
        infoPanel = new PanelInfoTransaksi(manajer);

        // Panel khusus target tabungan
        tabunganPanel = new TabunganPanel(manajer);

        // Setelah transaksi disimpan, diubah, atau dihapus,
        // perbarui tabel, ringkasan, dan panel informasi
        formPanel.setOnTransaksiDisimpan(() -> {
            tabelPanel.refreshTable();
            ringkasanPanel.refreshRingkasan();
            infoPanel.refreshInfo();
        });

        // Saat transaksi dipilih untuk diubah,
        // isi form kemudian pindah ke halaman transaksi
        tabelPanel.setOnUbahTransaksi(transaksi -> {
            formPanel.isiUntukUbah(transaksi);
            tampilkanHalaman(HALAMAN_FORM);
        });

        // Tombol lihat semua pada panel informasi
        // membuka halaman riwayat transaksi
        infoPanel.setOnLihatSemua(
            () -> tampilkanHalaman(HALAMAN_RIWAYAT)
        );

        // Menambahkan sidebar dan area utama
        add(buatSidebar(), BorderLayout.WEST);
        add(buatAreaKanan(), BorderLayout.CENTER);

        // Halaman pertama yang ditampilkan
        tampilkanHalaman(HALAMAN_DASHBOARD);
    }

    // ===================== AREA KANAN =====================

    private JPanel buatAreaKanan() {
        JPanel kanan = new JPanel(new BorderLayout());
        kanan.setBackground(UITheme.BG);

        kanan.add(buatHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();

        areaHalaman = new JPanel(cardLayout);
        areaHalaman.setOpaque(false);
        areaHalaman.setBorder(
            new EmptyBorder(24, 28, 24, 28)
        );

        // Mendaftarkan seluruh halaman
        areaHalaman.add(
            buatHalamanDashboard(),
            HALAMAN_DASHBOARD
        );

        areaHalaman.add(
            buatHalamanForm(),
            HALAMAN_FORM
        );

        areaHalaman.add(
            buatHalamanRiwayat(),
            HALAMAN_RIWAYAT
        );

        areaHalaman.add(
            buatHalamanSampah(),
            HALAMAN_SAMPAH
        );

        areaHalaman.add(
            buatHalamanTabungan(),
            HALAMAN_TABUNGAN
        );

        kanan.add(areaHalaman, BorderLayout.CENTER);

        return kanan;
    }

    /**
     * Header putih berisi judul halaman dan tanggal hari ini.
     */
    private JPanel buatHeader() {
        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(Color.WHITE);
        header.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                    0, 0, 1, 0, UITheme.BORDER
                ),
                new EmptyBorder(18, 28, 18, 28)
            )
        );

        labelJudul = UITheme.label(
            "",
            UITheme.font(Font.BOLD, 22),
            UITheme.PRIMARY
        );

        labelSubjudul = UITheme.label(
            "",
            UITheme.font(Font.PLAIN, 13),
            UITheme.TEXT_MUTED
        );

        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelSubjudul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel teks = new JPanel();
        teks.setLayout(
            new BoxLayout(teks, BoxLayout.Y_AXIS)
        );
        teks.setOpaque(false);

        teks.add(labelJudul);
        teks.add(Box.createVerticalStrut(3));
        teks.add(labelSubjudul);

        String tanggal = LocalDate.now().format(
            DateTimeFormatter.ofPattern(
                "EEEE, d MMMM yyyy",
                Locale.forLanguageTag("id-ID")
            )
        );

        UITheme.Badge pilTanggal = new UITheme.Badge(
            tanggal,
            UITheme.PRIMARY,
            UITheme.PRIMARY_SOFT
        );

        pilTanggal.setFont(
            UITheme.font(Font.BOLD, 12)
        );

        pilTanggal.setBorder(
            new EmptyBorder(8, 16, 8, 16)
        );

        JPanel sisiKanan = new JPanel(
            new GridBagLayout()
        );

        sisiKanan.setOpaque(false);
        sisiKanan.add(pilTanggal);

        header.add(teks, BorderLayout.WEST);
        header.add(sisiKanan, BorderLayout.EAST);

        return header;
    }

    // ===================== HALAMAN DASHBOARD =====================

    private JComponent buatHalamanDashboard() {
        ringkasanPanel.setOpaque(false);

        JPanel halaman = new JPanel(
            new BorderLayout()
        );

        halaman.setOpaque(false);
        halaman.add(
            ringkasanPanel,
            BorderLayout.CENTER
        );

        return halaman;
    }

    // ===================== HALAMAN TRANSAKSI BARU =====================

    /**
     * Dua kolom:
     * Form transaksi di kiri dan panel informasi di kanan.
     */
    private JComponent buatHalamanForm() {
        int tinggi = formPanel.getPreferredSize().height;

        formPanel.setPreferredSize(
            new Dimension(540, tinggi)
        );

        formPanel.setMinimumSize(
            new Dimension(420, tinggi)
        );

        infoPanel.setMinimumSize(
            new Dimension(300, 0)
        );

        JPanel baris = new JPanel(
            new GridBagLayout()
        );

        baris.setOpaque(false);

        GridBagConstraints kiri = new GridBagConstraints();

        kiri.gridx = 0;
        kiri.gridy = 0;
        kiri.weightx = 0;
        kiri.weighty = 1;
        kiri.fill = GridBagConstraints.BOTH;
        kiri.insets = new Insets(0, 0, 0, 20);

        baris.add(formPanel, kiri);

        GridBagConstraints kanan = new GridBagConstraints();

        kanan.gridx = 1;
        kanan.gridy = 0;
        kanan.weightx = 1;
        kanan.weighty = 1;
        kanan.fill = GridBagConstraints.BOTH;

        baris.add(infoPanel, kanan);

        JPanel pembungkus = new JPanel(
            new BorderLayout()
        );

        pembungkus.setOpaque(false);
        pembungkus.add(baris, BorderLayout.NORTH);

        // Scroll muncul otomatis jika jendela dipendekkan
        JScrollPane scroll = new JScrollPane(pembungkus);

        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    // ===================== HALAMAN RIWAYAT =====================

    private JComponent buatHalamanRiwayat() {
        JPanel halaman = new JPanel(
            new BorderLayout()
        );

        halaman.setOpaque(false);
        halaman.add(
            tabelPanel,
            BorderLayout.CENTER
        );

        return halaman;
    }

    // ===================== HALAMAN SAMPAH =====================

    private JComponent buatHalamanSampah() {
        JPanel halaman = new JPanel(
            new BorderLayout()
        );

        halaman.setOpaque(false);
        halaman.add(
            sampahPanel,
            BorderLayout.CENTER
        );

        return halaman;
    }

    // ===================== HALAMAN TABUNGAN =====================

    private JComponent buatHalamanTabungan() {
        JPanel halaman = new JPanel(
            new BorderLayout()
        );

        halaman.setOpaque(false);
        halaman.add(
            tabunganPanel,
            BorderLayout.CENTER
        );

        return halaman;
    }

    // ===================== NAVIGASI HALAMAN =====================

    private void tampilkanHalaman(String kunci) {
        cardLayout.show(areaHalaman, kunci);

        // Memperbarui tampilan menu aktif
        for (NavItem item : daftarNav) {
            item.setAktif(
                item.kunci.equals(kunci)
            );
        }

        switch (kunci) {

            case HALAMAN_FORM:
                labelJudul.setText("Transaksi Baru");

                labelSubjudul.setText(
                    "Catat pemasukan atau pengeluaranmu hari ini"
                );

                infoPanel.refreshInfo();
                break;

            case HALAMAN_RIWAYAT:
                labelJudul.setText("Riwayat Transaksi");

                labelSubjudul.setText(
                    "Lihat dan cari seluruh catatan keuanganmu"
                );

                tabelPanel.refreshTable();
                ringkasanPanel.refreshRingkasan();
                break;

            case HALAMAN_SAMPAH:
                labelJudul.setText("Sampah");

                labelSubjudul.setText(
                    "Kelola transaksi yang telah kamu hapus"
                );

                sampahPanel.refreshTable();
                break;

            case HALAMAN_TABUNGAN:
                labelJudul.setText("Target Tabungan");

                labelSubjudul.setText(
                    "Rencanakan dan pantau progres tabunganmu"
                );

                tabunganPanel.refreshData();
                break;

            case HALAMAN_DASHBOARD:
            default:
                labelJudul.setText(
                    sapaan() + "!"
                );

                labelSubjudul.setText(
                    "Ini ringkasan keuangan dan anggaran harianmu"
                );

                ringkasanPanel.refreshRingkasan();
                break;
        }
    }

    // ===================== SAPAAN =====================

    private String sapaan() {
        int jam = LocalTime.now().getHour();

        if (jam < 11) {
            return "Selamat pagi";
        }

        if (jam < 15) {
            return "Selamat siang";
        }

        if (jam < 18) {
            return "Selamat sore";
        }

        return "Selamat malam";
    }

    // ===================== DIALOG ANGGARAN =====================

    private void bukaDialogAnggaran() {
        AnggaranDialog dialog = new AnggaranDialog(
            this,
            manajer
        );

        dialog.setVisible(true);

        ringkasanPanel.refreshRingkasan();
        infoPanel.refreshInfo();
    }

    // ===================== SIDEBAR =====================

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setPaint(
                    new GradientPaint(
                        0, 0,
                        UITheme.SIDEBAR_TOP,
                        0, getHeight(),
                        UITheme.SIDEBAR_BOTTOM
                    )
                );

                g2.fillRect(
                    0, 0,
                    getWidth(),
                    getHeight()
                );

                g2.dispose();
            }
        };

        sidebar.setLayout(
            new BoxLayout(sidebar, BoxLayout.Y_AXIS)
        );

        sidebar.setPreferredSize(
            new Dimension(LEBAR_SIDEBAR, 0)
        );

        sidebar.setBorder(
            new EmptyBorder(26, 16, 20, 16)
        );

        sidebar.add(buatLogo());
        sidebar.add(Box.createVerticalStrut(34));

        JLabel labelMenu = UITheme.label(
            "MENU",
            UITheme.font(Font.BOLD, 11),
            UITheme.SIDEBAR_MUTED
        );

        labelMenu.setBorder(
            new EmptyBorder(0, 12, 0, 0)
        );

        labelMenu.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        sidebar.add(labelMenu);
        sidebar.add(Box.createVerticalStrut(10));

        // Menu Dashboard
        tambahNav(
            sidebar,
            "Dashboard",
            UITheme.IconType.DASHBOARD,
            HALAMAN_DASHBOARD
        );

        // Menu Transaksi Baru
        tambahNav(
            sidebar,
            "Transaksi Baru",
            UITheme.IconType.ADD,
            HALAMAN_FORM
        );

        // Menu Riwayat
        tambahNav(
            sidebar,
            "Riwayat",
            UITheme.IconType.HISTORY,
            HALAMAN_RIWAYAT
        );

        // Menu Sampah
        tambahNav(
            sidebar,
            "Sampah",
            UITheme.IconType.TRASH,
            HALAMAN_SAMPAH
        );

        // Menu Tabungan
        tambahNav(
            sidebar,
            "Tabungan",
            UITheme.IconType.BALANCE,
            HALAMAN_TABUNGAN
        );

        sidebar.add(Box.createVerticalGlue());

        // Kartu pengaturan anggaran tetap dipertahankan
        sidebar.add(buatKartuAnggaran());

        return sidebar;
    }

    // ===================== LOGO =====================

    private JPanel buatLogo() {
        JPanel logo = new JPanel(
            new BorderLayout(12, 0)
        );

        logo.setOpaque(false);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        logo.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );

        JLabel nama = UITheme.label(
            "MySaku",
            UITheme.font(Font.BOLD, 20),
            Color.WHITE
        );

        JLabel tagline = UITheme.label(
            "Keuangan Mahasiswa",
            UITheme.font(Font.PLAIN, 11),
            UITheme.SIDEBAR_TEXT
        );

        nama.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel teks = new JPanel();

        teks.setLayout(
            new BoxLayout(teks, BoxLayout.Y_AXIS)
        );

        teks.setOpaque(false);

        teks.add(Box.createVerticalGlue());
        teks.add(nama);
        teks.add(tagline);
        teks.add(Box.createVerticalGlue());

        logo.add(
            new LogoMark(),
            BorderLayout.WEST
        );

        logo.add(
            teks,
            BorderLayout.CENTER
        );

        return logo;
    }

    // ===================== TAMBAH NAVIGASI =====================

    private void tambahNav(
        JPanel sidebar,
        String teks,
        UITheme.IconType ikon,
        String kunci
    ) {
        NavItem item = new NavItem(
            teks,
            ikon,
            kunci
        );

        daftarNav.add(item);

        sidebar.add(item);
        sidebar.add(Box.createVerticalStrut(6));
    }

    // ===================== KARTU ANGGARAN =====================

    /**
     * Kartu di bagian bawah sidebar untuk membuka dialog anggaran.
     */
    private JComponent buatKartuAnggaran() {
        UITheme.RoundedPanel kartu =
            new UITheme.RoundedPanel(
                18,
                new Color(255, 255, 255, 28)
            );

        kartu.setLayout(
            new BoxLayout(kartu, BoxLayout.Y_AXIS)
        );

        kartu.setBorder(
            new EmptyBorder(16, 16, 16, 16)
        );

        kartu.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        JLabel ikon = new JLabel(
            new UITheme.VectorIcon(
                UITheme.IconType.BUDGET,
                24,
                Color.WHITE
            )
        );

        ikon.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        JLabel judul = UITheme.label(
            "Atur Anggaran",
            UITheme.font(Font.BOLD, 14),
            Color.WHITE
        );

        judul.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        JLabel deskripsi = UITheme.label(
            "<html><div style='width:150px'>"
                + "Tetapkan batas harian agar pengeluaranmu "
                + "tetap terkendali."
                + "</div></html>",
            UITheme.font(Font.PLAIN, 12),
            UITheme.SIDEBAR_TEXT
        );

        deskripsi.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        UITheme.ModernButton tombol =
            new UITheme.ModernButton(
                "Atur Anggaran",
                Color.WHITE,
                UITheme.ModernButton.FILLED
            );

        tombol.setForeground(
            UITheme.PRIMARY_DARK
        );

        tombol.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        tombol.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 40)
        );

        tombol.addActionListener(
            e -> bukaDialogAnggaran()
        );

        kartu.add(ikon);
        kartu.add(Box.createVerticalStrut(10));
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(deskripsi);
        kartu.add(Box.createVerticalStrut(14));
        kartu.add(tombol);

        kartu.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                kartu.getPreferredSize().height
            )
        );

        return kartu;
    }

    // ===================== NAV ITEM =====================

    /**
     * Item menu sidebar:
     * Gradien ungu saat aktif dan transparan saat hover.
     */
    private class NavItem extends JButton {

        private final String kunci;
        private final String teks;
        private final UITheme.IconType ikon;

        private boolean aktif = false;
        private boolean hover = false;

        NavItem(
            String teks,
            UITheme.IconType ikon,
            String kunci
        ) {
            this.teks = teks;
            this.ikon = ikon;
            this.kunci = kunci;

            getAccessibleContext().setAccessibleName(teks);

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(
                new Cursor(Cursor.HAND_CURSOR)
            );

            setAlignmentX(
                Component.LEFT_ALIGNMENT
            );

            setPreferredSize(
                new Dimension(200, 46)
            );

            setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 46)
            );

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

            addActionListener(
                e -> tampilkanHalaman(kunci)
            );
        }

        void setAktif(boolean aktif) {
            this.aktif = aktif;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            UITheme.antialias(g2);

            int w = getWidth();
            int h = getHeight();

            if (aktif) {
                g2.setPaint(
                    new GradientPaint(
                        0, 0,
                        UITheme.PRIMARY,
                        w, 0,
                        UITheme.PRIMARY_LIGHT
                    )
                );

                g2.fillRoundRect(
                    0, 0,
                    w, h,
                    14, 14
                );

            } else if (hover) {
                g2.setColor(
                    new Color(255, 255, 255, 24)
                );

                g2.fillRoundRect(
                    0, 0,
                    w, h,
                    14, 14
                );
            }

            Color warna = aktif
                ? Color.WHITE
                : UITheme.SIDEBAR_TEXT;

            new UITheme.VectorIcon(
                ikon,
                20,
                warna
            ).paintIcon(
                this,
                g2,
                16,
                (h - 20) / 2
            );

            g2.setFont(
                UITheme.font(
                    aktif ? Font.BOLD : Font.PLAIN,
                    14
                )
            );

            g2.setColor(warna);

            FontMetrics fm = g2.getFontMetrics();

            int y = (h - fm.getHeight()) / 2
                + fm.getAscent();

            g2.drawString(
                teks,
                48,
                y
            );

            g2.dispose();
        }
    }

    // ===================== LOGO MARK =====================

    /**
     * Logo kotak membulat dengan ikon aplikasi.
     */
    private static class LogoMark extends JComponent {

        LogoMark() {
            Dimension ukuran = new Dimension(44, 44);

            setPreferredSize(ukuran);
            setMinimumSize(ukuran);
            setMaximumSize(ukuran);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            UITheme.antialias(g2);

            g2.setPaint(
                new GradientPaint(
                    0, 0,
                    UITheme.PRIMARY,
                    getWidth(),
                    getHeight(),
                    UITheme.PRIMARY_LIGHT
                )
            );

            g2.fillRoundRect(
                0, 0,
                getWidth(),
                getHeight(),
                14, 14
            );

            new UITheme.VectorIcon(
                UITheme.IconType.BALANCE,
                24,
                Color.WHITE
            ).paintIcon(
                this,
                g2,
                (getWidth() - 24) / 2,
                (getHeight() - 24) / 2
            );

            g2.dispose();
        }
    }
}
