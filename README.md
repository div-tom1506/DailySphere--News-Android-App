# 📰 DailySphere

**DailySphere** is a modern Android news application built using Kotlin. It delivers up-to-date news from around the world using the [NewsData.io API](https://newsdata.io/). The app features an intuitive interface with category filters, language and country selection, real-time breaking news notifications, and more.

---

## ✨ Features

- 🔍 **Search News** — Easily search for articles using keywords.
- 🌐 **Category Selector** — Horizontal chip-based category filtering (e.g., Business, Sports, Technology).
- 🌎 **Language & Country Selector** — Customize news based on region and language.
- ⚡ **Breaking News Alerts** — Get real-time push notifications for breaking news.
- 📰 **Article Viewer** — Read full articles in-app using WebView.
- 📡 **Offline/No Internet Handling** — User-friendly messages when there is no internet.
- 🔄 **Pagination & Scroll to Top** — Smooth scrolling with "Back to Top" functionality.
- 💡 **Shimmer Loading UI** — Clean and elegant loading indicators.

---

## 📷 Screenshots
---

## 🛠️ Tech Stack

- Kotlin
- MVVM Architecture
- Retrofit (API calls)
- Coroutines (Async operations)
- LiveData & ViewModel (Lifecycle-aware UI updates)
- Glide (Efficient image loading and caching) ✅
- RecyclerView & Custom Adapters
- WorkManager (Scheduled background tasks)
- WebView (For article viewing)

---

## 🔐 Permissions

The app uses the following permissions:

- `INTERNET` – To fetch data from News API.
- `ACCESS_NETWORK_STATE` – To check network status.
- `POST_NOTIFICATIONS` – To send breaking news alerts (Android 13+).

---

## 🚀 Setup Instructions
### 1. **Clone the Repository**

```bash
git clone https://github.com/div-tom1506/DailySphere--News-Android-App.git
cd dailysphere 
```

### 2. **Open in Android Studio**
- Open Android Studio
- Click "Open" and select the cloned project directory

### 3. **Add API Key**
- This app uses the NewsData.io API for fetching news articles.
- Create a file named ```local.properties``` (if it doesn't exist) in the root project directory.
- Add your NewsData API key:
``` bash
NEWS_API_KEY=your_api_key_here
```
The app fetches this key in the code via ```BuildConfig.NEWS_API_KEY```

### 4. **Build the Project**
Click **Build > Rebuild Project** or use:
```bash
./gradlew build
```

### 5. **Run the App**
- Connect an Android device or start an emulator
- Click **Run > Run 'app'**

---
## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

