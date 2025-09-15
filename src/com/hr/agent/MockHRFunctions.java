package com.hr.agent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MockHRFunctions implements HRFunctions {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    @Override
    public String applyForLeave(String employeeName, String leaveType, LocalDate startDate, LocalDate endDate) {
        return String.format(
            "KONFIRMASI: Pengajuan cuti untuk %s (jenis: %s) dari tanggal %s hingga %s telah dicatat.",
            employeeName, leaveType, startDate.format(formatter), endDate.format(formatter)
        );
    }

    @Override
    public String schedulePerformanceReview(String employeeName, String reviewerName, LocalDate reviewDate) {
        return String.format(
            "KONFIRMASI: Sesi review performa untuk %s dengan %s telah dijadwalkan pada %s.",
            employeeName, reviewerName, reviewDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
        );
    }

    @Override
    public String checkLeaveRequestStatus(String employeeName) {
        // Implementasi nyata: mencari di leave_requests.csv
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("leave_requests.csv"))) {
            String line;
            boolean isFirstLine = true;
            String lastStatus = "Tidak ada pengajuan cuti";
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }
                String[] cols = line.split(",");
                if (cols.length >= 6) {
                    String idKaryawan = cols[1];
                    String status = cols[5];
                    
                    String empName = getEmployeeNameById(idKaryawan);
                    if (empName != null && empName.equalsIgnoreCase(employeeName)) {
                        lastStatus = status;
                    }
                }
            }
            
            return String.format(
                "INFO: Status pengajuan cuti terakhir untuk %s adalah: %s.",
                employeeName, lastStatus
            );
        } catch (java.io.IOException e) {
            return String.format(
                "ERROR: Tidak dapat mengakses data status cuti untuk %s: %s",
                employeeName, e.getMessage()
            );
        }
    }
    
    private String getEmployeeNameById(String id) {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("employees.csv"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }
                String[] cols = line.split(",");
                if (cols.length > 0 && cols[0].equals(id)) {
                    return cols[1]; // return nama
                }
            }
        } catch (java.io.IOException e) {
            // ignore
        }
        return null;
    }
    
    @Override
    public String submitExpenseReport(String employeeName, String category, double amount) {
        return String.format(
            "KONFIRMASI: Laporan pengeluaran untuk %s sebesar Rp%,.2f (kategori: %s) telah diajukan untuk diproses.",
            employeeName, amount, category
        );
    }

    @Override
    public String lookupColleagueInfo(String colleagueName) {
        // Implementasi nyata: mencari di employees.csv
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("employees.csv"))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }
                String[] cols = line.split(",");
                if (cols.length >= 8) {
                    String nama = cols[1];
                    String email = cols[2];
                    String jabatan = cols[3];
                    String departemen = cols[4];
                    String status = cols[7];
                    
                    if (nama.trim().equalsIgnoreCase(colleagueName.trim())) {
                        return String.format(
                            "INFO: Informasi untuk %s:\n• Email: %s\n• Jabatan: %s\n• Departemen: %s\n• Status: %s",
                            nama.trim(), email.trim(), jabatan.trim(), departemen.trim(), status.trim()
                        );
                    }
                }
            }
            
            return String.format(
                "INFO: Karyawan %s tidak ditemukan dalam database.",
                colleagueName
            );
        } catch (java.io.IOException e) {
            return String.format(
                "ERROR: Tidak dapat mengakses data karyawan untuk %s: %s",
                colleagueName, e.getMessage()
            );
        }
    }
}