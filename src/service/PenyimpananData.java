package service;

import java.io.IOException;

/** Kontrak penyimpanan data ke file. Dikerjakan oleh: NEHA */
public interface PenyimpananData {
    void simpanKeFile() throws IOException;
    void muatDariFile() throws IOException;
}
