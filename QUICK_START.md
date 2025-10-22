# TeraBox API - Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Add Your Cookies

Open `src/main/resources/application.yml` and replace the placeholder cookies:

```yaml
terabox:
  cookies:
    - "paste_your_cookie_1_here"
    - "paste_your_cookie_2_here"
    - "paste_your_cookie_3_here"
```

**How to get cookies:**
1. Open https://terabox.com in browser
2. Login to your account
3. Press F12 → Network tab
4. Refresh page
5. Click any request → Headers → Copy "Cookie" value

---

### Step 2: Build & Run

**Windows:**
```bash
run.bat
```

**Linux/Mac:**
```bash
mvn clean package
java -jar target/terabox-api-sp-1.0.0.jar
```

**Or use Maven directly:**
```bash
mvn spring-boot:run
```

---

### Step 3: Test It!

**Using cURL:**
```bash
curl -X POST http://localhost:8080/api/terabox \
  -H "Content-Type: application/json" \
  -d "{\"url\": \"https://terabox.com/s/1xxxxx\"}"
```

**Using PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/terabox" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"url": "https://terabox.com/s/1xxxxx"}'
```

**Using Postman:**
- Method: POST
- URL: `http://localhost:8080/api/terabox`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
  ```json
  {
    "url": "https://terabox.com/s/1xxxxx"
  }
  ```

---

## 📋 API Endpoints

### 1. Process TeraBox URL
- **URL:** `POST /api/terabox`
- **Body:** `{ "url": "terabox_url" }`
- **Response:** Official TeraBox API structure with file list

### 2. Health Check
- **URL:** `GET /api/health`
- **Response:** `{ "status": "OK", "message": "..." }`

---

## ✅ What You Get

- ✅ Single file URLs → Returns file info
- ✅ Folder URLs → Returns ALL files recursively
- ✅ Official TeraBox response format
- ✅ Direct download links
- ✅ Stream URLs for videos
- ✅ Thumbnail URLs
- ✅ File metadata (size, md5, timestamps)

---

## 🔧 Configuration

All settings in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080  # Change API port

terabox:
  cookies:
    - "cookie1"
    - "cookie2"
  
  http:
    connect-timeout: 30000  # Connection timeout (ms)
    read-timeout: 60000     # Read timeout (ms)
  
  retry:
    max-attempts: 3         # Retry attempts
    backoff-delay: 2000     # Retry delay (ms)

logging:
  level:
    com.terabox.api: DEBUG  # Log level: DEBUG, INFO, WARN, ERROR
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| "No cookies configured" | Add cookies in `application.yml` |
| "Cookie is invalid" | Get fresh cookies from TeraBox |
| "Invalid TeraBox URL" | Check URL format |
| "Connection timeout" | Increase timeout in config |
| Build fails | Check Java 17+ is installed |

---

## 📁 Project Structure

```
terabox-api-sp/
├── src/main/
│   ├── java/com/terabox/api/
│   │   ├── TeraBoxApiApplication.java    # Main app
│   │   ├── controller/                   # REST endpoints
│   │   ├── service/                      # Business logic
│   │   ├── dto/                          # Request/Response models
│   │   ├── config/                       # Configuration
│   │   └── util/                         # Utilities
│   └── resources/
│       └── application.yml               # ⚙️ CONFIG HERE
├── pom.xml                               # Dependencies
└── README.md                             # Full documentation
```

---

## 📖 Documentation

- **README.md** - Full user guide
- **SETUP.md** - Detailed setup instructions
- **IMPLEMENTATION_SUMMARY.md** - Technical details

---

## 🎯 Example Response

```json
{
  "errno": 0,
  "request_id": 9141406616576050447,
  "server_time": 1761125258,
  "cfrom_id": 0,
  "title": "/filename.mkv",
  "list": [
    {
      "category": "1",
      "fs_id": "96610459486545",
      "server_filename": "filename.mkv",
      "size": "1432461005",
      "md5": "3ac45c16e3b5efd76405189a44a74f44",
      "dlink": "https://...",
      "direct_link": "https://...",
      "stream_url": "https://...",
      "thumbs": { ... }
    }
  ],
  "share_id": 68536951105,
  "uk": 4402236204928
}
```

---

## 💡 Tips

1. **Multiple Cookies:** Add more cookies for better reliability
2. **Logging:** Set to DEBUG to see detailed processing
3. **Port:** Change port if 8080 is already in use
4. **Timeout:** Increase for slow connections
5. **Retry:** Increase attempts for unstable networks

---

## 🚀 Ready to Go!

Your TeraBox API is ready. Just add cookies and run!

Need help? Check the full documentation in README.md

