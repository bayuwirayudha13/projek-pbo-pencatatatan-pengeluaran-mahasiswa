
package gui;

import service.ManajerMySaku;
import model.Transaksi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tabel riwayat dan pencarian.
 * Dikerjakan oleh: ALHIYA
 */
public class TabelTransaksiPanel extends JPanel {

    private ManajerMySaku manajer;

    private JTable tabelTransaksi;
    private DefaultTableModel modelTabel;
    private JTextField inputPencarian;

    public TabelTransaksiPanel(ManajerMySaku manajer) {
        this.manajer = manajer;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setBackground(Color.WHITE);

        // Judul tabel
        JLabel judul = new JLabel("Riwayat Transaksi");
        judul.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Bagian pencarian
        JPanel panelAtas = new JPanel(new BorderLayout(10, 10));
        panelAtas.setBackground(Color.WHITE);

        inputPencarian = new JTextField();
        inputPencarian.setToolTipText(
            "Cari berdasarkan ID, tanggal, jenis, kategori, atau catatan"
        );

        JButton tombolCari = new JButton("Cari");
        JButton tombolReset = new JButton("Reset");

        JPanel panelPencarian = new JPanel(new BorderLayout(5, 5));
        panelPencarian.setBackground(Color.WHITE);
        panelPencarian.add(inputPencarian, BorderLayout.CENTER);

        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        tombolPanel.setBackground(Color.WHITE);
        tombolPanel.add(tombolCari);
        tombolPanel.add(tombolReset);

        panelPencarian.add(tombolPanel, BorderLayout.EAST);

        panelAtas.add(judul, BorderLayout.NORTH);
        panelAtas.add(panelPencarian, BorderLayout.CENTER);

        // Membuat model tabel
        String[] kolom = {
            "ID", "Tanggal", "Jenis", "Kategori", "Nominal", "Catatan"
        };

        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelTransaksi = new JTable(modelTabel);
        tabelTransaksi.setRowHeight(28);
        tabelTransaksi.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabelTransaksi.getTableHeader().setFont(
            new Font("SansSerif", Font.BOLD, 12)
        );

        // Mengatur lebar kolom
        tabelTransaksi.getColumnModel().getColumn(0).setPreferredWidth(65);
        tabelTransaksi.getColumnModel().getColumn(1).setPreferredWidth(95);
        tabelTransaksi.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabelTransaksi.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabelTransaksi.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabelTransaksi.getColumnModel().getColumn(5).setPreferredWidth(130);

        JScrollPane scrollPane = new JScrollPane(tabelTransaksi);

        // Aksi pencarian
        tombolCari.addActionListener(e -> cariData());

        inputPencarian.addActionListener(e -> cariData());

        // Mengembalikan seluruh data
        tombolReset.addActionListener(e -> {
            inputPencarian.setText("");
            refreshTable();
        });

        add(panelAtas, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Menampilkan data awal
        refreshTable();
    }

    /**
     * Menampilkan seluruh transaksi dari ManajerMySaku.
     */
    public void refreshTable() {
        tampilkanData(manajer.getSemuaTransaksi());
    }

    /**
     * Menampilkan data transaksi ke tabel.
     */
    private void tampilkanData(List<Transaksi> daftar) {
        modelTabel.setRowCount(0);

        for (Transaksi transaksi : daftar) {
            Object[] baris = {
                transaksi.getId(),
                transaksi.getTanggal(),
                transaksi.getTipeTransaksi(),
                transaksi.getKategori(),
                String.format("Rp %,.0f", transaksi.getNominal()),
                transaksi.getCatatan()
            };

            modelTabel.addRow(baris);
        }
    }

    /**
     * Mencari transaksi berdasarkan kata kunci.
     */
    private void cariData() {
        String kataKunci = inputPencarian.getText()
            .trim()
            .toLowerCase();

        List<Transaksi> hasil = new java.util.ArrayList<>();

        for (Transaksi transaksi : manajer.getSemuaTransaksi()) {

            String data = (
                transaksi.getId() + " " +
                transaksi.getTanggal() + " " +
                transaksi.getTipeTransaksi() + " " +
                transaksi.getKategori() + " " +
                transaksi.getCatatan()
            ).toLowerCase();

            if (data.contains(kataKunci)) {
                hasil.add(transaksi);
            }
        }

        tampilkanData(hasil);
    }
}
