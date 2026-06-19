# BandMatch 🎸🥁

BandMatch adalah platform berbasis web monolitik yang dirancang untuk mempertemukan manajer band dengan musisi (anggota band). Platform ini mempermudah proses pembentukan dan manajemen band melalui fitur rekrutmen terbuka, kurasi lamaran, serta pengiriman tawaran (*offer*) kontrak secara interaktif.

Proyek ini dibangun menggunakan **Spring Boot 4.1.x** dengan mengimplementasikan prinsip-prinsip Pemrograman Berorientasi Objek (OOP) tingkat lanjut seperti *Inheritance*, *Polymorphism*, *Abstraction*, dan *Encapsulation*.

---

## 🛠️ Spesifikasi Teknologi

| Lapisan / Komponen | Teknologi | Spesifikasi / Versi |
| :--- | :--- | :--- |
| **Frontend (UI)** | Thymeleaf, HTML5, CSS3 | 3.1.x / Standard |
| **Backend (Framework)** | Spring Boot, Spring MVC, Spring Data JPA | 4.1.x (GA Stable) |
| **Bahasa Pemrograman** | Java | OpenJDK 22.0.1 (Target: Java 21) |
| **Database (DB)** | H2 Database (In-Memory) | 2.3.x (Bawaan Spring Boot) |
| **Build & Dependency** | Apache Maven & Maven Wrapper (`mvnw`) | 3.9+ |
| **Version Control** | Git | Latest |
| **Development Tools** | IntelliJ IDEA | Community Edition |

---

## 📐 Arsitektur Kode

Proyek ini menerapkan **Layered Architecture (Arsitektur Berlapis)** / *Three-Tier Architecture* untuk memisahkan tanggung jawab kode (*Separation of Concerns*). Selain itu, pada lapisan model digunakan pendekatan **Domain-Driven Grouping** untuk mengelompokkan entitas berdasarkan konteks bisnisnya.

### Alur Kerja Data (Data Flow)
`User/Browser (UI)` ⇄ `Controller` ⇄ `Service (Business Logic)` ⇄ `Repository (JPA)` ⇄ `H2 Database`

---

## 📂 Struktur Pohon Direktori (Directory Tree)

```text
bandmatch/
├── pom.xml                            # Berkas konfigurasi utama Maven (dependensi & build)
├── mvnw / mvnw.cmd                    # Script eksekusi Maven Wrapper
├── .mvn/                              # Konfigurasi internal Maven Wrapper
└── src/
    ├── test/                          # Folder Unit Testing aplikasi
    └── main/
        ├── java/com/bandmatch/bandmatch/      # ROOT PACKAGE
        │   ├── BandmatchApplication.java      # Entry Point Utama Spring Boot
        │   │
        │   ├── controller/                    # 1. PRESENTATION LAYER (MVC Controller)
        │   │   ├── AuthController.java        # Menangani registrasi, login, dan logout session
        │   │   ├── ManagerController.java     # Fitur Manajer: Manajemen band, rekrutmen, & peninjauan lamaran
        │   │   └── MemberController.java      # Fitur Musisi: Manajemen portofolio & peninjauan tawaran
        │   │
        │   ├── service/                       # 2. BUSINESS LOGIC LAYER
        │   │   ├── AuthService.java           # Logika registrasi polimorfis untuk Manajer & Member
        │   │   ├── BandService.java           # Manajemen CRUD band dan pembukaan lowongan rekrutmen
        │   │   ├── InteractionService.java    # Alur transaksi pengiriman & respons lamaran/tawaran
        │   │   └── MemberService.java         # Manajemen profil musisi dan pembaruan portofolio
        │   │
        │   ├── repository/                    # 3. DATA ACCESS LAYER (Spring Data JPA)
        │   │   ├── UserRepository.java        # Kueri dasar autentikasi (findByEmail)
        │   │   ├── BandRepository.java        # Pencarian band spesifik dengan filter kriteria
        │   │   └── ...                        # Repository lainnya untuk akses tabel H2
        │   │
        │   └── domain/                        # 4. DOMAIN MODEL LAYER (Inti OOP & Entitas)
        │       ├── user/                      # Penerapan Hubungan Pewarisan (Inheritance)
        │       │   ├── User.java              # Abstract Class sebagai parent model pengguna
        │       │   ├── Manager.java           # Subclass khusus pengguna bertindak sebagai Manajer
        │       │   └── BandMember.java        # Subclass khusus pengguna bertindak sebagai Musisi
        │       ├── band/                      # Agregat data terkait grup band
        │       │   ├── Band.java              # Model data band (multi-genre)
        │       │   ├── Discography.java       # Model rekam jejak karya musik band
        │       │   └── Recruitment.java       # Lowongan posisi musisi yang dibutuhkan band
        │       └── interaction/               # Abstraksi Alur Transaksi Musik
        │           ├── MusicInteraction.java  # Interface kontrak interaksi (send, respond, status)
        │           ├── InteractionStatus.java # Enum status (PENDING, ACCEPTED, REJECTED, EXPIRED)
        │           ├── Application.java       # Implementasi lamaran dari Musisi ke Lowongan Band
        │           └── Offer.java             # Implementasi tawaran langsung dari Manajer ke Musisi
        │
        └── resources/                         # 5. LAPISAN SUMBER DAYA NON-JAVA
            ├── static/css/style.css           # Berkas penataan gaya tampilan global (CSS)
            └── templates/                     # Berkas UI Dinamis Berbasis Thymeleaf (*.html)
