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

  사용자 데이터를 Gemini AI로 분석하여 인사이트 제공 /
  Analyze user data with Gemini AI and provide insights


- 🔁 **멀티 모델 / 멀티 키 지원 / Multi-Model & Multi-Key Support**

  여러 Gemini API 키를 활용한 병렬 처리 /
  Parallel processing using multiple Gemini API keys


- 🧵 **큐 기반 처리 / Queue-Based Processing**

  BlockingQueue + Worker Thread Pool 구조 /
  BlockingQueue + Worker Thread Pool architecture


- 🧠 **컨텍스트 관리 / Context Management**

  사용자별 전처리 데이터(PreparedContext) 저장 및 재사용 /
  Store and reuse per-user preprocessed data (PreparedContext)


- 🗂 **분석 히스토리 관리 / Analysis History Management**

  분석 요청 및 결과 이력 관리 /
  Manage analysis request and result history

---

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

**Request Body:**
```json
{
  "userId": "user-001",
  "purpose": "Analysis category",
  "userPrompt": [
    {
      "dataKey": "Session 1",
      "data": {
        "Analysis info key 1": "Analysis info 1",
        "Analysis info key 2": "Analysis info 2",
        "Analysis info key 3": "Analysis info 3"
      }
    },
    {
      "dataKey": "Session 2",
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

사용자별 전처리 데이터를 관리하는 API입니다. 모든 API는 `ContextResponse` 형식으로 응답합니다.

API for managing per-user preprocessed data. All APIs respond in `ContextResponse` format.

#### Save Prepared Data

```http
POST /api/v1/context/save
Content-Type: application/json
```

**Request Body:**
```json
{
  "userId": "user-001",
  "category": "Prepared data category",
  "data": {
    "Prepared data Key 1": "Prepared data content 1",
    "Prepared data Key 2": "Prepared data content 2"
  }
}
```

**Response:**
```json
{
  "resultCode": 200,
  "resultMsg": "OK",
  "context": {
    "userId": "user-001",
    "category": "Prepared data category",
    "data": {
      "Prepared data Key 1": "Prepared data content 1",
      "Prepared data Key 2": "Prepared data content 2"
    }
  }
}
```

#### Get Prepared Data

```http
POST /api/v1/context/get?userId=user-001
```

**Response:**
```json
{
  "resultCode": 200,
  "resultMsg": "OK",
  "context": {
    "userId": "user-001",
    "category": "Prepared data category",
    "data": {
      "Prepared data Key 1": "Prepared data content 1",
      "Prepared data Key 2": "Prepared data content 2"
    }
  }
}
```

#### Update Prepared Data

```http
POST /api/v1/context/update
Content-Type: application/json
```

**Request Body:**
```json
{
  "userId": "user-001",
  "category": "Prepared data category",
  "data": {
    "Prepared data Key 1": "Prepared data content 3",
    "Prepared data Key 2": "Prepared data content 4"
  }
}
```

**Response:**
```json
{
  "resultCode": 200,
  "resultMsg": "OK",
  "context": {
    "userId": "user-001",
    "category": "Prepared data category",
    "data": {
      "Prepared data Key 1": "Prepared data content 3",
      "Prepared data Key 2": "Prepared data content 4"
    }
  }
}
```

#### Delete Prepared Data

```http
POST /api/v1/context/delete?userId=user-001
```

**Response:**
```json
{
  "resultCode": 200,
  "resultMsg": "OK",
  "context": null
}
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

각 워커는 하나의 Gemini API 키를 전담하며, 요청은 큐를 통해 워커들에게 순환 분배됩니다.

Each worker is dedicated to a single Gemini API key, and requests are distributed to workers in a round-robin fashion through the queue.

1. **GeminiQueueManager**: 

요청 큐와 워커 스레드 풀을 관리합니다. 요청이 들어오면 큐에 적재되고, 유휴 워커가 순차적으로 처리합니다.
   
Manages request queue and worker thread pool. Incoming requests are queued and processed by available workers sequentially.

2. **GeminiWorker**: 

각 워커는 **1:1로 할당된 API 키**를 사용하여 Gemini API를 호출합니다. 워커 수 = API 키 수로 구성됩니다.

Each worker calls Gemini API using a **1:1 assigned API key**. Number of workers = Number of API keys.

3. **GeminiRequest**: 

요청 정보와 CompletableFuture를 포함하여 비동기 응답 처리를 지원합니다. 

Contains request information and CompletableFuture for asynchronous response handling.

4. **GeminiResponse**:

API 응답과 처리한 워커의 모델 정보를 포함합니다. 

Contains API response and the model information of the worker that processed it.

### ⭐ Key Features

- **1:1 Worker-API Key Mapping**: 

각 워커가 하나의 API 키를 전담하여 Rate Limit을 분산 관리 / Each worker is dedicated to one API key for distributed rate limit management

- **Round-Robin Request Distribution**: 

요청이 큐를 통해 워커들에게 순환 분배 / Requests are distributed to workers in round-robin fashion through the queue

- **Asynchronous Processing**: 

CompletableFuture를 통한 비동기 응답 처리 / Asynchronous response handling via CompletableFuture

- **Rate Limit Handling**:

다중 API 키로 처리량 확보 / Ensures throughput with multiple API keys

- **Graceful Shutdown**: 

종료 시 대기 중인 요청 완료 후 종료 / Completes pending requests during shutdown

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
