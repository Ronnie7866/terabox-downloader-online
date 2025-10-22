# TeraBox API - Setup Guide

## Prerequisites

1. **Java 17 or higher**
   - Download from: https://adoptium.net/
   - Verify installation: `java -version`

2. **Maven 3.6+**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`

## Quick Start

### Step 1: Configure Cookies

1. Open `src/main/resources/application.yml`
2. Replace the placeholder cookies with your actual TeraBox cookies:

```yaml
terabox:
  cookies:
    - "your_actual_cookie_1"
    - "your_actual_cookie_2"
    - "your_actual_cookie_3"
```

**How to get TeraBox cookies:**
1. Open TeraBox website in your browser
2. Login to your account
3. Open Developer Tools (F12)
4. Go to Network tab
5. Refresh the page
6. Click on any request to terabox.com
7. Copy the entire `Cookie` header value

### Step 2: Build the Project

```bash
mvn clean package
```

This will:
- Download all dependencies
- Compile the code
- Run tests (if any)
- Create a JAR file in `target/` directory

### Step 3: Run the Application

**Option 1: Using the JAR file**
```bash
java -jar target/terabox-api-sp-1.0.0.jar
```

**Option 2: Using Maven**
```bash
mvn spring-boot:run
```

**Option 3: Using the batch script (Windows)**
```bash
run.bat
```

### Step 4: Test the API

The application will start on `http://localhost:8080`

**Test with cURL:**
```bash
curl -X POST http://localhost:8080/api/terabox \
  -H "Content-Type: application/json" \
  -d "{\"url\": \"https://terabox.com/s/1xxxxx\"}"
```

**Test with PowerShell:**
```powershell
$body = @{
    url = "https://terabox.com/s/1xxxxx"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/terabox" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

## Configuration Options

### Change Server Port

Edit `application.yml`:
```yaml
server:
  port: 9090  # Change to your desired port
```

### Adjust HTTP Timeouts

Edit `application.yml`:
```yaml
terabox:
  http:
    connect-timeout: 30000  # 30 seconds
    read-timeout: 60000     # 60 seconds
    write-timeout: 60000    # 60 seconds
```

### Configure Retry Logic

Edit `application.yml`:
```yaml
terabox:
  retry:
    max-attempts: 3      # Number of retry attempts
    backoff-delay: 2000  # Initial delay in milliseconds
```

### Adjust Logging Level

Edit `application.yml`:
```yaml
logging:
  level:
    com.terabox.api: DEBUG  # Options: TRACE, DEBUG, INFO, WARN, ERROR
```

## Troubleshooting

### Issue: "No cookies configured"

**Solution:** Make sure you've added at least one cookie in `application.yml`

### Issue: "Cookie is invalid - getting login page"

**Solution:** Your cookies have expired. Get fresh cookies from TeraBox website.

### Issue: "Failed to extract required tokens"

**Solution:** 
- Check if the URL is valid
- Verify your cookies are working
- Check if TeraBox website structure has changed

### Issue: "Connection timeout"

**Solution:** 
- Check your internet connection
- Increase timeout values in `application.yml`
- Check if TeraBox servers are accessible

### Issue: Build fails with "Java version" error

**Solution:** Make sure you have Java 17 or higher installed

## API Response Structure

### Success Response

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
      "isdir": "0",
      "local_ctime": "1752858248",
      "local_mtime": "1752858248",
      "md5": "3ac45c16e3b5efd76405189a44a74f44",
      "path": "/filename.mkv",
      "play_forbid": "0",
      "server_ctime": "1752858248",
      "server_filename": "filename.mkv",
      "server_mtime": "1753344009",
      "size": "1432461005",
      "thumbs": {
        "url1": "https://...",
        "url2": "https://...",
        "url3": "https://...",
        "icon": "https://..."
      },
      "emd5": "e296aab0bj6f91f945840a23edaed575",
      "dlink": "https://...",
      "direct_link": "https://...",
      "stream_url": "https://..."
    }
  ],
  "share_id": 68536951105,
  "uk": 4402236204928
}
```

### Error Response

```json
{
  "errno": -1,
  "error": "Error message",
  "request_id": 9141406616576050447,
  "server_time": 1761125258
}
```

## Production Deployment

### 1. Build for Production

```bash
mvn clean package -DskipTests
```

### 2. Run with Production Profile

Create `application-prod.yml` with production settings, then:

```bash
java -jar target/terabox-api-sp-1.0.0.jar --spring.profiles.active=prod
```

### 3. Run as a Service (Linux)

Create a systemd service file `/etc/systemd/system/terabox-api.service`:

```ini
[Unit]
Description=TeraBox API Service
After=network.target

[Service]
Type=simple
User=your-user
WorkingDirectory=/path/to/terabox-api-sp
ExecStart=/usr/bin/java -jar /path/to/terabox-api-sp/target/terabox-api-sp-1.0.0.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

Then:
```bash
sudo systemctl daemon-reload
sudo systemctl enable terabox-api
sudo systemctl start terabox-api
```

### 4. Run with Docker (Optional)

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/terabox-api-sp-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t terabox-api .
docker run -p 8080:8080 terabox-api
```

## Support

For issues or questions:
1. Check the logs in the console
2. Enable DEBUG logging for more details
3. Verify your cookies are valid
4. Test with a known working TeraBox URL

## Next Steps

- Add more cookies for better load distribution
- Monitor logs for errors
- Set up proper logging to files
- Configure reverse proxy (nginx) for production
- Add monitoring and health checks

