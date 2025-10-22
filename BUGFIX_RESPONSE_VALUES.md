# Bug Fix: Using Actual API Response Values

## Issue Identified

The initial implementation was **generating its own values** instead of using the actual values from TeraBox API response.

### What Was Wrong

#### 1. Response Metadata (FIXED ✅)

**Before:**
```java
return TeraBoxResponse.builder()
    .errno(0)
    .requestId(System.currentTimeMillis() * 1000)  // ❌ Generated
    .serverTime(System.currentTimeMillis() / 1000) // ❌ Generated
    .cfromId(0)                                     // ❌ Hardcoded
    .title(processedFiles.isEmpty() ? "" : ...)    // ❌ Constructed
    .shareId(shareId)                              // ✅ From API
    .uk(uk)                                        // ✅ From API
    .build();
```

**After:**
```java
// Extract metadata from API response
long requestId = apiResponse.path("request_id").asLong(...);
long serverTime = apiResponse.path("server_time").asLong(...);
int cfromId = apiResponse.path("cfrom_id").asInt(0);
String title = apiResponse.path("title").asText("");
long shareId = apiResponse.path("share_id").asLong(0);
long uk = apiResponse.path("uk").asLong(0);

return TeraBoxResponse.builder()
    .errno(errno)           // ✅ From API
    .requestId(requestId)   // ✅ From API
    .serverTime(serverTime) // ✅ From API
    .cfromId(cfromId)       // ✅ From API
    .title(title)           // ✅ From API
    .shareId(shareId)       // ✅ From API
    .uk(uk)                 // ✅ From API
    .build();
```

#### 2. File Item Fields (FIXED ✅)

**Before:**
```java
return FileItem.builder()
    .isdir("0")                                    // ❌ Hardcoded
    .path("/" + fileNode.path("server_filename"))  // ❌ Constructed
    .playForbid("0")                               // ❌ Hardcoded
    .localCtime(fileNode.path("local_ctime"))      // ✅ From API
    .localMtime(fileNode.path("local_mtime"))      // ✅ From API
    .build();
```

**After:**
```java
return FileItem.builder()
    .isdir(fileNode.path("isdir").asText("0"))          // ✅ From API
    .path(fileNode.path("path").asText(""))             // ✅ From API
    .playForbid(fileNode.path("play_forbid").asText("0")) // ✅ From API
    .localCtime(fileNode.path("local_ctime").asText(""))  // ✅ From API
    .localMtime(fileNode.path("local_mtime").asText(""))  // ✅ From API
    .build();
```

#### 3. Thumbnail URLs (FIXED ✅)

**Before:**
```java
Thumbs thumbs = Thumbs.builder()
    .url1(thumbsNode.path("url3").asText(...))  // ❌ Wrong field
    .url2(thumbsNode.path("url3").asText(...))  // ❌ Wrong field
    .url3(thumbsNode.path("url3").asText(...))  // ✅ Correct
    .icon(thumbsNode.path("url3").asText(...))  // ❌ Wrong field
    .build();
```

**After:**
```java
Thumbs thumbs = Thumbs.builder()
    .url1(thumbsNode.path("url1").asText(...))  // ✅ Correct
    .url2(thumbsNode.path("url2").asText(...))  // ✅ Correct
    .url3(thumbsNode.path("url3").asText(...))  // ✅ Correct
    .icon(thumbsNode.path("icon").asText(...))  // ✅ Correct
    .build();
```

## Why This Matters

### 1. **Authenticity**
The API should return the **exact data from TeraBox**, not fabricated values.

### 2. **Consistency**
- `request_id` from TeraBox is unique and traceable
- `server_time` reflects TeraBox's actual server time
- `title` is the actual folder/file title from TeraBox

### 3. **Correctness**
- File `path` might include folder structure (e.g., `/folder/subfolder/file.mkv`)
- `isdir` could be "1" for folders
- `play_forbid` might be "1" for restricted files

### 4. **Debugging**
Using actual values makes it easier to:
- Trace requests back to TeraBox
- Compare responses with TeraBox's official API
- Debug issues

## What We're Doing Now

### ✅ Correct Approach

1. **Hit TeraBox API** with constructed URL
2. **Receive JSON response** with all fields
3. **Extract values** from the response
4. **Pass through** the values to our response
5. **Only add** what we compute ourselves:
   - `direct_link` (from HEAD request)
   - `stream_url` (constructed for video playback)

### Example Flow

```
1. Client Request
   ↓
2. TeraBox API Call
   ↓
3. TeraBox Response:
   {
     "errno": 0,
     "request_id": 9141406616576050447,
     "server_time": 1761125258,
     "cfrom_id": 0,
     "title": "/My Files/video.mkv",
     "list": [
       {
         "fs_id": "96610459486545",
         "path": "/My Files/video.mkv",
         "isdir": "0",
         "local_ctime": "1752858248",
         "local_mtime": "1752858248",
         "play_forbid": "0",
         ...
       }
     ],
     "share_id": 68536951105,
     "uk": 4402236204928
   }
   ↓
4. We Extract & Pass Through:
   - errno: 0 (from response)
   - request_id: 9141406616576050447 (from response)
   - server_time: 1761125258 (from response)
   - cfrom_id: 0 (from response)
   - title: "/My Files/video.mkv" (from response)
   - share_id: 68536951105 (from response)
   - uk: 4402236204928 (from response)
   ↓
5. We Add Our Computed Values:
   - direct_link (from HEAD request to dlink)
   - stream_url (constructed for streaming)
   ↓
6. Return Complete Response
```

## Summary

### Before Fix
- ❌ Generated our own `request_id` and `server_time`
- ❌ Hardcoded `cfrom_id`, `isdir`, `play_forbid`
- ❌ Constructed `title` and `path`
- ❌ Used wrong thumbnail fields

### After Fix
- ✅ Extract all values from TeraBox API response
- ✅ Pass through authentic data
- ✅ Only add computed values (`direct_link`, `stream_url`)
- ✅ Use correct field mappings

## Files Modified

1. `TeraBoxExtractor.java` - `fetchData()` method
   - Extract `request_id`, `server_time`, `cfrom_id`, `title` from API response

2. `TeraBoxExtractor.java` - `processFile()` method
   - Use `isdir`, `path`, `play_forbid` from API response
   - Use correct thumbnail field mappings

## Testing

After this fix, the API will return:
- ✅ Authentic TeraBox metadata
- ✅ Correct file paths (including folder structure)
- ✅ Correct `isdir` values
- ✅ Correct thumbnail URLs
- ✅ Traceable `request_id` from TeraBox

The response will now be a **true pass-through** of TeraBox data with our added enhancements (`direct_link` and `stream_url`).

