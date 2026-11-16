package gui;

import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Jendela utama aplikasi MySaku.
 * Dikerjakan oleh: ALHIYA
 *
 * Layout: sidebar navigasi di kiri, header dan halaman di kanan.
 * Halaman: Dashboard (ringkasan), Transaksi Baru (form), Riwayat (tabel).
 */
public class MySakuGUI extends JFrame {

    private static final String HALAMAN_DASHBOARD = "dashboard";
    private static final String HALAMAN_FORM = "form";
    private static final String HALAMAN_RIWAYAT = "riwayat";

    private static final int LEBAR_SIDEBAR = 250;

    private ManajerMySaku manajer;

    private FormTransaksiPanel formPanel;
    private TabelTransaksiPanel tabelPanel;
    private RingkasanPanel ringkasanPanel;

    private CardLayout cardLayout;
    private JPanel areaHalaman;
    private JLabel labelJudul;
    private JLabel labelSubjudul;
    private ArrayList<NavItem> daftarNav = new ArrayList<>();

    public MySakuGUI(ManajerMySaku manajer) {
        this.manajer = manajer;

        setTitle("MySaku - Pencatat Keuangan Mahasiswa");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 0));

        // Membuat komponen panel
        formPanel = new FormTransaksiPanel(manajer);
        tabelPanel = new TabelTransaksiPanel(manajer);
        ringkasanPanel = new RingkasanPanel(manajer);

        // Setelah transaksi disimpan, diubah, atau dihapus,
        // perbarui tabel dan ringkasan
        formPanel.setOnTransaksiDisimpan(() -> {
            tabelPanel.refreshTable();
            ringkasanPanel.refreshRingkasan();
        });

        // Saat baris tabel dipilih untuk diubah, isi form lalu
        // pindah ke halaman form
        tabelPanel.setOnUbahTransaksi(transaksi -> {
            formPanel.isiUntukUbah(transaksi);
            tampilkanHalaman(HALAMAN_FORM);
        });

        add(buatSidebar(), BorderLayout.WEST);
        add(buatAreaKanan(), BorderLayout.CENTER);

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
        areaHalaman.setBorder(new EmptyBorder(24, 28, 24, 28));

        areaHalaman.add(buatHalamanDashboard(), HALAMAN_DASHBOARD);
        areaHalaman.add(buatHalamanForm(), HALAMAN_FORM);
        areaHalaman.add(buatHalamanRiwayat(), HALAMAN_RIWAYAT);

        kanan.add(areaHalaman, BorderLayout.CENTER);
        return kanan;
    }

    /**
     * Header putih: judul halaman di kiri, tanggal hari ini di kanan.
     */
    private JPanel buatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(18, 28, 18, 28)
        ));

        labelJudul = UITheme.label(
            "", UITheme.font(Font.BOLD, 22), UITheme.TEXT
        );
        labelSubjudul = UITheme.label(
            "", UITheme.font(Font.PLAIN, 13), UITheme.TEXT_MUTED
        );
        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelSubjudul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel teks = new JPanel();
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.setOpaque(false);
        teks.add(labelJudul);
        teks.add(Box.createVerticalStrut(3));
        teks.add(labelSubjudul);

        String tanggal = LocalDate.now().format(
            DateTimeFormatter.ofPattern(
                "EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID")
            )
        );
        UITheme.Badge pilTanggal = new UITheme.Badge(
            tanggal, UITheme.PRIMARY_DARK, UITheme.PRIMARY_SOFT
        );
        pilTanggal.setFont(UITheme.font(Font.BOLD, 12));
        pilTanggal.setBorder(new EmptyBorder(8, 16, 8, 16));

        JPanel sisiKanan = new JPanel(new GridBagLayout());
        sisiKanan.setOpaque(false);
        sisiKanan.add(pilTanggal);

        header.add(teks, BorderLayout.WEST);
        header.add(sisiKanan, BorderLayout.EAST);
        return header;
    }

    // ===================== HALAMAN =====================

    private JComponent buatHalamanDashboard() {
        // Panel menentukan tinggi sendiri, ditempel di bagian atas halaman
        ringkasanPanel.setOpaque(false);

        JPanel halaman = new JPanel(new GridBagLayout());
        halaman.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        halaman.add(ringkasanPanel, gbc);
        return halaman;
    }

    private JComponent buatHalamanForm() {
        // Form dibuat selebar 540 px dan diletakkan di tengah atas halaman
        int tinggi = formPanel.getPreferredSize().height;
        formPanel.setPreferredSize(new Dimension(540, tinggi));

        JPanel pembungkus = new JPanel(new GridBagLayout());
        pembungkus.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        pembungkus.add(formPanel, gbc);

        // Scroll muncul otomatis jika jendela dikecilkan
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

    private JComponent buatHalamanRiwayat() {
        JPanel halaman = new JPanel(new BorderLayout());
        halaman.setOpaque(false);
        halaman.add(tabelPanel, BorderLayout.CENTER);
        return halaman;
    }

    // ===================== NAVIGASI =====================

    private void tampilkanHalaman(String kunci) {
        cardLayout.show(areaHalaman, kunci);

        for (NavItem item : daftarNav) {
            item.setAktif(item.kunci.equals(kunci));
        }

        switch (kunci) {
            case HALAMAN_FORM:
                labelJudul.setText("Transaksi Baru");
                labelSubjudul.setText(
                    "Catat pemasukan atau pengeluaranmu hari ini"
                );
                break;
            case HALAMAN_RIWAYAT:
                labelJudul.setText("Riwayat Transaksi");
                labelSubjudul.setText(
                    "Lihat dan cari seluruh catatan keuanganmu"
                );
                break;
            default:
                labelJudul.setText("Dashboard");
                labelSubjudul.setText(
                    "Ringkasan keuangan dan anggaran harianmu"
                );
                ringkasanPanel.refreshRingkasan();
                break;
        }
    }

    private void bukaDialogAnggaran() {
        AnggaranDialog dialog = new AnggaranDialog(this, manajer);
        dialog.setVisible(true);

        ringkasanPanel.refreshRingkasan();
    }

    // ===================== SIDEBAR =====================

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(
                    0, 0, UITheme.PRIMARY_DARK,
                    0, getHeight(), new Color(23, 44, 108)
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(LEBAR_SIDEBAR, 0));
        sidebar.setBorder(new EmptyBorder(26, 16, 20, 16));

        sidebar.add(buatLogo());
        sidebar.add(Box.createVerticalStrut(34));

        JLabel labelMenu = UITheme.label(
            "MENU", UITheme.font(Font.BOLD, 11), new Color(147, 170, 226)
        );
        labelMenu.setBorder(new EmptyBorder(0, 12, 0, 0));
        labelMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(labelMenu);
        sidebar.add(Box.createVerticalStrut(10));

        tambahNav(sidebar, "Dashboard",
            UITheme.IconType.DASHBOARD, HALAMAN_DASHBOARD);
        tambahNav(sidebar, "Transaksi Baru",
            UITheme.IconType.ADD, HALAMAN_FORM);
        tambahNav(sidebar, "Riwayat",
            UITheme.IconType.HISTORY, HALAMAN_RIWAYAT);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buatKartuAnggaran());

        return sidebar;
    }

    private JPanel buatLogo() {
        JPanel logo = new JPanel(new BorderLayout(12, 0));
        logo.setOpaque(false);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel nama = UITheme.label(
            "MySaku", UITheme.font(Font.BOLD, 20), Color.WHITE
        );
        JLabel tagline = UITheme.label(
            "Keuangan Mahasiswa",
            UITheme.font(Font.PLAIN, 11),
            new Color(191, 210, 255)
        );
        nama.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel teks = new JPanel();
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.setOpaque(false);
        teks.add(Box.createVerticalGlue());
        teks.add(nama);
        teks.add(tagline);
        teks.add(Box.createVerticalGlue());

        logo.add(new LogoMark(), BorderLayout.WEST);
        logo.add(teks, BorderLayout.CENTER);
        return logo;
    }

    private void tambahNav(
        JPanel sidebar,
        String teks,
        UITheme.IconType ikon,
        String kunci
    ) {
        NavItem item = new NavItem(teks, ikon, kunci);
        daftarNav.add(item);
        sidebar.add(item);
        sidebar.add(Box.createVerticalStrut(6));
    }

    /**
     * Kartu di dasar sidebar untuk membuka dialog anggaran.
     */
    private JComponent buatKartuAnggaran() {
        UITheme.RoundedPanel kartu = new UITheme.RoundedPanel(
            18, new Color(255, 255, 255, 30)
        );
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBorder(new EmptyBorder(16, 16, 16, 16));
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ikon = new JLabel(
            new UITheme.VectorIcon(
                UITheme.IconType.BUDGET, 24, Color.WHITE
            )
        );
        ikon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel judul = UITheme.label(
            "Atur Anggaran", UITheme.font(Font.BOLD, 14), Color.WHITE
        );
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel deskripsi = UITheme.label(
            "<html><div style='width:150px'>"
                + "Tetapkan batas harian agar pengeluaranmu tetap terkendali."
                + "</div></html>",
            UITheme.font(Font.PLAIN, 12),
            new Color(205, 220, 255)
        );
        deskripsi.setAlignmentX(Component.LEFT_ALIGNMENT);

        UITheme.ModernButton tombol = new UITheme.ModernButton(
            "Atur Anggaran", Color.WHITE, UITheme.ModernButton.FILLED
        );
        tombol.setForeground(UITheme.PRIMARY_DARK);
        tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tombol.addActionListener(e -> bukaDialogAnggaran());

        kartu.add(ikon);
        kartu.add(Box.createVerticalStrut(10));
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(deskripsi);
        kartu.add(Box.createVerticalStrut(14));
        kartu.add(tombol);

        // Cegah kartu melar saat sidebar lebih tinggi dari kontennya
        kartu.setMaximumSize(new Dimension(
            Integer.MAX_VALUE, kartu.getPreferredSize().height
        ));
        return kartu;
    }

    // ===================== KOMPONEN KECIL =====================

    /**
     * Item menu sidebar: kapsul biru saat aktif, transparan saat hover.
     */
    private class NavItem extends JButton {

        private final String kunci;
        private final String teks;
        private final UITheme.IconType ikon;
        private boolean aktif = false;
        private boolean hover = false;

        NavItem(String teks, UITheme.IconType ikon, String kunci) {
            this.teks = teks;
            this.ikon = ikon;
            this.kunci = kunci;

            getAccessibleContext().setAccessibleName(teks);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(200, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

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

            addActionListener(e -> tampilkanHalaman(kunci));
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
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 26));
                g2.fillRoundRect(0, 0, w, h, 14, 14);
            }

            Color warna = aktif ? Color.WHITE : new Color(199, 214, 247);

            new UITheme.VectorIcon(ikon, 20, warna)
                .paintIcon(this, g2, 16, (h - 20) / 2);

            g2.setFont(UITheme.font(
                aktif ? Font.BOLD : Font.PLAIN, 14
            ));
            g2.setColor(warna);
            FontMetrics fm = g2.getFontMetrics();
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(teks, 48, y);

            g2.dispose();
        }
    }

    /**
     * Logo kotak membulat dengan ikon dompet.
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

            g2.setPaint(new GradientPaint(
                0, 0, UITheme.PRIMARY,
                getWidth(), getHeight(), new Color(96, 165, 250)
            ));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

            new UITheme.VectorIcon(
                UITheme.IconType.BALANCE, 24, Color.WHITE
            ).paintIcon(
                this, g2,
                (getWidth() - 24) / 2, (getHeight() - 24) / 2
            );

            g2.dispose();
        }
    }
}