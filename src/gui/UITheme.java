package gui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tema visual bersama untuk seluruh GUI MySaku.
 * Berisi warna, font, dan komponen kustom (kartu, tombol, badge, ikon,
 * grafik batang, grafik donat, dan kalender mini).
 * Hanya menyentuh tampilan; tidak berhubungan dengan logika aplikasi.
 */
public final class UITheme {

    private UITheme() {
    }

    // ===================== WARNA =====================
    public static final Color PRIMARY_DARK = new Color(37, 34, 95);
    public static final Color PRIMARY = new Color(96, 78, 201);
    public static final Color PRIMARY_LIGHT = new Color(142, 124, 224);
    public static final Color PRIMARY_SOFT = new Color(237, 233, 254);
    public static final Color ACCENT = new Color(244, 114, 94);

    public static final Color BG = new Color(244, 242, 253);
    public static final Color CARD = Color.WHITE;
    public static final Color TABLE_HEAD = new Color(238, 235, 252);
    public static final Color TABLE_ZEBRA = new Color(250, 249, 254);

    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color SUCCESS_DARK = new Color(22, 163, 74);
    public static final Color SUCCESS_SOFT = new Color(220, 252, 231);

    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color WARNING_DARK = new Color(180, 110, 5);
    public static final Color WARNING_SOFT = new Color(254, 243, 199);

    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color DANGER_DARK = new Color(220, 38, 38);
    public static final Color DANGER_SOFT = new Color(254, 226, 226);

    public static final Color TEXT = new Color(30, 27, 75);
    public static final Color TEXT_MUTED = new Color(110, 107, 140);
    public static final Color BORDER = new Color(228, 225, 245);

    // Warna khusus sidebar
    public static final Color SIDEBAR_TOP = PRIMARY_DARK;
    public static final Color SIDEBAR_BOTTOM = new Color(27, 24, 78);
    public static final Color SIDEBAR_TEXT = new Color(203, 199, 240);
    public static final Color SIDEBAR_MUTED = new Color(148, 143, 205);

    // Warna berurutan untuk grafik dengan banyak bagian
    public static final Color[] CHART_COLORS = {
        PRIMARY, ACCENT, SUCCESS, WARNING,
        new Color(56, 189, 248), DANGER
    };

    // ===================== FONT =====================
    private static final String FAMILY = pilihFamily();

