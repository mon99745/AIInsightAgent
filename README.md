# 🤖 AIInsightAgent, AIA

**Google Gemini API**를 활용한 **데이터 분석 및 인사이트 도출 플랫폼**입니다.
큐 기반 비동기 아키텍처를 통해 **높은 처리량**과 **안정적인 AI 호출**을 제공합니다.

A **data analysis and insight generation platform** powered by **Google Gemini API**.
Provides **high throughput** and **reliable AI calls** through queue-based asynchronous architecture.

---

## 📌 Table of Contents

1. [Project Introduction](#-project-introduction)
2. [Tech Stack](#-tech-stack)
3. [Project Structure](#-project-structure)
4. [Configuration](#-configuration)
5. [API Specification](#-api-specification)
6. [Architecture](#-architecture)
7. [Testing](#-testing)

---

## 🧩 Project Introduction

### 🔍 Overview

**AIInsightAgent**는 Google Gemini AI를 기반으로 데이터를 분석하고,
구조화된 인사이트를 도출하는 **RESTful API 서비스**입니다.

다수의 Gemini API 키를 활용한 **큐 기반 비동기 처리 아키텍처**를 통해
높은 처리량과 안정적인 요청 처리를 지원합니다.

**AIInsightAgent** is a **RESTful API service** that analyzes data based on Google Gemini AI
and generates structured insights.

It supports high throughput and reliable request processing through a **queue-based asynchronous processing architecture**
utilizing multiple Gemini API keys.

---

### 🚀 Key Features

- 📊 **데이터 분석 / Data Analysis**
  사용자 데이터를 Gemini AI로 분석하여 인사이트 제공
  Analyze user data with Gemini AI and provide insights
- 🔁 **멀티 모델 / 멀티 키 지원 / Multi-Model & Multi-Key Support**
  여러 Gemini API 키를 활용한 병렬 처리
  Parallel processing using multiple Gemini API keys
- 🧵 **큐 기반 처리 / Queue-Based Processing**
  BlockingQueue + Worker Thread Pool 구조
  BlockingQueue + Worker Thread Pool architecture
- 🧠 **컨텍스트 관리 / Context Management**
  사용자별 전처리 데이터(PreparedContext) 저장 및 재사용
  Store and reuse per-user preprocessed data (PreparedContext)
- 🗂 **분석 히스토리 관리 / Analysis History Management**
  분석 요청 및 결과 이력 관리
  Manage analysis request and result history

## 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.x |
| Build Tool | Gradle 8.x |
| Database | MariaDB |
| ORM | Spring Data JPA, Hibernate |
| AI | Google Gemini API (google-genai 1.24.0) |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito |

---

## 🗂 Project Structure

### 📦 Module Configuration

```
AIInsightAgent/
├── aia-app/          # Main application module (Controller, Service, Repository)
├── aia-core/         # Core business logic module (Gemini integration, Queue management)
└── aia-common/       # Common utility module
```

### 📁 Directory Structure

```
aia-app/
└── src/main/java/com/aiinsightagent/app/
    ├── controller/       # REST API controllers
    ├── service/          # Business services
    ├── repository/       # Data access layer
    ├── entity/           # JPA entities
    ├── enums/            # Enumerations
    ├── exception/        # Exception handling
    └── util/             # Utilities

aia-core/
└── src/main/java/com/aiinsightagent/core/
    ├── adapter/          # Gemini API adapter
    ├── config/           # Configuration classes
    ├── context/          # ThreadLocal context
    ├── exception/        # Exception definitions
    ├── facade/           # Facade pattern implementation
    ├── model/            # Domain models
    ├── parser/           # Response parser
    ├── preprocess/       # Preprocessing logic
    ├── queue/            # Queue manager and workers
    └── util/             # Utilities
```

---

## ⚙ Configuration

#### 🤖 Gemini API Configuration

```yaml
spring:
  ai:
    gemini:
      models:
        - id: m00
          name: gemini-2.5-flash
          api-key: ${GEMINI_API_KEY_0:}
        - id: m01
          name: gemini-2.5-flash
          api-key: ${GEMINI_API_KEY_1:}
        # Up to 10 models can be configured
      temperature: 0.7
      max-output-tokens: 8192
```

#### 🧵 Queue / Worker Configuration

```yaml
aiinsight:
  request:
    queue:
      worker-count: 10           # Number of concurrent workers
      queue-capacity: 100        # Maximum queue size
      request-timeout-seconds: 60    # Request timeout
      shutdown-timeout-seconds: 30   # Shutdown wait time
```

#### 🗄 Database Configuration

```yaml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/analysis_platform
    username: sa
    password: your-password
```

---

## 📡 API Specification

### 📊 Data Insight API

#### Request Data Analysis

```http
POST /api/v1/analysis
Content-Type: application/json
```

**Example Request Body:**
```json
{
  "userId": "user-001",
  "purpose": "Analysis category",
  "userPrompt": [
    {
      "dataKey": "Session ID",
      "data": {
        "Analysis info key 1": "Analysis info 1",
        "Analysis info key 2": "Analysis info 2",
        "Analysis info key 3": "Analysis info 3"
      }
    }
  ]
}
```

**Response:**
```json
{
  "resultCode": 200,
  "resultMsg": "OK",
  "insight": {
    "summary": "Analysis summary",
    "issueCategories": [
      {
        "category": "Performance",
        "description": "Issue description",
        "severity": "HIGH"
      }
    ],
    "rootCauseInsights": ["Root cause analysis 1", "Root cause analysis 2"],
    "recommendedActions": ["Recommended action 1", "Recommended action 2"],
    "priorityScore": 75
  }
}
```

#### Get Analysis History

```http
GET /api/v1/analysis/history?userId=user-001
```

### Prepared Context API

#### Save Prepared Data

```http
POST /api/v1/context/save
Content-Type: application/json
```

**Example Request Body:**
```json
{
  "userId": "user-001",
  "contextType": "Prepared data category",
  "payload": "Prepared data content"
}
```

#### Get Prepared Data

```http
POST /api/v1/context/get?userId=user-001
```

#### Update Prepared Data

```http
POST /api/v1/context/update
```

#### Delete Prepared Data

```http
POST /api/v1/context/delete?userId=user-001
```

---

## 🏗 Architecture

### Request Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                           Client Request                          │
└──────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│                          InsightController                        │
│                         (REST API Endpoint)                       │
└──────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│                           InsightService                          │
│                   (Business Logic, Transaction Management)        │
└──────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│                          InsightFacade                            │
│                   (Prompt Composition, Response Parsing)          │
└──────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│                         GeminiChatAdapter                         │
│                     (Gemini API Call Abstraction)                 │
└──────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│                        GeminiQueueManager                         │
│                   (BlockingQueue + Worker Thread Pool)            │
└──────────────────────────────────────────────────────────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                    ▼
┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│   GeminiWorker[0]   │ │   GeminiWorker[1]   │ │   GeminiWorker[N]   │
│   (API Key: m00)    │ │   (API Key: m01)    │ │   (API Key: m0N)    │
└─────────────────────┘ └─────────────────────┘ └─────────────────────┘
              │                    │                    │
              └────────────────────┼────────────────────┘
                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│                       Google Gemini API                           │
└──────────────────────────────────────────────────────────────────┘
```

### Queue-Based Processing Structure

1. **GeminiQueueManager**: Manages request queue and worker thread pool
2. **GeminiWorker**: Each worker calls Gemini API with assigned API key
3. **GeminiRequest**: Contains request information and CompletableFuture
4. **GeminiResponse**: Contains API response and processed worker's model information

### ⭐ Key Features

- **Round-Robin Assignment**: Assigns API keys to workers in rotation
- **Asynchronous Processing**: Asynchronous response handling via CompletableFuture
- **Rate Limit Handling**: Ensures throughput with multiple API keys
- **Graceful Shutdown**: Completes pending requests during shutdown

---

## 🧪 Testing

### Run Unit Tests

```bash
./gradlew test
```

### Test Specific Module

```bash
# Test aia-core module only
./gradlew :aia-core:test

# Test aia-app module only
./gradlew :aia-app:test
```

### Run Specific Test Class

```bash
./gradlew :aia-core:test --tests "com.aiinsightagent.core.adapter.GeminiChatAdapterTest"
```

### Test Reports

Check reports after running tests:
```
aia-core/build/reports/tests/test/index.html
aia-app/build/reports/tests/test/index.html
```
