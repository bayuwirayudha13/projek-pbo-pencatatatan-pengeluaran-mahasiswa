
package gui;

import service.ManajerMySaku;
import javax.swing.*;
import java.awt.*;

/**
 * Jendela utama aplikasi MySaku.
 * Dikerjakan oleh: ALHIYA
 */
public class MySakuGUI extends JFrame {

    private ManajerMySaku manajer;

    private FormTransaksiPanel formPanel;
    private TabelTransaksiPanel tabelPanel;
    private RingkasanPanel ringkasanPanel;

    public MySakuGUI(ManajerMySaku manajer) {
        this.manajer = manajer;

        setTitle("MySaku - Pencatat Keuangan Mahasiswa");
        setSize(1000, 650);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        // Warna dasar aplikasi
        Color warnaLatar = new Color(245, 247, 250);
        getContentPane().setBackground(warnaLatar);

        // Membuat komponen panel
        formPanel = new FormTransaksiPanel(manajer);
        tabelPanel = new TabelTransaksiPanel(manajer);
        ringkasanPanel = new RingkasanPanel(manajer);
        formPanel.setOnTransaksiDisimpan(() -> {
            tabelPanel.refreshTable();
            ringkasanPanel.refreshRingkasan();
        });

        // Header aplikasi
        JPanel headerPanel = buatHeader();

        // Area konten utama
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(warnaLatar);
        contentPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );

        // Mengatur ukuran area form
        formPanel.setPreferredSize(new Dimension(300, 350));

        contentPanel.add(formPanel, BorderLayout.WEST);
        contentPanel.add(tabelPanel, BorderLayout.CENTER);

        // Area ringkasan
        ringkasanPanel.setPreferredSize(new Dimension(100, 100));

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(ringkasanPanel, BorderLayout.SOUTH);
    }

    /**
     * Membuat bagian header aplikasi.
     */
    private JPanel buatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 64, 110));
        header.setBorder(
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        JLabel judul = new JLabel("MySaku");
        judul.setForeground(Color.WHITE);
        judul.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel deskripsi = new JLabel(
            "Pencatat Keuangan Mahasiswa"
        );
        deskripsi.setForeground(new Color(220, 230, 245));
        deskripsi.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel identitas = new JPanel();
        identitas.setLayout(new BoxLayout(identitas, BoxLayout.Y_AXIS));
        identitas.setBackground(new Color(30, 64, 110));

        identitas.add(judul);
        identitas.add(deskripsi);

        JButton tombolAnggaran = new JButton("Atur Anggaran");
        tombolAnggaran.setFocusPainted(false);
        tombolAnggaran.setFont(
            new Font("SansSerif", Font.BOLD, 12)
        );

        tombolAnggaran.addActionListener(e -> {
            AnggaranDialog dialog = new AnggaranDialog(this, manajer);
            dialog.setVisible(true);

            // Memperbarui ringkasan setelah dialog ditutup
            ringkasanPanel.refreshRingkasan();
        });




        header.add(identitas, BorderLayout.WEST);
        header.add(tombolAnggaran, BorderLayout.EAST);

        return header;
    }
}
