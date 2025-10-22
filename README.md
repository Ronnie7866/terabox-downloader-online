# TeraBox API - Spring Boot Implementation

A simple and efficient Spring Boot API to extract file information from TeraBox share URLs.

## Features

- ✅ Single endpoint to process TeraBox URLs
- ✅ Returns official TeraBox API response structure
- ✅ Handles both single files and folders (recursive)
- ✅ Random cookie rotation for load balancing
- ✅ Automatic retry with exponential backoff
- ✅ No encryption, Redis, rate limiting, or client management
- ✅ Clean and simple implementation

## Requirements

- Java 17 or higher
- Maven 3.6+

## Configuration

### 1. Add Your Cookies

Edit `src/main/resources/application.yml` and add your TeraBox cookies:

```yaml
terabox:
  cookies:
    - "your_cookie_1_here"
    - "your_cookie_2_here"
    - "your_cookie_3_here"
```

### 2. Optional Configuration

You can also configure:
- HTTP timeouts
- Retry attempts and backoff delay
- Server port

## Build & Run

### Using Maven

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/terabox-api-sp-1.0.0.jar
```

### Using Maven Spring Boot Plugin

```bash
# Run directly
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## API Endpoints

### 1. Process TeraBox URL

**Endpoint:** `POST /api/terabox`

**Request Body:**
```json
{
  "url": "https://terabox.com/s/1xxxxx"
}
```

**Response (Success):**
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

**Response (Error):**
```json
{
  "errno": -1,
  "error": "Error message here",
  "request_id": 9141406616576050447,
  "server_time": 1761125258
}
```

### 2. Health Check

**Endpoint:** `GET /api/health`

**Response:**
```json
{
  "status": "OK",
  "message": "TeraBox API is running"
}
```

## How It Works

1. **URL Processing:**
   - Receives TeraBox URL
   - Attempts to get redirected URL
   - If redirect exists → uses it directly
   - If no redirect → extracts `surl` and builds canonical URL with `dm.1024tera.com`

2. **Data Extraction:**
   - Fetches HTML page with cookie
   - Extracts tokens: `dp-logid`, `jsToken`, `shorturl`
   - Calls TeraBox API to get file list
   - Processes files and folders recursively

3. **Cookie Management:**
   - Random selection from cookie pool
   - No rotation tracking (stateless)

4. **Retry Logic:**
   - Configurable retry attempts (default: 3)
   - Exponential backoff (default: 2 seconds base)

## Project Structure

```
terabox-api-sp/
├── src/main/java/com/terabox/api/
│   ├── TeraBoxApiApplication.java          # Main application
│   ├── config/
│   │   └── TeraBoxProperties.java          # Configuration properties
│   ├── controller/
│   │   └── TeraBoxController.java          # REST controller
│   ├── dto/
│   │   ├── TeraBoxRequest.java             # Request DTO
│   │   ├── TeraBoxResponse.java            # Response DTO
│   │   ├── FileItem.java                   # File item DTO
│   │   ├── Thumbs.java                     # Thumbnails DTO
│   │   └── ErrorResponse.java              # Error response DTO
│   ├── service/
│   │   ├── CookieManager.java              # Cookie rotation service
│   │   └── TeraBoxExtractor.java           # Core extraction logic
│   └── util/
│       ├── UrlValidator.java               # URL validation
│       └── StringUtils.java                # String parsing utilities
├── src/main/resources/
│   └── application.yml                     # Application configuration
├── pom.xml                                 # Maven dependencies
└── README.md                               # This file
```

## Testing

### Using cURL

```bash
curl -X POST http://localhost:8080/api/terabox \
  -H "Content-Type: application/json" \
  -d '{"url": "https://terabox.com/s/1xxxxx"}'
```

### Using Postman

1. Create a POST request to `http://localhost:8080/api/terabox`
2. Set header: `Content-Type: application/json`
3. Set body (raw JSON):
   ```json
   {
     "url": "https://terabox.com/s/1xxxxx"
   }
   ```

## Logging

The application uses SLF4J with Logback. Logs include:
- Request processing details
- Cookie selection
- URL redirection
- Token extraction
- File processing
- Errors and warnings

Log level can be configured in `application.yml`:
```yaml
logging:
  level:
    com.terabox.api: DEBUG  # Change to INFO, WARN, or ERROR
```

## Error Handling

The API handles various error scenarios:
- Invalid TeraBox URL → `errno: -1`, HTTP 400
- Missing URL → `errno: -1`, HTTP 400
- Cookie expired/invalid → `errno: -1`, HTTP 500
- Network errors → Automatic retry with backoff
- API errors → `errno: -1`, HTTP 500

## License

This project is provided as-is for educational purposes.

## Support

For issues or questions, please check the logs for detailed error messages.