    private static String pilihFamily() {
        String[] pilihan = {
            "Poppins", "Inter", "Segoe UI", "SF Pro Text",
            "Noto Sans", "Ubuntu", "SansSerif"
        };
        Set<String> tersedia = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()
        ));
        for (String nama : pilihan) {
            if (tersedia.contains(nama)) {
                return nama;
            }
        }
        return "SansSerif";
    }

    public static Font font(int style, int size) {
        return new Font(FAMILY, style, size);
    }

    // ===================== HELPER UMUM =====================
    public static String formatRupiah(double nominal) {
        NumberFormat format = NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("id-ID")
        );
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format.format(nominal);
    }

    /**
     * Format ringkas untuk sumbu grafik: 50 rb, 1,5 jt, 2 M.
     */
    public static String formatSingkat(double nilai) {
        double abs = Math.abs(nilai);
        if (abs >= 1_000_000_000d) {
            return rapikan(nilai / 1_000_000_000d) + " M";
        }
        if (abs >= 1_000_000d) {
            return rapikan(nilai / 1_000_000d) + " jt";
        }
        if (abs >= 1_000d) {
            return rapikan(nilai / 1_000d) + " rb";
        }
        return rapikan(nilai);
    }

    private static String rapikan(double x) {
        if (Math.abs(x - Math.rint(x)) < 0.05) {
            return String.valueOf((long) Math.rint(x));
        }
        return String.format(Locale.forLanguageTag("id-ID"), "%.1f", x);
    }

    private static String kapital(String teks) {
        if (teks == null || teks.isEmpty()) {
            return "";
        }
        return teks.substring(0, 1).toUpperCase(Locale.forLanguageTag("id-ID"))
            + teks.substring(1);
    }

    public static void antialias(Graphics2D g2) {
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        g2.setRenderingHint(
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_PURE
        );
    }

    public static JLabel label(String teks, Font font, Color warna) {
        JLabel label = new JLabel(teks);
        label.setFont(font);
        label.setForeground(warna);
        return label;
    }

    public static Color gelapkan(Color c, int jumlah) {
        return new Color(
            Math.max(0, c.getRed() - jumlah),
            Math.max(0, c.getGreen() - jumlah),
            Math.max(0, c.getBlue() - jumlah)
        );
    }

    /**
     * Mencampur dua warna. t = 0 menghasilkan a, t = 1 menghasilkan b.
     */
    public static Color campur(Color a, Color b, double t) {
        double k = Math.max(0, Math.min(1, t));
        return new Color(
            (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * k),
            (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k),
            (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k)
        );
    }

    // ===================== KARTU MEMBULAT =====================
    /**
     * Panel putih bersudut membulat dengan bayangan halus.
     * Padding isi diatur lewat setBorder(new EmptyBorder(...)) seperti biasa;
     * ruang untuk bayangan ditambahkan otomatis.
     */
    public static class RoundedPanel extends JPanel {

        private static final int SIDE = 6;
        private static final int TOP = 4;
        private static final int BOTTOM = 9;

        private int arc;
        private Color fill;
        private boolean shadow;
        private Color borderColor;

        public RoundedPanel(int arc, Color fill, boolean shadow) {
            this.arc = arc;
            this.fill = fill;
            this.shadow = shadow;
            setOpaque(false);
        }

        public RoundedPanel(int arc, Color fill) {
            this(arc, fill, false);
        }

        public void setFill(Color fill) {
            this.fill = fill;
            repaint();
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        @Override
        public Insets getInsets() {
            Insets i = super.getInsets();
            if (!shadow) {
                return i;
            }
            return new Insets(
                i.top + TOP, i.left + SIDE,
                i.bottom + BOTTOM, i.right + SIDE
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int x = shadow ? SIDE : 0;
            int y = shadow ? TOP : 0;
            int w = getWidth() - (shadow ? SIDE * 2 : 0);
            int h = getHeight() - (shadow ? TOP + BOTTOM : 0);

            if (shadow) {
                for (int i = SIDE; i >= 1; i--) {
                    g2.setColor(new Color(38, 33, 100, 7));
                    g2.fill(new RoundRectangle2D.Float(
                        x - i, y - i + 3, w + i * 2, h + i * 2,
                        arc + i * 2, arc + i * 2
                    ));
                }
            }

            g2.setColor(fill);
            g2.fillRoundRect(x, y, w, h, arc, arc);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===================== TOMBOL MODERN =====================
    /**
     * Tombol bersudut membulat dengan efek hover dan tekan.
     */
    public static class ModernButton extends JButton {

        public static final int FILLED = 0;
        public static final int OUTLINE = 1;
        public static final int SOFT = 2;

        private Color base;
        private int gaya;
        private int arc = 12;
        private boolean hover = false;
        private boolean pressed = false;

        public ModernButton(String teks, Color base, int gaya) {
            super(teks);
            this.base = base;
            this.gaya = gaya;

            setFont(font(Font.BOLD, 13));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));
            terapkanWarnaTeks();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        public void setBase(Color base) {
            this.base = base;
            terapkanWarnaTeks();
            repaint();
        }

        public void setArc(int arc) {
            this.arc = arc;
            repaint();
        }

        private void terapkanWarnaTeks() {
            if (gaya == FILLED) {
                setForeground(Color.WHITE);
            } else {
                setForeground(base);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int w = getWidth();
            int h = getHeight();

            if (gaya == FILLED) {
                Color c = base;
                if (pressed) {
                    c = gelapkan(base, 35);
                } else if (hover) {
                    c = gelapkan(base, 18);
                }
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            } else if (gaya == SOFT) {
                Color soft = new Color(
                    base.getRed(), base.getGreen(), base.getBlue(),
                    hover ? 40 : 22
                );
                g2.setColor(soft);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            } else {
                g2.setColor(hover
                    ? new Color(base.getRed(), base.getGreen(),
                        base.getBlue(), 18)
                    : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(hover ? base : BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===================== BADGE =====================
    /**
     * Label berbentuk kapsul untuk status (AMAN, Pemasukan, dst).
     */
    public static class Badge extends JLabel {

        private Color latar;

        public Badge(String teks, Color teksWarna, Color latar) {
            super(teks);
            this.latar = latar;
            setForeground(teksWarna);
            setFont(font(Font.BOLD, 11));
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(4, 12, 4, 12));
        }

        public void setWarna(Color teksWarna, Color latar) {
            this.latar = latar;
            setForeground(teksWarna);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            g2.setColor(latar);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===================== PROGRESS BAR MEMBULAT =====================
    public static class RoundedProgressBar extends JComponent {

        private int nilai = 0;
        private Color warna = PRIMARY;

        public RoundedProgressBar() {
            setPreferredSize(new Dimension(100, 10));
            setMinimumSize(new Dimension(20, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
            setOpaque(false);
        }

        public void setNilai(int nilai) {
            this.nilai = Math.max(0, Math.min(100, nilai));
            repaint();
        }

        public void setWarna(Color warna) {
            this.warna = warna;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            int h = getHeight();
            int w = getWidth();

            g2.setColor(new Color(233, 230, 250));
            g2.fillRoundRect(0, 0, w, h, h, h);

            int isi = (int) (w * (nilai / 100.0));
            if (isi > 0) {
                g2.setColor(warna);
                g2.fillRoundRect(0, 0, Math.max(isi, h), h, h, h);
            }
            g2.dispose();
        }
    }

    // ===================== BORDER MEMBULAT UNTUK INPUT =====================
    public static class RoundedBorder extends AbstractBorder {

        private Color warna;
        private int arc;
        private int padV;
        private int padH;

        public RoundedBorder(Color warna, int arc, int padV, int padH) {
            this.warna = warna;
            this.arc = arc;
            this.padV = padV;
            this.padH = padH;
        }

        @Override
        public void paintBorder(Component c, Graphics g,
                                int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            g2.setColor(warna);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(padV, padH, padV, padH);
        }
    }

    // ===================== STYLING INPUT =====================
    public static void styleField(final JTextField field) {
        field.setFont(font(Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setCaretColor(PRIMARY);

        final RoundedBorder normal = new RoundedBorder(BORDER, 12, 8, 12);
        final RoundedBorder fokus = new RoundedBorder(PRIMARY, 12, 8, 12);
        field.setBorder(normal);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(fokus);
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(normal);
                field.repaint();
            }
        });

        field.setPreferredSize(new Dimension(200, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    public static void styleCombo(JComboBox<String> combo) {
        combo.setUI(new ModernComboUI());
        combo.setFont(font(Font.PLAIN, 13));
        combo.setForeground(TEXT);
        combo.setBackground(Color.WHITE);
        combo.setBorder(new RoundedBorder(BORDER, 12, 2, 4));
        combo.setFocusable(true);
        combo.setPreferredSize(new Dimension(200, 40));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                );
                setBorder(new EmptyBorder(8, 10, 8, 10));
                setFont(font(Font.PLAIN, 13));
                setForeground(TEXT);
                setBackground(isSelected && index >= 0
                    ? PRIMARY_SOFT : Color.WHITE);
                return this;
            }
        });
    }

    private static class ModernComboUI extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton tombol = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    antialias(g2);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.setColor(TEXT_MUTED);
                    g2.setStroke(new BasicStroke(
                        1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
                    ));
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            tombol.setBorder(new EmptyBorder(0, 0, 0, 0));
            tombol.setContentAreaFilled(false);
            tombol.setFocusPainted(false);
            tombol.setOpaque(false);
            return tombol;
        }

        @Override
        public void paintCurrentValueBackground(
            Graphics g, Rectangle bounds, boolean hasFocus
        ) {
            g.setColor(Color.WHITE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    // ===================== SCROLLBAR TIPIS =====================
    public static void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ThinScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 9));
    }

    private static class ThinScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(209, 205, 235);
            trackColor = Color.WHITE;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return tombolNol();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return tombolNol();
        }

        private JButton tombolNol() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(Color.WHITE);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 8, 8);
            g2.dispose();
        }
    }
    // ===================== GRAFIK BATANG =====================
    /**
     * Grafik batang berpasangan (dua seri per kelompok), misalnya
     * pemasukan dan pengeluaran per tanggal. Arahkan kursor ke sebuah
     * kelompok untuk melihat nilainya. Label sumbu X otomatis dijarangkan
     * jika terlalu rapat.
     */
    public static class BarChart extends JComponent {

        private String[] label = new String[0];
        private String[] judulTip = new String[0];
        private double[] seriA = new double[0];
        private double[] seriB = new double[0];
        private String namaA = "Pemasukan";
        private String namaB = "Pengeluaran";
        private Color warnaA = SUCCESS;
        private Color warnaB = DANGER;
        private String pesanKosong = "Belum ada data";
        private int hover = -1;

        // Hasil perhitungan tata letak terakhir (dipakai untuk mouse)
        private int areaX = 0;
        private int areaY = 0;
        private int areaW = 1;
        private int areaH = 1;
        private double lebarGrup = 1;

        public BarChart() {
            setPreferredSize(new Dimension(420, 270));
            setMinimumSize(new Dimension(240, 180));
            setOpaque(false);

            MouseAdapter pendengar = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int indeks = indeksDi(e.getX(), e.getY());
                    if (indeks != hover) {
                        hover = indeks;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (hover != -1) {
                        hover = -1;
                        repaint();
                    }
                }
            };
            addMouseListener(pendengar);
            addMouseMotionListener(pendengar);
        }

        public void setData(String[] label, double[] seriA, double[] seriB) {
            setData(label, label, seriA, seriB);
        }

        /**
         * judulTip = judul yang tampil di tooltip (boleh lebih panjang
         * dari label sumbu X).
         */
        public void setData(
            String[] label, String[] judulTip,
            double[] seriA, double[] seriB
        ) {
            this.label = label == null ? new String[0] : label.clone();
            this.judulTip = judulTip == null
                ? this.label : judulTip.clone();
            this.seriA = seriA == null ? new double[0] : seriA.clone();
            this.seriB = seriB == null ? new double[0] : seriB.clone();
            this.hover = -1;
            repaint();
        }

        public void setSeri(
            String namaA, Color warnaA, String namaB, Color warnaB
        ) {
            this.namaA = namaA;
            this.warnaA = warnaA;
            this.namaB = namaB;
            this.warnaB = warnaB;
            repaint();
        }

        public void setPesanKosong(String pesan) {
            this.pesanKosong = pesan;
            repaint();
        }

        private int indeksDi(int x, int y) {
            if (label.length == 0) {
                return -1;
            }
            if (x < areaX || x >= areaX + areaW
                || y < areaY || y > areaY + areaH + 20) {
                return -1;
            }
            int indeks = (int) ((x - areaX) / lebarGrup);
            return Math.min(label.length - 1, Math.max(0, indeks));
        }

        private static double nilai(double[] data, int i) {
            return i < data.length ? Math.max(0, data[i]) : 0;
        }

        private static double puncakRapi(double maks) {
            if (maks <= 0) {
                return 100000;
            }
            double pangkat = Math.pow(10, Math.floor(Math.log10(maks)));
            double f = maks / pangkat;
            double nf;
            if (f <= 1) {
                nf = 1;
            } else if (f <= 2) {
                nf = 2;
            } else if (f <= 4) {
                nf = 4;
            } else if (f <= 8) {
                nf = 8;
            } else {
                nf = 10;
            }
            return nf * pangkat;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int w = getWidth();
            int h = getHeight();
            int n = label.length;

            double maks = 0;
            for (int i = 0; i < n; i++) {
                maks = Math.max(maks,
                    Math.max(nilai(seriA, i), nilai(seriB, i)));
            }
            boolean kosong = maks <= 0;
            double puncak = puncakRapi(maks);
            int jumlahGaris = 4;

            g2.setFont(font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            int atas = 30;
            int bawah = 26;
            areaX = fm.stringWidth(formatSingkat(puncak)) + 14;
            areaY = atas;
            areaW = Math.max(10, w - areaX - 6);
            areaH = Math.max(10, h - atas - bawah);
            lebarGrup = n == 0 ? areaW : (double) areaW / n;

            // Jarak antar label sumbu X agar tidak bertumpuk
            int lebarLabelMaks = 0;
            for (String teks : label) {
                lebarLabelMaks = Math.max(
                    lebarLabelMaks, fm.stringWidth(teks)
                );
            }
            int langkah = 1;
            while (langkah < n
                && lebarGrup * langkah < lebarLabelMaks + 8) {
                langkah++;
            }

            // ---------- Legenda (kanan atas) ----------
            String[] namaLegenda = {namaB, namaA};
            Color[] warnaLegenda = {warnaB, warnaA};
            int xr = w - 4;
            for (int k = 0; k < 2; k++) {
                xr -= fm.stringWidth(namaLegenda[k]);
                g2.setColor(TEXT_MUTED);
                g2.drawString(namaLegenda[k], xr, 14);
                xr -= 14;
                g2.setColor(warnaLegenda[k]);
                g2.fillOval(xr, 5, 8, 8);
                xr -= 14;
            }

            // ---------- Garis bantu dan label sumbu Y ----------
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= jumlahGaris; i++) {
                int y = areaY + areaH - areaH * i / jumlahGaris;
                g2.setColor(new Color(233, 230, 250));
                g2.drawLine(areaX, y, areaX + areaW, y);

                String teks = formatSingkat(puncak * i / jumlahGaris);
                g2.setColor(TEXT_MUTED);
                g2.drawString(
                    teks,
                    areaX - 8 - fm.stringWidth(teks),
                    y + (fm.getAscent() - fm.getDescent()) / 2
                );
            }

            // ---------- Sorotan kelompok yang ditunjuk ----------
            if (hover >= 0 && hover < n) {
                g2.setColor(new Color(237, 233, 254, 150));
                g2.fillRoundRect(
                    areaX + (int) (hover * lebarGrup) + 1, areaY,
                    (int) lebarGrup - 2, areaH, 10, 10
                );
            }

            // ---------- Batang dan label sumbu X ----------
            for (int i = 0; i < n; i++) {
                double tengah = areaX + lebarGrup * (i + 0.5);
                int lebarBatang = (int) Math.max(3,
                    Math.min(18, lebarGrup * 0.26));
                int jarak = 2;

                int xA = (int) (tengah - jarak / 2.0 - lebarBatang);
                int xB = (int) (tengah + jarak / 2.0);

                gambarBatang(g2, xA, lebarBatang, nilai(seriA, i),
                    puncak, warnaA);
                gambarBatang(g2, xB, lebarBatang, nilai(seriB, i),
                    puncak, warnaB);

                boolean tampilLabel = (i % langkah == 0) || i == hover;
                if (tampilLabel) {
                    g2.setFont(font(
                        i == hover ? Font.BOLD : Font.PLAIN, 11
                    ));
                    FontMetrics fmLabel = g2.getFontMetrics();
                    g2.setColor(i == hover ? TEXT : TEXT_MUTED);
                    g2.drawString(
                        label[i],
                        (int) (tengah
                            - fmLabel.stringWidth(label[i]) / 2.0),
                        areaY + areaH + 17
                    );
                }
            }

            // ---------- Pesan kosong ----------
            if (kosong) {
                g2.setFont(font(Font.PLAIN, 13));
                FontMetrics fmKosong = g2.getFontMetrics();
                g2.setColor(TEXT_MUTED);
                g2.drawString(
                    pesanKosong,
                    areaX + (areaW - fmKosong.stringWidth(pesanKosong)) / 2,
                    areaY + areaH / 2
                );
            }

            // ---------- Tooltip ----------
            if (hover >= 0 && hover < n) {
                gambarTooltip(g2, w, puncak);
            }

            g2.dispose();
        }

        private void gambarBatang(
            Graphics2D g2, int x, int lebar,
            double nilai, double puncak, Color warna
        ) {
            if (nilai <= 0) {
                return;
            }
            int tinggi = (int) Math.max(3,
                Math.round(areaH * (nilai / puncak)));
            int y = areaY + areaH - tinggi;

            // Sudut membulat hanya di bagian atas batang
            Graphics2D gb = (Graphics2D) g2.create();
            gb.clipRect(x, y, lebar, tinggi);
            gb.setPaint(new GradientPaint(
                0, y, campur(warna, Color.WHITE, 0.35),
                0, y + tinggi, warna
            ));
            gb.fill(new RoundRectangle2D.Double(
                x, y, lebar, tinggi + lebar, lebar, lebar
            ));
            gb.dispose();
        }

        private void gambarTooltip(Graphics2D g2, int lebarKomponen,
                                   double puncak) {
            String judul = hover < judulTip.length
                ? judulTip[hover] : label[hover];

            String[] baris = {
                judul,
                namaA + "  " + formatRupiah(nilai(seriA, hover)),
                namaB + "  " + formatRupiah(nilai(seriB, hover))
            };

            g2.setFont(font(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();

            int lebarTeks = 0;
            for (String b : baris) {
                lebarTeks = Math.max(lebarTeks, fm.stringWidth(b));
            }
            int lebarTip = lebarTeks + 34;
            int tinggiTip = baris.length * 18 + 14;

            double tinggiTerbesar = Math.max(
                nilai(seriA, hover), nilai(seriB, hover)
            );
            int yBatang = areaY + areaH
                - (int) (areaH * (tinggiTerbesar / puncak));

            int tx = (int) (areaX + lebarGrup * (hover + 0.5)
                - lebarTip / 2.0);
            tx = Math.max(2, Math.min(lebarKomponen - lebarTip - 2, tx));
            int ty = Math.max(2, yBatang - tinggiTip - 8);

            g2.setColor(PRIMARY_DARK);
            g2.fillRoundRect(tx, ty, lebarTip, tinggiTip, 12, 12);

            // Judul tooltip
            g2.setColor(Color.WHITE);
            g2.drawString(baris[0], tx + 12, ty + 7 + fm.getAscent());

            // Dua seri dengan titik warna
            Color[] titik = {warnaA, warnaB};
            for (int k = 1; k <= 2; k++) {
                int yBaris = ty + 7 + k * 18;
                g2.setColor(titik[k - 1]);
                g2.fillOval(tx + 12, yBaris + fm.getAscent() / 2 - 1, 7, 7);
                g2.setColor(Color.WHITE);
                g2.drawString(baris[k], tx + 24, yBaris + fm.getAscent());
            }
        }
    }

    // ===================== GRAFIK DONAT =====================
    /**
     * Grafik donat dengan teks di tengah. Legenda dibuat oleh panel
     * yang memakainya.
     */
    public static class DonutChart extends JComponent {

        private double[] nilai = new double[0];
        private Color[] warna = new Color[0];
        private String teksBesar = "";
        private String teksKecil = "";

        public DonutChart() {
            setPreferredSize(new Dimension(170, 170));
            setMinimumSize(new Dimension(110, 110));
            setOpaque(false);
        }

        public void setData(double[] nilai, Color[] warna) {
            this.nilai = nilai == null ? new double[0] : nilai.clone();
            this.warna = warna == null ? new Color[0] : warna.clone();
            repaint();
        }

        public void setTeksTengah(String besar, String kecil) {
            this.teksBesar = besar == null ? "" : besar;
            this.teksKecil = kecil == null ? "" : kecil;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int w = getWidth();
            int h = getHeight();
            int d = Math.min(w, h) - 8;
            if (d < 30) {
                g2.dispose();
                return;
            }

            float tebal = Math.max(10f, d * 0.15f);
            double diameter = d - tebal;
            double x = (w - diameter) / 2.0;
            double y = (h - diameter) / 2.0;

            // Lingkaran dasar
            g2.setStroke(new BasicStroke(tebal, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(233, 230, 250));
            g2.draw(new Ellipse2D.Double(x, y, diameter, diameter));

            double total = 0;
            int jumlahIsi = 0;
            for (double v : nilai) {
                if (v > 0) {
                    total += v;
                    jumlahIsi++;
                }
            }

            if (total > 0) {
                double mulai = 90;
                for (int i = 0; i < nilai.length; i++) {
                    if (nilai[i] <= 0) {
                        continue;
                    }
                    double sudut = -360.0 * nilai[i] / total;
                    double celah = (jumlahIsi > 1 && Math.abs(sudut) > 8)
                        ? 4 : 0;

                    g2.setColor(i < warna.length
                        ? warna[i] : CHART_COLORS[i % CHART_COLORS.length]);
                    g2.draw(new Arc2D.Double(
                        x, y, diameter, diameter,
                        mulai - celah / 2.0, sudut + celah, Arc2D.OPEN
                    ));
                    mulai += sudut;
                }
            }

            // Teks di tengah
            g2.setFont(font(Font.BOLD, 22));
            FontMetrics fmBesar = g2.getFontMetrics();
            g2.setFont(font(Font.PLAIN, 11));
            FontMetrics fmKecil = g2.getFontMetrics();

            int tinggiTotal = fmBesar.getHeight()
                + (teksKecil.isEmpty() ? 0 : fmKecil.getHeight());
            int yAtas = (h - tinggiTotal) / 2;

            g2.setFont(font(Font.BOLD, 22));
            g2.setColor(TEXT);
            g2.drawString(
                teksBesar,
                (w - fmBesar.stringWidth(teksBesar)) / 2,
                yAtas + fmBesar.getAscent()
            );

            if (!teksKecil.isEmpty()) {
                g2.setFont(font(Font.PLAIN, 11));
                g2.setColor(TEXT_MUTED);
                g2.drawString(
                    teksKecil,
                    (w - fmKecil.stringWidth(teksKecil)) / 2,
                    yAtas + fmBesar.getHeight() + fmKecil.getAscent()
                );
            }

            g2.dispose();
        }
    }

    // ===================== KALENDER MINI =====================
    /**
     * Kalender bulanan. Hari ini disorot dengan lingkaran, tanggal yang
     * punya transaksi diberi titik. Tanggal bisa diklik (lihat
     * setOnTanggalDipilih) dan tombol panah memindahkan bulan.
     */
    public static class MiniCalendar extends JComponent {

        private static final String[] HARI = {
            "Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"
        };
        private static final int TINGGI_KEPALA = 34;
        private static final int TINGGI_HARI = 24;

        private YearMonth bulan = YearMonth.now();
        private Set<LocalDate> bertanda = Collections.emptySet();
        private LocalDate tanggalHover = null;
        private java.util.function.Consumer<LocalDate> onTanggalDipilih;
        private final Rectangle tombolKiri = new Rectangle();
        private final Rectangle tombolKanan = new Rectangle();

        public MiniCalendar() {
            setPreferredSize(new Dimension(300, 250));
            setMinimumSize(new Dimension(220, 200));
            setOpaque(false);

            MouseAdapter pendengar = new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() != MouseEvent.BUTTON1) {
                        return;
                    }

                    if (tombolKiri.contains(e.getPoint())) {
                        bulan = bulan.minusMonths(1);
                        tanggalHover = null;
                        repaint();
                    } else if (tombolKanan.contains(e.getPoint())) {
                        bulan = bulan.plusMonths(1);
                        tanggalHover = null;
                        repaint();
                    } else {
                        LocalDate tanggal = tanggalDi(e.getX(), e.getY());
                        if (tanggal != null && onTanggalDipilih != null) {
                            onTanggalDipilih.accept(tanggal);
                        }
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    LocalDate baru = tanggalDi(e.getX(), e.getY());
                    boolean diPanah = tombolKiri.contains(e.getPoint())
                        || tombolKanan.contains(e.getPoint());

                    if (!java.util.Objects.equals(baru, tanggalHover)) {
                        tanggalHover = baru;
                        repaint();
                    }

                    setCursor(new Cursor(
                        diPanah || baru != null
                            ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (tanggalHover != null) {
                        tanggalHover = null;
                        repaint();
                    }
                }
            };
            addMouseListener(pendengar);
            addMouseMotionListener(pendengar);
        }

        public void setTanggalBertanda(Set<LocalDate> tanggal) {
            this.bertanda = tanggal == null
                ? Collections.<LocalDate>emptySet() : tanggal;
            repaint();
        }

        /**
         * Aksi saat sebuah tanggal diklik (tanggal dengan atau tanpa titik).
         */
        public void setOnTanggalDipilih(
            java.util.function.Consumer<LocalDate> callback
        ) {
            this.onTanggalDipilih = callback;
        }

        public void keBulanIni() {
            bulan = YearMonth.now();
            tanggalHover = null;
            repaint();
        }
        public void setBulan(YearMonth bulan) {
    if (bulan == null) {
        return;
    }

    this.bulan = bulan;
    tanggalHover = null;
    repaint();
}

        /**
         * Mencari tanggal pada koordinat tertentu, atau null jika
         * koordinat itu bukan sel tanggal.
         */
        private LocalDate tanggalDi(int x, int y) {
            int w = getWidth();
            int h = getHeight();
            int atasGrid = TINGGI_KEPALA + TINGGI_HARI;

            if (x < 0 || x >= w || y < atasGrid || y >= h) {
                return null;
            }

            double lebarKolom = w / 7.0;
            double tinggiSel = (h - atasGrid) / 6.0;

            int kolom = Math.min(6, (int) (x / lebarKolom));
            int baris = Math.min(5, (int) ((y - atasGrid) / tinggiSel));

            int geser = bulan.atDay(1).getDayOfWeek().getValue() - 1;
            int hari = baris * 7 + kolom - geser + 1;

            if (hari < 1 || hari > bulan.lengthOfMonth()) {
                return null;
            }
            return bulan.atDay(hari);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int w = getWidth();
            int h = getHeight();

            // ---------- Judul bulan ----------
            String judul = kapital(bulan.getMonth().getDisplayName(
                TextStyle.FULL, Locale.forLanguageTag("id-ID")
            )) + " " + bulan.getYear();

            g2.setFont(font(Font.BOLD, 14));
            FontMetrics fmJudul = g2.getFontMetrics();
            g2.setColor(TEXT);
            g2.drawString(
                judul, 4,
                (TINGGI_KEPALA - fmJudul.getHeight()) / 2
                    + fmJudul.getAscent()
            );

            // ---------- Tombol panah ----------
            int ukuranPanah = 28;
            int yPanah = (TINGGI_KEPALA - ukuranPanah) / 2;
            tombolKanan.setBounds(w - ukuranPanah, yPanah,
                ukuranPanah, ukuranPanah);
            tombolKiri.setBounds(w - ukuranPanah * 2 - 4, yPanah,
                ukuranPanah, ukuranPanah);
            gambarPanah(g2, tombolKiri, true);
            gambarPanah(g2, tombolKanan, false);

            double lebarKolom = w / 7.0;

            // ---------- Nama hari ----------
            g2.setFont(font(Font.BOLD, 11));
            FontMetrics fmHari = g2.getFontMetrics();
            g2.setColor(TEXT_MUTED);
            for (int k = 0; k < 7; k++) {
                int x = (int) (k * lebarKolom + lebarKolom / 2
                    - fmHari.stringWidth(HARI[k]) / 2.0);
                int y = TINGGI_KEPALA
                    + (TINGGI_HARI - fmHari.getHeight()) / 2
                    + fmHari.getAscent();
                g2.drawString(HARI[k], x, y);
            }

            // ---------- Tanggal ----------
            double tinggiSel = (h - TINGGI_KEPALA - TINGGI_HARI) / 6.0;
            int geser = bulan.atDay(1).getDayOfWeek().getValue() - 1;
            int jumlahHari = bulan.lengthOfMonth();
            LocalDate hariIni = LocalDate.now();

            for (int hari = 1; hari <= jumlahHari; hari++) {
                int posisi = geser + hari - 1;
                int baris = posisi / 7;
                int kolom = posisi % 7;

                double cx = kolom * lebarKolom + lebarKolom / 2;
                double cy = TINGGI_KEPALA + TINGGI_HARI
                    + baris * tinggiSel + tinggiSel / 2;
                double jari = Math.min(lebarKolom, tinggiSel) / 2 - 3;

                LocalDate tanggal = bulan.atDay(hari);
                boolean adalahHariIni = tanggal.equals(hariIni);
                boolean adaTransaksi = bertanda.contains(tanggal);

                if (adalahHariIni) {
                    g2.setPaint(new GradientPaint(
                        (float) (cx - jari), (float) (cy - jari), PRIMARY,
                        (float) (cx + jari), (float) (cy + jari),
                        PRIMARY_LIGHT
                    ));
                    g2.fill(new Ellipse2D.Double(
                        cx - jari, cy - jari, jari * 2, jari * 2
                    ));
                } else if (tanggal.equals(tanggalHover)) {
                    g2.setColor(PRIMARY_SOFT);
                    g2.fill(new Ellipse2D.Double(
                        cx - jari, cy - jari, jari * 2, jari * 2
                    ));
                }

                String teks = String.valueOf(hari);
                g2.setFont(font(
                    adalahHariIni ? Font.BOLD : Font.PLAIN, 12
                ));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(adalahHariIni ? Color.WHITE : TEXT);
                g2.drawString(
                    teks,
                    (int) (cx - fm.stringWidth(teks) / 2.0),
                    (int) (cy - fm.getHeight() / 2.0 + fm.getAscent() - 2)
                );

                if (adaTransaksi) {
                    g2.setColor(adalahHariIni ? Color.WHITE : ACCENT);
                    g2.fill(new Ellipse2D.Double(cx - 2, cy + 8, 4, 4));
                }
            }

            g2.dispose();
        }

        private void gambarPanah(Graphics2D g2, Rectangle r, boolean kiri) {
            g2.setColor(PRIMARY_SOFT);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);

            int cx = r.x + r.width / 2;
            int cy = r.y + r.height / 2;

            g2.setColor(PRIMARY);
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
        }
    }

    // ===================== IKON VEKTOR =====================
    public enum IconType {
        DASHBOARD, ADD, HISTORY, BUDGET, INCOME, EXPENSE, BALANCE, SEARCH,
        CALENDAR, CHART, PIE, TAG
    }

    /**
     * Ikon digambar dengan Java2D, tanpa file gambar eksternal.
     */
    public static class VectorIcon implements Icon {

        private IconType tipe;
        private int ukuran;
        private Color warna;

        public VectorIcon(IconType tipe, int ukuran, Color warna) {
            this.tipe = tipe;
            this.ukuran = ukuran;
            this.warna = warna;
        }

        @Override
        public int getIconWidth() {
            return ukuran;
        }

        @Override
        public int getIconHeight() {
            return ukuran;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            g2.translate(x, y);
            g2.setColor(warna);
            float tebal = Math.max(1.6f, ukuran / 11f);
            g2.setStroke(new BasicStroke(
                tebal, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
            ));

            double s = ukuran;
            double m = s * 0.12;

            switch (tipe) {
                case DASHBOARD: {
                    double kotak = s * 0.34;
                    double a = m + tebal / 2;
                    double b = s - m - tebal / 2 - kotak;
                    g2.draw(new RoundRectangle2D.Double(a, a, kotak, kotak, 4, 4));
                    g2.draw(new RoundRectangle2D.Double(b, a, kotak, kotak, 4, 4));
                    g2.draw(new RoundRectangle2D.Double(a, b, kotak, kotak, 4, 4));
                    g2.draw(new RoundRectangle2D.Double(b, b, kotak, kotak, 4, 4));
                    break;
                }
                case ADD: {
                    g2.drawOval((int) m, (int) m,
                        (int) (s - 2 * m), (int) (s - 2 * m));
                    g2.drawLine((int) (s / 2), (int) (s * 0.30),
                        (int) (s / 2), (int) (s * 0.70));
                    g2.drawLine((int) (s * 0.30), (int) (s / 2),
                        (int) (s * 0.70), (int) (s / 2));
                    break;
                }
                case HISTORY: {
                    g2.drawOval((int) m, (int) m,
                        (int) (s - 2 * m), (int) (s - 2 * m));
                    g2.drawLine((int) (s / 2), (int) (s * 0.30),
                        (int) (s / 2), (int) (s / 2));
                    g2.drawLine((int) (s / 2), (int) (s / 2),
                        (int) (s * 0.68), (int) (s * 0.60));
                    break;
                }
                case BUDGET: {
                    g2.drawOval((int) m, (int) m,
                        (int) (s - 2 * m), (int) (s - 2 * m));
                    g2.drawOval((int) (s * 0.32), (int) (s * 0.32),
                        (int) (s * 0.36), (int) (s * 0.36));
                    g2.fillOval((int) (s * 0.46), (int) (s * 0.46),
                        (int) (s * 0.08) + 1, (int) (s * 0.08) + 1);
                    break;
                }
                case INCOME: {
                    g2.drawLine((int) (s / 2), (int) (s * 0.20),
                        (int) (s / 2), (int) (s * 0.78));
                    g2.drawLine((int) (s / 2), (int) (s * 0.78),
                        (int) (s * 0.30), (int) (s * 0.58));
                    g2.drawLine((int) (s / 2), (int) (s * 0.78),
                        (int) (s * 0.70), (int) (s * 0.58));
                    break;
                }
                case EXPENSE: {
                    g2.drawLine((int) (s / 2), (int) (s * 0.80),
                        (int) (s / 2), (int) (s * 0.22));
                    g2.drawLine((int) (s / 2), (int) (s * 0.22),
                        (int) (s * 0.30), (int) (s * 0.42));
                    g2.drawLine((int) (s / 2), (int) (s * 0.22),
                        (int) (s * 0.70), (int) (s * 0.42));
                    break;
                }
                case BALANCE: {
                    g2.draw(new RoundRectangle2D.Double(
                        m, s * 0.24, s - 2 * m, s * 0.58, 6, 6));
                    g2.drawLine((int) m, (int) (s * 0.40),
                        (int) (s - m), (int) (s * 0.40));
                    g2.fillOval((int) (s * 0.64), (int) (s * 0.55),
                        (int) (s * 0.12) + 1, (int) (s * 0.12) + 1);
                    break;
                }
                case SEARCH: {
                    g2.drawOval((int) m, (int) m,
                        (int) (s * 0.58), (int) (s * 0.58));
                    g2.drawLine((int) (s * 0.64), (int) (s * 0.64),
                        (int) (s - m), (int) (s - m));
                    break;
                }
                case CALENDAR: {
                    g2.draw(new RoundRectangle2D.Double(
                        m, s * 0.22, s - 2 * m, s * 0.66, 6, 6));
                    g2.drawLine((int) m, (int) (s * 0.42),
                        (int) (s - m), (int) (s * 0.42));
                    g2.drawLine((int) (s * 0.32), (int) (s * 0.10),
                        (int) (s * 0.32), (int) (s * 0.28));
                    g2.drawLine((int) (s * 0.68), (int) (s * 0.10),
                        (int) (s * 0.68), (int) (s * 0.28));
                    break;
                }
                case CHART: {
                    g2.drawLine((int) (s * 0.26), (int) (s * 0.84),
                        (int) (s * 0.26), (int) (s * 0.56));
                    g2.drawLine((int) (s * 0.50), (int) (s * 0.84),
                        (int) (s * 0.50), (int) (s * 0.24));
                    g2.drawLine((int) (s * 0.74), (int) (s * 0.84),
                        (int) (s * 0.74), (int) (s * 0.42));
                    break;
                }
                case PIE: {
                    g2.drawOval((int) m, (int) m,
                        (int) (s - 2 * m), (int) (s - 2 * m));
                    g2.drawLine((int) (s / 2), (int) (s / 2),
                        (int) (s / 2), (int) m);
                    g2.drawLine((int) (s / 2), (int) (s / 2),
                        (int) (s * 0.84), (int) (s * 0.68));
                    break;
                }
                case TAG: {
                    int[] px = {
                        (int) (s * 0.14), (int) (s * 0.50),
                        (int) (s * 0.86), (int) (s * 0.86),
                        (int) (s * 0.50)
                    };
                    int[] py = {
                        (int) (s * 0.50), (int) (s * 0.14),
                        (int) (s * 0.14), (int) (s * 0.50),
                        (int) (s * 0.86)
                    };
                    g2.drawPolygon(px, py, 5);
                    g2.fillOval((int) (s * 0.64), (int) (s * 0.28),
                        (int) (s * 0.10) + 1, (int) (s * 0.10) + 1);
                    break;
                }
                default:
                    break;
            }

            g2.dispose();
        }
    }
}