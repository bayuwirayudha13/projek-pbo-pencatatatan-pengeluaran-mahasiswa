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
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tema visual bersama untuk seluruh GUI MySaku.
 * Berisi warna, font, dan komponen kustom (kartu, tombol, badge, ikon).
 * Hanya menyentuh tampilan; tidak berhubungan dengan logika aplikasi.
 */
public final class UITheme {

    private UITheme() {
    }

    // ===================== WARNA =====================
    public static final Color PRIMARY_DARK = new Color(30, 58, 138);
    public static final Color PRIMARY = new Color(59, 130, 246);
    public static final Color PRIMARY_SOFT = new Color(219, 234, 254);
    public static final Color BG = new Color(243, 244, 248);
    public static final Color CARD = Color.WHITE;
    public static final Color TABLE_HEAD = new Color(232, 238, 251);
    public static final Color TABLE_ZEBRA = new Color(249, 250, 253);

    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color SUCCESS_DARK = new Color(22, 163, 74);
    public static final Color SUCCESS_SOFT = new Color(220, 252, 231);

    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color WARNING_DARK = new Color(180, 110, 5);
    public static final Color WARNING_SOFT = new Color(254, 243, 199);

    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color DANGER_DARK = new Color(220, 38, 38);
    public static final Color DANGER_SOFT = new Color(254, 226, 226);

    public static final Color TEXT = new Color(31, 41, 55);
    public static final Color TEXT_MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(226, 232, 240);

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
                    g2.setColor(new Color(15, 23, 42, 6));
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
            } else if (gaya == SOFT) {
                setForeground(base);
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

            g2.setColor(new Color(226, 232, 240));
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
            thumbColor = new Color(203, 213, 225);
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

    // ===================== IKON VEKTOR =====================
    public enum IconType {
        DASHBOARD, ADD, HISTORY, BUDGET, INCOME, EXPENSE, BALANCE, SEARCH
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
                default:
                    break;
            }

            g2.dispose();
        }
    }
}