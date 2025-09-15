package com.hr.agent;

import java.time.LocalDate;

/**
 * PANDUAN UNTUK KANDIDAT:
 * Buatlah sebuah kelas yang mengimplementasikan interface `HRFunctions` ini.
 * Untuk proyek ini, implementasi "mock" sudah cukup. Artinya, fungsi-fungsi ini
 * tidak perlu melakukan logika bisnis yang kompleks, cukup mengembalikan/mencetak 
 * pesan konfirmasi bahwa fungsi berhasil dipanggil dengan parameter yang benar.
 */
public interface HRFunctions {

    /**
     * Memproses pengajuan cuti seorang karyawan.
     * @return String berisi pesan konfirmasi.
     */
    String applyForLeave(String employeeName, String leaveType, LocalDate startDate, LocalDate endDate);

    /**
     * Menjadwalkan sesi review performa.
     * @return String berisi pesan konfirmasi.
     */
    String schedulePerformanceReview(String employeeName, String reviewerName, LocalDate reviewDate);

    /**
     * Memeriksa status pengajuan cuti terakhir dari seorang karyawan.
     * Implementasi mock bisa mencari dari file leave_requests.csv.
     * @return String berisi status cuti.
     */
    String checkLeaveRequestStatus(String employeeName);
    
    /**
     * Mengajukan laporan pengeluaran (expense report).
     * @return String berisi pesan konfirmasi.
     */
    String submitExpenseReport(String employeeName, String category, double amount);

    /**
     * Mencari informasi dasar (non-sensitif) tentang rekan kerja.
     * Implementasi mock bisa mencari dari file employees.csv.
     * @return String berisi info kontak atau jabatan.
     */
    String lookupColleagueInfo(String colleagueName);
}