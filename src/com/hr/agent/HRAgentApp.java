package com.hr.agent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HRAgentApp {

    private static HRFunctions hrFunctions = new MockHRFunctions();
    private static Map<String, String> employeeData = new HashMap<>();
    private static Map<String, Map<String, Integer>> leaveBalances = new HashMap<>();
    
    // Pattern untuk deteksi pertanyaan
    private static final Pattern QUESTION_PATTERNS = Pattern.compile(
        "(apa|siapa|berapa|kapan|dimana|bagaimana|apakah|bisa|boleh|mau tau|pengen tau|ingin tau|" +
        "tolong|bisa tolong|boleh tolong|mohon|tolong bantu|bisa bantu|boleh bantu)"
    );
    
    // Pattern untuk deteksi perintah
    private static final Pattern COMMAND_PATTERNS = Pattern.compile(
        "(ajukan|buat|create|submit|kirim|lapor|report|jadwalkan|schedule|set|atur|" +
        "update|ubah|change|modify|hapus|delete|remove|batal|cancel|" +
        "proses|process|eksekusi|execute|jalankan|run|cek|info|informasi|data|detail|lihat|tampilkan|show|display)"
    );

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Load data saat startup
        loadEmployeeData();
        loadLeaveBalances();

        System.out.println("=== Selamat datang di HR AI Agent ===");
        System.out.println("Saya dapat membantu Anda dengan:");
        System.out.println("- Pertanyaan tentang data karyawan (manajer, sisa cuti, departemen, jabatan, status, email)");
        System.out.println("- Perintah HR (ajukan cuti, jadwalkan review, lapor pengeluaran, cek status cuti, cari info rekan kerja)");
        System.out.println("Ketik 'keluar' untuk mengakhiri.");
        
        while (true) {
            System.out.print("\nInput: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("keluar") || input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("HR Agent: Terima kasih, sampai jumpa!");
                break;
            }

            if (input.isEmpty()) {
                System.out.println("HR Agent: Silakan ketik pertanyaan atau perintah Anda.");
                continue;
            }

            // Deteksi apakah pertanyaan atau perintah
            if (isQuestion(input)) {
                handleQuestion(input);
            } else if (isCommand(input)) {
                handleCommand(input);
            } else {
                System.out.println("HR Agent: Maaf, saya tidak mengerti. Apakah ini pertanyaan atau perintah? " +
                    "\nContoh pertanyaan: 'siapa manajer Rina?' atau 'sisa cuti Budi berapa?' " +
                    "\nContoh perintah: 'ajukan cuti sakit' atau 'jadwalkan review performa'");
            }
        }

        scanner.close();
    }

    // ===================== DETEKSI =====================

    private static boolean isQuestion(String input) {
        String lowerInput = input.toLowerCase();
        return QUESTION_PATTERNS.matcher(lowerInput).find();
    }
    
    private static boolean isCommand(String input) {
        String lowerInput = input.toLowerCase();
        return COMMAND_PATTERNS.matcher(lowerInput).find();
    }

    // ===================== HANDLER PERTANYAAN =====================

    private static void handleQuestion(String input) {
        try {
            String lowerInput = input.toLowerCase();
            
            // Pertanyaan tentang manajer
            if (lowerInput.contains("manajer") || lowerInput.contains("manager")) {
                String employeeName = extractEmployeeName(input);
                if (employeeName != null) {
                    String managerInfo = getManagerInfo(employeeName);
                    System.out.println("HR Agent: " + managerInfo);
                } else {
                    System.out.println("HR Agent: Siapa yang ingin Anda tanyakan manajernya?");
                }
            
            // Pertanyaan tentang sisa cuti
            } else if (lowerInput.contains("sisa cuti") || lowerInput.contains("cuti") || lowerInput.contains("leave")) {
                String employeeName = extractEmployeeName(input);
                if (employeeName != null) {
                    String leaveInfo = getLeaveBalanceInfo(employeeName);
                    System.out.println("HR Agent: " + leaveInfo);
                } else {
                    System.out.println("HR Agent: Siapa yang ingin Anda tanyakan sisa cutinya?");
                }
            
            // Pertanyaan tentang departemen
            } else if (lowerInput.contains("departemen") || lowerInput.contains("department") || 
                      lowerInput.contains("divisi") || lowerInput.contains("bagian")) {
                String employeeName = extractEmployeeName(input);
                if (employeeName != null) {
                    String deptInfo = getDepartmentInfo(employeeName);
                    System.out.println("HR Agent: " + deptInfo);
                } else {
                    System.out.println("HR Agent: Departemen siapa yang ingin Anda tanyakan?");
                }
            
            // Pertanyaan tentang jabatan
            } else if (lowerInput.contains("jabatan") || lowerInput.contains("posisi") || 
                      lowerInput.contains("role") || lowerInput.contains("job")) {
                String employeeName = extractEmployeeName(input);
                if (employeeName != null) {
                    String jobInfo = getJobInfo(employeeName);
                    System.out.println("HR Agent: " + jobInfo);
                } else {
                    System.out.println("HR Agent: Jabatan siapa yang ingin Anda tanyakan?");
                }
            
            // Pertanyaan tentang informasi karyawan
            } else if (lowerInput.contains("info") || lowerInput.contains("informasi") || 
                      lowerInput.contains("data") || lowerInput.contains("detail")) {
                try {
                    String employeeName = extractEmployeeName(input);
                    if (employeeName == null) {
                        System.out.println("HR Agent: Informasi siapa yang ingin dicari?");
                        return;
                    }
                    
                    System.out.println("HR Agent: " + hrFunctions.lookupColleagueInfo(employeeName));
                    
                } catch (Exception e) {
                    System.out.println("HR Agent: Gagal mencari informasi rekan kerja: " + e.getMessage());
                }

            // Pertanyaan tentang status karyawan
            } else if (lowerInput.contains("status") || lowerInput.contains("keadaan")) {
                String employeeName = extractEmployeeName(input);
                if (employeeName != null) {
                    String statusInfo = getEmployeeStatus(employeeName);
                    System.out.println("HR Agent: " + statusInfo);
                } else {
                    System.out.println("HR Agent: Status siapa yang ingin Anda tanyakan?");
                }
            
            // Pertanyaan tentang email
            } else if (lowerInput.contains("email") || lowerInput.contains("kontak")) {
                String employeeName = extractEmployeeName(input);
                if (employeeName != null) {
                    String emailInfo = getEmailInfo(employeeName);
                    System.out.println("HR Agent: " + emailInfo);
                } else {
                    System.out.println("HR Agent: Email siapa yang ingin Anda tanyakan?");
                }
            
            } else {
                System.out.println("HR Agent: Maaf, saya belum mengerti pertanyaan ini. " +
                    "\nSaya bisa membantu dengan pertanyaan tentang manajer, sisa cuti, informasi karyawan, " +
                    "departemen, jabatan, status, atau email karyawan.");
            }
        } catch (Exception e) {
            System.out.println("HR Agent: Terjadi kesalahan saat memproses pertanyaan: " + e.getMessage());
        }
    }

    // ===================== HANDLER PERINTAH =====================

    private static void handleCommand(String input) {
        try {
            String lowerInput = input.toLowerCase();
            
            // Perintah ajukan cuti
            if (lowerInput.contains("ajukan cuti") || lowerInput.contains("minta cuti")) {
                handleLeaveRequest(input);
            
            // Perintah jadwalkan review performa
            } else if (lowerInput.contains("review performa") || lowerInput.contains("review") || 
                      lowerInput.contains("jadwalkan review") || lowerInput.contains("performance")) {
                handlePerformanceReview(input);
            
            // Perintah cek status cuti
            } else if (lowerInput.contains("cek status") || lowerInput.contains("status cuti") || 
                      lowerInput.contains("check status")) {
                handleCheckLeaveStatus(input);
            
            // Perintah lapor pengeluaran
            } else if (lowerInput.contains("lapor pengeluaran") || lowerInput.contains("expense") || 
                      lowerInput.contains("pengeluaran") || lowerInput.contains("reimburse")) {
                handleExpenseReport(input);
            
            // Perintah cari info rekan kerja
            } else if (lowerInput.contains("cari info") || lowerInput.contains("info rekan") || 
                      lowerInput.contains("lookup") || lowerInput.contains("colleague")) {
                handleLookupColleague(input);
            
            } else {
                System.out.println("HR Agent: Perintah belum dikenali. Saya bisa membantu dengan: " +
                    "\najukan cuti, jadwalkan review performa, cek status cuti, lapor pengeluaran, atau cari info rekan kerja.");
            }
        } catch (Exception e) {
            System.out.println("HR Agent: Terjadi kesalahan saat memproses perintah: " + e.getMessage());
        }
    }
    
    // ===================== HANDLER PERINTAH SPESIFIK =====================
    
    private static void handleLeaveRequest(String input) {
        try {
            String employeeName = extractEmployeeName(input);
            if (employeeName == null) {
                System.out.println("HR Agent: Siapa yang ingin mengajukan cuti?");
                return;
            }

            String leaveType = extractLeaveType(input);
            LocalDate[] dates = extractDates(input);

            if (dates[0] == null || dates[1] == null) {
                System.out.println("HR Agent: Kapan tanggal cuti yang diinginkan? (contoh: dari 1-5 januari)");
                return;
            }

            // Hitung jumlah hari cuti yang diajukan
            long daysRequested = java.time.temporal.ChronoUnit.DAYS.between(dates[0], dates[1]);
            if (daysRequested <= 0) daysRequested = 1;

            // Cek sisa cuti
            Map<String, Integer> balances = leaveBalances.get(employeeName.toLowerCase());
            if (balances == null || !balances.containsKey(leaveType)) {
                System.out.println("HR Agent: Data sisa cuti untuk " + capitalize(employeeName) + " tidak ditemukan.");
                return;
            }
            int sisaCuti = balances.get(leaveType);
            if (sisaCuti < daysRequested) {
                System.out.println("HR Agent: Sisa cuti " + leaveType + " untuk " + capitalize(employeeName) + " sudah habis atau tidak cukup.");
                return;
            }

            // Proses pengajuan cuti (tambah ke leave_requests.csv)
            try {
                String employeeData = HRAgentApp.employeeData.get(employeeName.toLowerCase().trim());
                if (employeeData == null) {
                    System.out.println("Karyawan " + capitalize(employeeName) + " tidak ditemukan.");
                    return ;
                }
                String[] parts = employeeData.split(",");
                String employeeId = parts[0];

                Path filePath = Paths.get("leave_requests.csv");
                String newRequestId = generateNewRequestsId(filePath);

                String newRow = String.join(",",
                    newRequestId,
                    employeeId,
                    leaveType,
                    dates[0].toString(),
                    dates[1].toString(),
                    "Menunggu Persetujuan"
                );
                Files.write(filePath, Arrays.asList(newRow), StandardOpenOption.APPEND);

                // Kurangi sisa cuti di leave_balances.csv
                Path leaveBalancePath = Paths.get("leave_balances.csv");
                List<String> leaveLines = Files.readAllLines(leaveBalancePath);
                for (int i = 1; i < leaveLines.size(); i++) { // mulai dari 1, skip header
                    String[] cols = leaveLines.get(i).split(",");
                    if (cols.length >= 3 && cols[0].equals(employeeId) && cols[1].equalsIgnoreCase(leaveType)) {
                        int newBalance = Integer.parseInt(cols[2]) - (int)daysRequested;
                        if (newBalance < 0) newBalance = 0;
                        cols[2] = String.valueOf(newBalance);
                        leaveLines.set(i, String.join(",", cols));
                        break;
                    }
                }
                Files.write(leaveBalancePath, leaveLines);

                // Update map leaveBalances juga
                balances.put(leaveType, sisaCuti - (int)daysRequested);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Terjadi error saat menyimpan pengajuan cuti.");
                return;
            }
            
            System.out.println("HR Agent: " + hrFunctions.applyForLeave(employeeName, leaveType, dates[0], dates[1]));

        } catch (Exception e) {
            System.out.println("HR Agent: Gagal memproses pengajuan cuti: " + e.getMessage());
        }
    }

    private static String generateNewRequestsId(Path filePath) throws IOException {
        try{
            List<String> lines = Files.readAllLines(filePath);
            if (lines.size() <= 1) return "LR001"; // kosong selain header
            String lastLine = lines.get(lines.size() - 1);
            String lastRequestId = lastLine.split(",")[0];
            int nextId = Integer.parseInt(lastRequestId.substring(2)) + 1;
            return String.format("LR%03d", nextId);
        } catch (IOException e) {
            throw new IOException("Gagal membaca file leave_requests.csv: " + e.getMessage());
        }
    }
    
    private static void handlePerformanceReview(String input) {
        try {
            String employeeName = extractFirstEmployeeName(input);
            System.out.println(employeeName);
            if (employeeName == null) {
                System.out.println("HR Agent: Siapa yang akan direview?");
                return;
            }
            
            String reviewerName = extractReviewerName(input);
            if (reviewerName == null) {
                reviewerName = "Manager"; // default
            }
            System.out.println(reviewerName);

            String employeeId = getEmployeeIdByName(employeeName);
            String reviewerId = getEmployeeIdByName(reviewerName);

            if (employeeId == null) {
                System.out.println("ERROR: Karyawan " + employeeName + " tidak ditemukan.");
                return;
            }
            if (reviewerId == null) {
                reviewerId = "999"; // fallback ID reviewer default
            }
            
            LocalDate reviewDate = extractSingleDate(input);
            if (reviewDate == null) {
                reviewDate = LocalDate.now().plusDays(7); // default 1 minggu
            }

            Path filePath = Paths.get("performance_reviews.csv");
            String newReviewId = generateNewReviewId(filePath);

            String record = String.join(",",
                    newReviewId,
                    employeeId,
                    reviewerId,
                    reviewDate.toString(),
                    "0",                  // skor default 0
                    "Terjadwal"           // status default
            );

            Files.write(filePath, (record + System.lineSeparator()).getBytes(),StandardOpenOption.APPEND);
                    
            System.out.println("HR Agent: " + hrFunctions.schedulePerformanceReview(employeeName, capitalize(reviewerName), reviewDate));
        
        } catch (Exception e) {
            System.out.println("HR Agent: Gagal menjadwalkan review performa: " + e.getMessage());
        }
    }

    private static String generateNewReviewId(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            if (lines.size() <= 1) return "PR01"; // kosong selain header
            String lastLine = lines.get(lines.size() - 1);
            String lastReviewId = lastLine.split(",")[0];
            int nextId = Integer.parseInt(lastReviewId.substring(2)) + 1;
            return String.format("PR%02d", nextId);
        } catch (IOException e) {
            return "PR01";
        }
    }
    
    private static void handleCheckLeaveStatus(String input) {
        try {
            String employeeName = extractEmployeeName(input);
            if (employeeName == null) {
                System.out.println("HR Agent: Siapa yang ingin dicek status cutinya?");
                return;
            }
            
            System.out.println("HR Agent: " + hrFunctions.checkLeaveRequestStatus(employeeName));
            
        } catch (Exception e) {
            System.out.println("HR Agent: Gagal mengecek status cuti: " + e.getMessage());
        }
    }
    
    private static void handleExpenseReport(String input) {
        try {
            String employeeName = extractEmployeeName(input);
            if (employeeName == null) {
                System.out.println("HR Agent: Siapa yang melaporkan pengeluaran?");
                return;
            }
            
            String category = extractExpenseCategory(input);
            double amount = extractAmount(input);
            
            if (amount <= 0) {
                System.out.println("HR Agent: Berapa jumlah pengeluaran yang ingin dilaporkan?");
                return;
            }
            
            System.out.println("HR Agent: " + hrFunctions.submitExpenseReport(employeeName, category, amount));
            
        } catch (Exception e) {
            System.out.println("HR Agent: Gagal memproses laporan pengeluaran: " + e.getMessage());
        }
    }
    
    private static void handleLookupColleague(String input) {
        try {
            String employeeName = extractEmployeeName(input);
            if (employeeName == null) {
                System.out.println("HR Agent: Informasi siapa yang ingin dicari?");
                return;
            }
            
            System.out.println("HR Agent: " + hrFunctions.lookupColleagueInfo(employeeName));
            
        } catch (Exception e) {
            System.out.println("HR Agent: Gagal mencari informasi rekan kerja: " + e.getMessage());
        }
    }

    // ===================== DATA LOADING =====================
    
    private static void loadEmployeeData() {
        try (BufferedReader br = new BufferedReader(new FileReader("employees.csv"))) {
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
                    // Store data ke map dengan key nama
                    employeeData.put(nama.toLowerCase(), line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading employee data: " + e.getMessage());
        }
    }
    
    private static void loadLeaveBalances() {
        try (BufferedReader br = new BufferedReader(new FileReader("leave_balances.csv"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }
                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String idKaryawan = cols[0];
                    String tipeCuti = cols[1];
                    int sisaHari = Integer.parseInt(cols[2]);
                    
                    // Cari nama karyawan berdasarkan id_karyawan
                    String employeeName = getEmployeeNameById(idKaryawan);
                    if (employeeName != null) {
                        leaveBalances.computeIfAbsent(employeeName.toLowerCase(), k -> new HashMap<>())
                                   .put(tipeCuti, sisaHari);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading leave balances: " + e.getMessage());
        }
    }
    
    private static String getEmployeeNameById(String id) {
        for (Map.Entry<String, String> entry : employeeData.entrySet()) {
            String[] cols = entry.getValue().split(",");
            if (cols.length > 0 && cols[0].equals(id)) {
                return cols[1]; // return nama
            }
        }
        return null;
    }

    private static String getEmployeeIdByName(String name) {
        for (Map.Entry<String, String> entry : employeeData.entrySet()) {
            String[] cols = entry.getValue().split(",");
            if (cols.length > 1 && cols[1].equalsIgnoreCase(name.trim())) {
                return cols[0]; // return id_karyawan
            }
        }
        return null;
    }

    // ===================== DATA LOOKUP FUNCTIONS =====================

    private static String getManagerInfo(String employeeName) {
        String employeeData = HRAgentApp.employeeData.get(employeeName.toLowerCase().trim());
        if (employeeData == null) {
            return "Karyawan " + capitalize(employeeName) + " tidak ditemukan.";
        }
        
        String[] cols = employeeData.split(",");
        if (cols.length < 6) {
            return "Data karyawan tidak lengkap.";
        }
        
        String idManajer = cols[5].trim();
        if (idManajer.isEmpty()) {
            return capitalize(employeeName) + " adalah direktur utama (tidak memiliki manajer).";
        }
        
        String managerName = getEmployeeNameById(idManajer);;
        if (managerName != null) {
            return "Manajer " + capitalize(employeeName) + " adalah " + managerName + ".";
        } else {
            return "Data manajer untuk " + capitalize(employeeName) + " tidak ditemukan.";
        }
    }

    private static String getLeaveBalanceInfo(String employeeName) {
        Map<String, Integer> balances = leaveBalances.get(employeeName.toLowerCase());
        if (balances == null || balances.isEmpty()) {
            return "Data sisa cuti untuk " + capitalize(employeeName) + " tidak ditemukan.";
        }
        
        StringBuilder result = new StringBuilder("Sisa cuti " + capitalize(employeeName) + ":\n");
        for (Map.Entry<String, Integer> entry : balances.entrySet()) {
            result.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" hari\n");
        }
        return result.toString().trim();
    }

    private static String getDepartmentInfo(String employeeName) {
        String employeeData = HRAgentApp.employeeData.get(employeeName.toLowerCase());
        if (employeeData == null) {
            return "Karyawan " + capitalize(employeeName) + " tidak ditemukan.";
        }
        
        String[] cols = employeeData.split(",");
        if (cols.length < 5) {
            return "Data departemen tidak tersedia.";
        }
        
        return capitalize(employeeName) + " bekerja di departemen " + cols[4] + ".";
    }

    private static String getJobInfo(String employeeName) {
        String employeeData = HRAgentApp.employeeData.get(employeeName.toLowerCase());
        if (employeeData == null) {
            return "Karyawan " + capitalize(employeeName) + " tidak ditemukan.";
        }
        
        String[] cols = employeeData.split(",");
        if (cols.length < 4) {
            return "Data jabatan tidak tersedia.";
        }
        
        return capitalize(employeeName) + " memiliki jabatan " + cols[3] + ".";
    }

    private static String getEmployeeStatus(String employeeName) {
        String employeeData = HRAgentApp.employeeData.get(employeeName.toLowerCase());
        if (employeeData == null) {
            return "Karyawan " + capitalize(employeeName) + " tidak ditemukan.";
        }
        
        String[] cols = employeeData.split(",");
        if (cols.length < 8) {
            return "Data status tidak tersedia.";
        }
        
        return "Status " + capitalize(employeeName) + " adalah " + cols[7] + ".";
    }

    private static String getEmailInfo(String employeeName) {
        String employeeData = HRAgentApp.employeeData.get(employeeName.toLowerCase());
        if (employeeData == null) {
            return "Karyawan " + capitalize(employeeName) + " tidak ditemukan.";
        }
        
        String[] cols = employeeData.split(",");
        if (cols.length < 3) {
            return "Data email tidak tersedia.";
        }
        
        return "Email " + capitalize(employeeName) + " adalah " + cols[2] + ".";
    }

    // ===================== PARSING FUNCTIONS =====================
    
    private static String extractEmployeeName(String input) {
        String lowerInput = input.toLowerCase().trim();

        // Loop semua nama dari employeeData
        for (String record : employeeData.values()) {
            String[] cols = record.split(",");
            if (cols.length >= 6) {
                String fullName = cols[1].toLowerCase().trim();

                // Kalau input mengandung nama lengkap karyawan
                if (lowerInput.contains(fullName)) {
                    return capitalize(fullName);
                }

                // Kalau input hanya mengandung nama depan
                String firstName = fullName.split("\\s+")[0];
                if (lowerInput.contains(firstName)) {
                    return capitalize(fullName);
                }
            }
        }

        return null;
    }

    private static String extractFirstEmployeeName(String input) {
        String lowerInput = input.toLowerCase().trim();
        String bestMatch = null;
        int earliestIndex = Integer.MAX_VALUE;

        for (String record : employeeData.values()) {
            String[] cols = record.split(",");
            if (cols.length >= 6) {
                String fullName = cols[1].toLowerCase().trim();

                int idx = lowerInput.indexOf(fullName);
                if (idx != -1 && idx < earliestIndex) {
                    earliestIndex = idx;
                    bestMatch = capitalize(fullName);
                }

                String firstName = fullName.split("\\s+")[0];
                idx = lowerInput.indexOf(firstName);
                if (idx != -1 && idx < earliestIndex) {
                    earliestIndex = idx;
                    bestMatch = capitalize(fullName);
                }
            }
        }
        return bestMatch;
    }

    
    private static String extractLeaveType(String input) {
        String lowerInput = input.toLowerCase();
        
        if (lowerInput.contains("sakit") || lowerInput.contains("illness")) {
            return "Sakit";
        } else if (lowerInput.contains("tahunan") || lowerInput.contains("annual") || 
                  lowerInput.contains("libur") || lowerInput.contains("vacation")) {
            return "Tahunan";
        } else if (lowerInput.contains("melahirkan") || lowerInput.contains("maternity")) {
            return "Cuti Melahirkan";
        } else {
            return "Tahunan"; // default
        }
    }
    
    private static LocalDate[] extractDates(String input) {
        LocalDate[] dates = new LocalDate[2];
        String lowerInput = input.toLowerCase();
        
        // Pattern: "dari 1-5 januari" atau "1 sampai 5 januari"
        Pattern dateRangePattern = Pattern.compile("(\\d{1,2})\\s*(?:-|sampai|hingga|to)\\s*(\\d{1,2})\\s+(\\w+)");
        java.util.regex.Matcher matcher = dateRangePattern.matcher(lowerInput);
        
        if (matcher.find()) {
            int startDay = Integer.parseInt(matcher.group(1));
            int endDay = Integer.parseInt(matcher.group(2));
            String monthName = matcher.group(3);
            
            int month = parseMonthName(monthName);
            int year = LocalDate.now().getYear(); // default tahun sekarang
            
            try {
                dates[0] = LocalDate.of(year, month, startDay);
                dates[1] = LocalDate.of(year, month, endDay);
                return dates;
            } catch (Exception e) {
                // Invalid date
            }
        }
        
        LocalDate singleDate = extractSingleDate(input);
        if (singleDate != null) {
            dates[0] = singleDate;
            dates[1] = singleDate.plusDays(1); // default satu hari
            return dates;
        }
        
        if (lowerInput.contains("besok") || lowerInput.contains("tomorrow")) {
            System.out.println(lowerInput);
            dates[0] = LocalDate.now().plusDays(1);
            dates[1] = dates[0].plusDays(1);
            return dates;
        } else if (lowerInput.contains("hari ini") || lowerInput.contains("today")) {
            dates[0] = LocalDate.now();
            dates[1] = dates[0].plusDays(1);
            return dates;
        }

        return dates;
    }
    
    private static LocalDate extractSingleDate(String input) {
        String lowerInput = input.toLowerCase();
        
        // Pattern: "1 januari" atau "15 agustus"
        Pattern singleDatePattern = Pattern.compile("(\\d{1,2})\\s+(\\w+)");
        java.util.regex.Matcher matcher = singleDatePattern.matcher(lowerInput);
        
        if (matcher.find()) {
            int day = Integer.parseInt(matcher.group(1));
            String monthName = matcher.group(2);
            
            int month = parseMonthName(monthName);
            int year = LocalDate.now().getYear(); // default tahun sekarang
            
            try {
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    private static int parseMonthName(String monthName) {
        String[] months = {"januari", "februari", "maret", "april", "mei", "juni",
                          "juli", "agustus", "september", "oktober", "november", "desember"};
        
        for (int i = 0; i < months.length; i++) {
            if (months[i].startsWith(monthName) || monthName.startsWith(months[i])) {
                return i + 1;
            }
        }
        
        // Bulan dalam bahasa Inggris
        String[] englishMonths = {"january", "february", "march", "april", "may", "june",
                                 "july", "august", "september", "october", "november", "december"};
        
        for (int i = 0; i < englishMonths.length; i++) {
            if (englishMonths[i].startsWith(monthName) || monthName.startsWith(englishMonths[i])) {
                return i + 1;
            }
        }
        
        return LocalDate.now().getMonthValue(); // default bulan sekarang
    }
    
    private static String extractReviewerName(String input) {
        String lowerInput = input.toLowerCase();
        
        // cari pola "dengan [nama]" atau "by [nama]" atau "oleh [nama]"
        Pattern reviewerPattern = Pattern.compile("(?:dengan|by|oleh)\\s+(\\w+\\s+\\w+)");
        java.util.regex.Matcher matcher = reviewerPattern.matcher(lowerInput);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    private static String extractExpenseCategory(String input) {
        String lowerInput = input.toLowerCase();
        
        if (lowerInput.contains("transport") || lowerInput.contains("transportasi") || 
            lowerInput.contains("ongkos") || lowerInput.contains("taksi") || lowerInput.contains("grab")) {
            return "Transportasi";
        } else if (lowerInput.contains("makan") || lowerInput.contains("food") || 
                  lowerInput.contains("restoran") || lowerInput.contains("lunch")) {
            return "Makanan";
        } else if (lowerInput.contains("hotel") || lowerInput.contains("penginapan") || 
                  lowerInput.contains("akomodasi")) {
            return "Akomodasi";
        } else if (lowerInput.contains("komunikasi") || lowerInput.contains("telepon") || 
                  lowerInput.contains("internet") || lowerInput.contains("data")) {
            return "Komunikasi";
        } else if (lowerInput.contains("alat") || lowerInput.contains("peralatan") || 
                  lowerInput.contains("stationery") || lowerInput.contains("kantor")) {
            return "Peralatan Kantor";
        } else {
            return "Lain-lain";
        }
    }
    
    private static double extractAmount(String input) {
        // Cari pola angka dengan atau tanpa "Rp", koma/desimal, kata ribu/juta/k/m
        Pattern amountPattern = Pattern.compile("(?:rp\\s*)?(\\d+(?:[.,]\\d+)?)\\s*(?:ribu|juta|k|m)?", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = amountPattern.matcher(input);
        
        if (matcher.find()) {
            try {
                double amount = Double.parseDouble(matcher.group(1).replace(",", "."));
                
                // Cek untuk "ribu", "juta", "k", "m"
                String fullMatch = matcher.group(0).toLowerCase();
                if (fullMatch.contains("juta") || fullMatch.contains("m")) {
                    amount *= 1000000;
                } else if (fullMatch.contains("ribu") || fullMatch.contains("k")) {
                    amount *= 1000;
                }
                
                return amount;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        return 0;
    }

    // ===================== UTIL =====================

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Arrays.stream(s.split("\\s+"))
                 .filter(word -> !word.isEmpty())
                 .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                 .collect(Collectors.joining(" "));
    }
}
