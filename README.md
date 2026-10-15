<h1 align="center">Little Books - Kotlin</h1>

<p align="center">
 <a><img alt="Min SDK" src="https://img.shields.io/badge/Min SDK-24-020290?logo=android&logoColor=white"/></a>
 <a><img alt="Target SDK" src="https://img.shields.io/badge/Target SDK-37-0EB265?logo=android&logoColor=0EB265"/></a>
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.20-blue?logo=kotlin&logoColor=white"/></a>
 <a href="https://opensource.org/licenses/MIT"><img alt="License" src="https://img.shields.io/badge/License-MIT-CC9900?logo=MIT&logoColor=white"/></a>
</p>

## Day Mode
Splash | Login | Register 
--- | --- | --- | 
![](UI/1.jpg) | ![](UI/2.jpg) | ![](UI/3.jpg) | 

Home | Categories | Settings
--- | --- | --- | 
![](UI/4.jpg) | ![](UI/5.jpg) | ![](UI/6.jpg) | 

About App | Add New Book | Book Details 
--- | --- | --- | 
![](UI/7.jpg) | ![](UI/8.jpg) | ![](UI/9.jpg) | 

View Book | My Profile | Edit Profile
--- | --- | --- | 
![](UI/10.jpg) | ![](UI/11.jpg) | ![](UI/12.jpg) | 

My Books | Editors Choice | Category Books  
--- | --- | --- | 
![](UI/13.jpg) | ![](UI/14.jpg) | ![](UI/15.jpg) | 

Search Book | Favorites | Privacy Policy
--- | --- | --- | 
![](UI/16.jpg) | ![](UI/17.jpg) | ![](UI/18.jpg) | 

## Night Mode
Splash | Login | Register
--- | --- | --- | 
![](UI/19.jpg) | ![](UI/20.jpg) | ![](UI/21.jpg) | 

Home | Categories | Settings
--- | --- | --- | 
![](UI/22.jpg) | ![](UI/23.jpg) | ![](UI/24.jpg) | 

About App | Add New Book | Book Details
--- | --- | --- | 
![](UI/25.jpg) | ![](UI/26.jpg) | ![](UI/27.jpg) | 

View Book | My Profile | Edit Profile
--- | --- | --- | 
![](UI/28.jpg) | ![](UI/29.jpg) | ![](UI/30.jpg) | 

My Books | Category Books | Category Books 
--- | --- | --- | 
![](UI/31.jpg) | ![](UI/32.jpg) | ![](UI/33.jpg) | 

Search Book | Favorites | Privacy Policy
--- | --- | --- | 
![](UI/34.jpg) | ![](UI/35.jpg) | ![](UI/36.jpg) | 

<a href='https://play.google.com/store/apps/details?id=com.flatcode.littlebooks'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="170px"/></a>
<br />

---

### ✨ Core Functionalities
*   **Robust Authentication Flow**: Secure identity management via **Firebase Authentication**.
*   **Book Catalog & PDF Reading**: Read PDF books, explore categories, authors, and book details.
*   **Favorites & Personal Library**: Add books to favorites, manage published books and categories.
*   **Context-Aware Theming**: Adaptive UI support for dark and light modes.
*   **Resilient Offline Capabilities**: "Local-first" data strategy using **Room Database**.
*   **Elastic Cloud Synchronization**: Distributed data persistence via **Firebase**.

---

## 🛠️ Built With
*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Framework:** Material Design 3, ViewBinding
*   **Database:** [Room](https://developer.android.com/training/data-storage/room)
*   **Cloud Infrastructure:** [Firebase](https://firebase.google.com/) (Auth, Realtime Database)
*   **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
*   **Async Operations:** Coroutines & Flow

---

### 🏗️ Technical Architecture

#### User App
```text
app/src/main/java/com/flatcode/littlebooks/
├── db/                 # Room Database Configuration & DAOs
├── di/                 # Dependency Injection (Hilt modules)
├── model/              # Data Entities & Models
├── repository/         # Repository Pattern Implementation
├── ui/                 # Presentation Layer
│   ├── auth/           # Authentication (Login, Register)
│   ├── book/           # Book Operations & PDF Viewer
│   ├── category/       # Category Books & Browsing
│   ├── main/           # Main Dashboard Navigation
│   ├── profile/        # User Profile, Favorites & Edit Profile
│   ├── publisher/      # Publishers Management
│   ├── settings/       # App Settings & Privacy Policy
│   └── splash/         # Splash Screen
├── viewmodel/          # ViewModels Layer
└── utils/              # Extensions, Constants & Utility Classes
```

#### Admin App
```text
app/src/main/java/com/flatcode/littlebooksadmin/
├── di/                 # Dependency Injection (Hilt modules)
├── model/              # Data Entities & Models
├── repository/         # Repository Pattern Implementation
├── ui/                 # Admin Presentation Layer
│   ├── ads/            # Ads Management
│   ├── auth/           # Admin Authentication
│   ├── book/           # Book Management & PDF Uploads
│   ├── category/       # Category Management
│   ├── main/           # Admin Dashboard
│   ├── profile/        # Admin Profile Settings
│   ├── settings/       # App Settings
│   └── user/           # User Management
└── utils/              # Extensions, Constants & Utility Classes
```

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Ladybug or newer.
*   JDK 21.
*   Android SDK Level 37 (Compile SDK).

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/selimdawa/LittleBooksKotlin.git
    ```
2.  Open the project in Android Studio.
3.  Sync Project with Gradle Files.
4.  Add your `google-services.json` file to the `app/` directory.
5.  Run the app on your device or emulator.

---

### 🛠️ Technology Stack
*   **Language**: Kotlin (Modern, expressive, and safe development).
*   **Dependency Injection**: **Dagger Hilt** (Simplified, standard DI for Android).
*   **Cloud Infrastructure**: **Firebase** (Scalable authentication and real-time NoSQL storage).
*   **Local Persistence**: **Room** (SQLite abstraction layer for robust offline data).
*   **Concurrency**: **Coroutines & Flow** (High-performance asynchronous data streams).
*   **View Interop**: **ViewBinding** (Safe and efficient UI component interaction).

---

## 🔗 Links & Resources
*   **Legacy Versions:**
    *   [Kotlin Old Code Version](https://github.com/selimdawa/LittleBooksKotlinOld/)
    *   [Java Old Code Version](https://github.com/selimdawa/LittleBooks/)
*   **Author:** [Selim Dawa](https://github.com/selimdawa)

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
