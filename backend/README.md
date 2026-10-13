# WhatsOnMyPlate — Backend Service

## Purpose

The `backend` module serves as the core REST API and AI engine for the **WhatsOnMyPlate** culinary assistant application. It provides endpoints for dish scanning, recipe discovery, pantry management, community interactions, user authentication, multimodal AI analysis, and cloud media storage.

---

## Technology Stack

- **Java**: 17 (LTS)
- **Framework**: Spring Boot 3.4.3
- **AI Framework**: Spring AI 1.0.0-M6 (Google Gemini / OpenAI-compatible API)
- **AI Model**: Google Gemini Flash (`gemini-3.6-flash`)
- **Media Storage**: Cloudinary Java SDK 1.39.0 (`cloudinary-http44`)
- **Security**: Spring Security 6, BCrypt Password Encoding
- **Token Format**: JSON Web Tokens (JJWT 0.12.6, HMAC-SHA256)
- **Web**: Spring Web (Spring MVC)
- **Data Access**: Spring Data JPA / Hibernate 6.6.8
- **Database Migrations**: Flyway 10.x (`flyway-core`, `flyway-mysql`)
- **Database Driver**: MySQL Connector/J
- **Database**: MySQL Server 8.x (Local)
- **Validation**: Jakarta Validation / Hibernate Validator
- **Monitoring & Metrics**: Spring Boot Actuator
- **Build Tool**: Apache Maven 3.9+
- **Testing**: JUnit 5, Spring Boot Starter Test, MockMvc, Spring Security Test

---

## Package Structure (`com.whatsonmyplate`)

```text
com.whatsonmyplate
├── WhatsOnMyPlateApplication.java  # Main application entry point
├── config/                         # Cross-origin (CORS), Cloudinary, and web MVC configurations
│   └── CloudinaryConfig.java       # Singleton Cloudinary bean provider
├── controller/                     # REST controllers (Health, Auth, Recipe, etc.)
│   ├── AuthController.java         # POST /register, POST /login, GET /me
│   ├── HealthController.java       # GET /api/health
│   └── RecipeController.java       # Recipe CRUD & Image Analysis endpoints
├── dto/                            # Data transfer objects and API response schemas
│   ├── ErrorResponse.java
│   ├── ai/                         # Structured AI models (AiRecipeResult, AiIngredient, AiInstruction)
│   ├── auth/                       # Auth DTOs (RegisterRequest, LoginRequest, AuthResponse, UserProfileDto)
│   ├── recipe/                     # Recipe DTOs (RecipeCreateRequest, RecipeUpdateRequest, RecipeResponse, RecipePageResponse, etc.)
│   └── storage/                    # Storage DTOs (ImageUploadResult)
├── entity/                         # JPA domain entity models
│   ├── User.java                   # User accounts & audit
│   ├── Recipe.java                 # Recipe catalog & AI meal metadata
│   ├── PantryItem.java             # User ingredient stock inventory
│   ├── CommunityPost.java          # Community meal remake creations
│   ├── Comment.java                # Comments on community posts
│   ├── Rating.java                 # 1-5 star reviews for recipes / posts
│   ├── SavedRecipe.java            # User bookmark junction table
│   ├── enums/                      # RecipeSource, SpiceLevel
│   └── model/                      # IngredientItem, InstructionStep (JSON columns)
├── exception/                      # Global exception handlers (AiServiceException, ImageStorageException, etc.)
├── repository/                     # Spring Data JPA repositories
├── security/                       # Security filters and token utilities
│   ├── SecurityConfig.java         # Stateless SecurityFilterChain, BCrypt, and CORS
│   ├── CustomUserDetailsService.java # UserDetailsService loading user by email
│   ├── JwtService.java             # JWT generation, signature verification, claim extraction
│   ├── JwtAuthenticationFilter.java# Intercepts Bearer tokens and authenticates SecurityContext
│   └── JwtAuthenticationEntryPoint.java # Formats 401 Unauthorized JSON responses
├── service/                        # Business logic interfaces and service implementations
│   ├── AuthService.java            # Registration, authentication, and profile lookup
│   ├── RecipeService.java          # Recipe CRUD, pagination, and sorting
│   ├── ImageRecipeService.java     # Image validation, Cloudinary upload, Gemini analysis orchestration
│   ├── RecipePersistenceService.java # Transactional persistence of AI recipes across Spring proxy
│   ├── ai/                         # AI services (AiRecipeService, GeminiAiService, Prompts, Validator)
│   └── storage/                    # Storage services (ImageStorageService, CloudinaryImageStorageService)
└── util/                           # Shared utility helpers and constants
```

