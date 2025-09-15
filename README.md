# HR AI Agent - Asisten HR Berbasis AI

## Deskripsi
HR AI Agent adalah aplikasi Java yang memahami permintaan karyawan dalam Bahasa Indonesia dan mengeksekusi aksi HR secara otomatis. Aplikasi ini membedakan pertanyaan (data lookup) dan perintah (aksi HR) dari input pengguna.

## Fitur Utama

### 🔍 Pertanyaan (Question Handling)
- **Manajer**: "Siapa manajer Rina?"
- **Sisa Cuti**: "Sisa cuti Budi berapa?"
- **Informasi Karyawan**: "Info Budi apa aja?"
- **Departemen**: "Budi kerja di departemen mana?"
- **Jabatan**: "Jabatan Rina apa?"
- **Status Karyawan**: "Status Budi gimana?"
- **Email**: "Email Rina berapa?"

### ⚡ Perintah (Command Handling)
- **Ajukan Cuti**: "Saya mau ajukan cuti sakit dari 1-5 Januari"
- **Jadwalkan Review**: "Jadwalkan review performa Budi dengan Pak Andi"
- **Cek Status Cuti**: "Cek status cuti Rina"
- **Lapor Pengeluaran**: "Lapor pengeluaran transportasi 250 ribu"
- **Cari Info Rekan**: "Cari info tentang Budi"

### 🧠 Natural Language Processing
- Deteksi otomatis pertanyaan vs perintah
- Parsing tanggal format Indonesia (misal: 1 Januari, 15 Agustus)
- Fuzzy matching nama karyawan (mengatasi typo)
- Variasi input informal

### 🆕 Fitur Baru
- **Penjadwalan Review Performa Otomatis**: Input tanggal dan reviewer lebih fleksibel
- **Pengurangan Sisa Cuti Otomatis**: Setelah pengajuan cuti berhasil
- **Validasi Data**: Error handling lebih baik jika data tidak ditemukan
- **Dukungan Multi-Tipe Cuti**: Tahunan, Sakit, Melahirkan

## Struktur Data

### File CSV
- `employees.csv` - Data karyawan
- `leave_balances.csv` - Sisa cuti per tipe
- `leave_requests.csv` - Riwayat pengajuan cuti
- `performance_reviews.csv` - Data review performa

### Format
```
employees.csv: id,nama,email,jabatan,departemen,id_manajer,tanggal_bergabung,status_karyawan
leave_balances.csv: id_karyawan,tipe_cuti,sisa_hari
leave_requests.csv: id_request,id_karyawan,tipe_cuti,tanggal_mulai,tanggal_selesai,status_request
performance_reviews.csv: id_review,id_karyawan,id_reviewer,tanggal_review,skor_performa,status_review
```

## Cara Menjalankan

### Prasyarat
- Java 8+
- File CSV berada di direktori yang sama dengan file `.java`

### Kompilasi & Menjalankan
```bash
javac -d bin src/com/hr/agent/*.java
java -cp bin com.hr.agent.HRAgentApp
```

### Contoh Interaksi
```
=== Selamat datang di HR AI Agent ===
Saya dapat membantu Anda dengan:
• Pertanyaan tentang data karyawan (siapa manajer Rina?, sisa cuti Budi berapa?)
• Perintah HR (ajukan cuti, jadwalkan review, lapor pengeluaran)
Ketik 'keluar' untuk mengakhiri.

Input: siapa manajer rina?
HR Agent: Manajer Rina adalah Santi Putri.

Input: sisa cuti budi berapa?
HR Agent: Sisa cuti Budi:
- Tahunan: 12 hari
- Sakit: 7 hari

Input: saya mau ajukan cuti sakit dari 1-5 januari
HR Agent: KONFIRMASI: Pengajuan cuti untuk Budi (jenis: Sakit) dari tanggal 1 Januari 2024 hingga 5 Januari 2024 telah dicatat.

Input: keluar
HR Agent: Terima kasih, sampai jumpa!
```

## Teknologi

- **Regex Pattern Matching**: Deteksi pertanyaan/perintah
- **Fuzzy String Matching**: Nama karyawan
- **Date Parsing**: Format tanggal Indonesia
- **CSV Reading & Writing**: Data HR
- **In-Memory Caching**: Akses data cepat
- **Error Handling**: Validasi input dan data

## Arsitektur

- **HRAgentApp**: Main class, input handler
- **HRFunctions**: Interface fungsi HR
- **MockHRFunctions**: Implementasi mock
- **Input Parser**: Analisis input
- **Question/Command Handler**: Eksekusi aksi/data lookup
- **Data Lookup**: File CSV
- **Response Generator**: Output ke user