---

## Cloudinary Image Storage

WhatsOnMyPlate integrates Cloudinary for cloud media storage via the official `cloudinary-http44` Java SDK.

### Architecture & Folder Structure

- **Storage Abstraction (`ImageStorageService`)**: Defines cloud storage operations (`uploadImage`, `deleteImage`).
- **Cloudinary Implementation (`CloudinaryImageStorageService`)**:
  - Uploads validated images to the designated folder: `whatsonmyplate/recipes`.
  - Generates secure random UUID public IDs.
  - Returns permanent HTTPS secure URLs (`secure_url`).
- **Fail-Safe Cleanup**: If Gemini analysis, AI schema validation, or database persistence fails following a successful Cloudinary upload, `ImageRecipeService` immediately invokes `deleteImage(publicId)` to prevent orphaned assets in Cloudinary.
- **Transaction Safety**: Cloudinary network uploads execute entirely outside the database transaction.

### Configuration & Environment Variables

| Variable | Description |
|---|---|
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret key |

> **Security Note**: Cloudinary credentials are strictly server-side only. They are never exposed via endpoints, never stored in MySQL, and never logged.

---

## Image-to-Recipe API

The Image-to-Recipe feature enables authenticated users to upload a photo of a dish, upload it to Cloudinary, have Google Gemini 2.5 Flash analyze the dish, validate the recipe schema, and persist the recipe with its permanent Cloudinary image URL into MySQL.

### Endpoint Details

- **Method & Path**: `POST /api/recipes/analyze-image`
- **Content-Type**: `multipart/form-data`
- **Authentication**: Required (`Authorization: Bearer <JWT>`)

### Parameters

| Field | Type | Required | Description |
|---|---|---|---|
| `image` | File (`MultipartFile`) | **Yes** | Image of the prepared dish. Max size: **10 MB**. |
| `promptNotes` | String | *Optional* | Additional culinary instructions or dietary notes (max 1000 characters). |

### Supported Image Formats

- `image/jpeg` (`.jpg`, `.jpeg`)
- `image/png` (`.png`)
- `image/webp` (`.webp`)

---

## Google Gemini / Spring AI

WhatsOnMyPlate integrates Google Gemini via Spring AI for multimodal dish analysis and structured recipe generation.

### Configuration & Environment Variables

| Variable | Description | Default |
|---|---|---|
| `GEMINI_API_KEY` | Google AI Studio API Key | *(Must be configured for live AI)* |
| `GEMINI_MODEL` | Gemini Model identifier | `gemini-2.5-flash` |
| `GEMINI_BASE_URL` | Google GenAI API endpoint | `https://generativelanguage.googleapis.com/v1beta/openai` |

---

## Authentication & Authorization

WhatsOnMyPlate uses stateless JSON Web Token (JWT) authentication backed by BCrypt password hashing.

### Endpoints Overview

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Registers a new user, hashes password with BCrypt, returns JWT. |
| `POST` | `/api/auth/login` | Public | Authenticates credentials with AuthenticationManager, returns JWT. |
| `GET` | `/api/auth/me` | Authenticated | Returns current authenticated user profile without exposing password hashes. |
| `GET` | `/api/health` | Public | Health monitoring endpoint. |

---

## How to Build and Run

### 1. Run Automated Tests
```bash
mvn clean test
```

### 2. Run the Development Server
```bash
mvn spring-boot:run
```
