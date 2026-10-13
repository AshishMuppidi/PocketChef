# WhatsOnMyPlate

## Description

**WhatsOnMyPlate** is an AI-powered culinary assistant and recipe platform that bridges the gap between food photography, personalized cooking, and community sharing. Users can upload or snap a photo of any prepared dish to identify the meal using multimodal vision AI, receive ingredient breakdowns and step-by-step preparation instructions, dynamically adjust spice levels, receive dietary ingredient substitutions, manage pantry stocks, and share meal remakes and reviews with fellow home cooks.

---

## Features

* **Multimodal AI Dish Recognition**: Upload or capture food photos; the backend utilizes Google Gemini multimodal AI via Spring AI to analyze the dish, extract ingredients, and generate complete culinary recipes.
* **Smart Ingredient Substitution**: Intelligent alternative recommendations for missing ingredients or dietary restrictions.
* **Spice Level Adjustment**: Dynamic seasoning and heat calibration across `MILD`, `MEDIUM`, `SPICY`, and `EXTRA_SPICY` settings.
* **Recipe Catalog & Search**: Browse and filter curated and AI-generated recipes by keyword query, maximum preparation time, recipe source, and spice level.
* **Pantry Inventory Management**: Keep track of on-hand kitchen ingredients with quantity/unit tracking and discover recipes matching currently available stock.
* **Community Feed & Remakes**: Post photo remakes of recipes, share culinary tips, write captions, assign 1-5 star ratings, comment on posts, and like creations.
* **Stateless JWT Authentication**: Secure user registration, authentication, and profile retrieval using Spring Security, BCrypt password hashing, and HMAC-SHA256 tokens.
* **Cloud Media Storage**: Automatic cloud image uploads via Cloudinary SDK with fail-safe cleanup if downstream processing fails.
* **Client-Side Image Optimization**: HTML5 Canvas image resizing and compression before upload to optimize bandwidth and processing speed.

---

## Tech Stack

### Frontend
* **Framework**: React 18
* **Build Tool**: Vite 6
* **Styling**: Tailwind CSS v4
* **Routing**: React Router 6
* **HTTP Client**: Axios (with centralized interceptors and error handling)
* **Icons**: Lucide React
* **Image Processing**: HTML5 Canvas API (browser compression)

### Backend
* **Language / Runtime**: Java 17 (LTS)
* **Framework**: Spring Boot 3.4.3
* **Web**: Spring MVC / Spring Web
* **Data Access**: Spring Data JPA / Hibernate
* **Validation**: Jakarta Validation / Hibernate Validator
* **AI Integration**: Spring AI (OpenAI-compatible client integration for Google Gemini)
* **AI Model**: Google Gemini Flash (`gemini-2.5-flash`)
* **Cloud Storage**: Cloudinary Java SDK (`cloudinary-http44`)
* **Monitoring**: Spring Boot Actuator

### Database & Migrations
* **Database**: MySQL Server 8.x
* **Migrations**: Flyway 10.x (`flyway-core`, `flyway-mysql`)
* **Data Types**: Native MySQL JSON columns for ingredients and instructions

### Authentication & Security
* **Security Framework**: Spring Security 6 (stateless filter chain)
* **Tokens**: JSON Web Tokens (JJWT 0.12.6, HMAC-SHA256)
* **Password Encoding**: BCrypt (strength 10)
* **CORS**: Configurable cross-origin resource sharing

### Infrastructure / Deployment
* **Container / Runtime**: Java 17 JVM (Spring Boot standalone executable JAR)
* **Frontend Hosting**: Static SPA hosting (Vite build output)
* *Status*: Application is fully container/cloud-ready; no live production deployment currently active.

### Testing
* **Backend**: JUnit 5, Spring Boot Starter Test, Mockito, MockMvc, Spring Security Test (126 automated tests)
* **Frontend**: Production build verification via Vite

---

## Project Structure

```text
WhatsOnMyPlate/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/whatsonmyplate/
│   │   │   │   ├── config/              # Cloudinary bean configuration
│   │   │   │   ├── controller/          # REST controllers (Auth, Recipe, Pantry, Community, Health)
│   │   │   │   ├── dto/                 # Request/response schemas (AI, Auth, Community, Pantry, Recipe)
│   │   │   │   ├── entity/              # JPA domain entities (User, Recipe, PantryItem, CommunityPost, etc.)
│   │   │   │   ├── exception/           # Global exception handling and error responses
│   │   │   │   ├── repository/          # Spring Data JPA repositories and custom queries
│   │   │   │   ├── security/            # SecurityFilterChain, JWT filter, service, and entry point
│   │   │   │   └── service/             # Core business logic, AI orchestration, and cloud storage
│   │   │   └── resources/
│   │   │       ├── db/migration/        # Flyway SQL migrations (V1 initial schema, V2 likes)
│   │   │       ├── application.yml      # Base Spring Boot configuration
│   │   │       └── application-dev.yml  # Development profile configuration
│   │   └── test/                        # 126 unit and integration test suites
│   ├── .env.example                     # Template for backend environment variables
│   └── pom.xml                          # Maven build configuration
├── frontend/
│   ├── src/
│   │   ├── api/                         # Axios client instance and interceptors
│   │   ├── components/                  # Reusable UI components (Navbar, Footer, Recipe, Pantry, Scan)
│   │   ├── context/                     # React Contexts (AuthContext)
│   │   ├── layouts/                     # RootLayout application shell
│   │   ├── pages/                       # Page views (Home, ScanDish, RecipeDetails, Search, Pantry, etc.)
│   │   └── utils/                       # Utilities (API errors, auth storage, canvas image compression)
│   ├── .env.example                     # Template for frontend environment variables
│   ├── package.json                     # Frontend dependencies and scripts
│   └── vite.config.js                   # Vite and Tailwind CSS plugins
├── docs/                                # Project documentation assets
├── .gitignore                           # Git ignore rules for build, IDE, OS, and env artifacts
└── README.md                            # Project documentation
```

---

## Local Setup

### 1. Requirements
* **Java**: JDK 17+
* **Maven**: 3.9+
* **Node.js**: 18+ (tested with Node 22+)
* **npm**: 9+
* **MySQL Server**: 8.x running locally

### 2. Database Setup
1. Start your local MySQL service on port `3306`.
2. Connect to MySQL as `root`:
   ```sql
   CREATE DATABASE whatsonmyplate;
   CREATE USER 'whatson'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON whatsonmyplate.* TO 'whatson'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Flyway migrations in `backend/src/main/resources/db/migration/` automatically apply the database schema upon backend launch.

### 3. Environment Variables Setup
* **Backend**: Copy `backend/.env.example` to `backend/.env` or export environment variables:
  ```bash
  cd backend
  cp .env.example .env
  ```
  Configure your database credentials, JWT secret, Gemini API key, and Cloudinary keys in `backend/.env`.

* **Frontend**: Copy `frontend/.env.example` to `frontend/.env`:
  ```bash
  cd frontend
  cp .env.example .env
  ```

### 4. Backend Startup
```bash
cd backend
mvn spring-boot:run
```
The backend starts on `http://localhost:8080`.
* Health check: `http://localhost:8080/api/health`

### 5. Frontend Startup
```bash
cd frontend
npm install
npm run dev
```
The frontend development server starts on `http://localhost:5173`.

---

## Environment Variables

### Backend (`backend/.env`)

| Variable | Description | Required / Default |
|---|---|---|
| `DB_HOST` | MySQL hostname | Optional (default: `localhost`) |
| `DB_PORT` | MySQL port | Optional (default: `3306`) |
| `DB_NAME` | MySQL database name | Optional (default: `whatsonmyplate`) |
| `DB_USERNAME` | MySQL user | Optional (default: `root`) |
| `DB_PASSWORD` | MySQL password | **Required for live DB** |
| `JWT_SECRET` | Secret key for JWT signing (256+ bits) | **Required for production** |
| `JWT_EXPIRATION_MS` | JWT validity period in milliseconds | Optional (default: `86400000` = 24h) |
| `GEMINI_API_KEY` | Google AI Studio API key | **Required for live AI dish recognition** |
| `GEMINI_MODEL` | Gemini model name | Optional (default: `gemini-2.5-flash`) |
| `GEMINI_BASE_URL` | Google Gemini OpenAI endpoint | Optional (default: Google Generative Language API) |
| `CLOUDINARY_CLOUD_NAME`| Cloudinary cloud name | **Required for image uploads** |
| `CLOUDINARY_API_KEY` | Cloudinary API key | **Required for image uploads** |
| `CLOUDINARY_API_SECRET`| Cloudinary API secret | **Required for image uploads** |
| `CORS_ALLOWED_ORIGINS` | Permitted frontend origins (comma-separated) | Optional (default: `http://localhost:5173`) |

### Frontend (`frontend/.env`)

| Variable | Description | Required / Default |
|---|---|---|
| `VITE_API_BASE_URL` | Base URL of the backend REST API | Optional (default: `http://localhost:8080/api`) |

---

## API Endpoints

### Public Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Service health status |
| `POST` | `/api/auth/register` | Register new user account and obtain JWT |
| `POST` | `/api/auth/login` | Authenticate credentials and obtain JWT |
| `GET` | `/api/recipes` | Search and list recipes with pagination and filtering |
| `GET` | `/api/recipes/{id}` | Retrieve recipe details by ID |
| `GET` | `/api/community/posts` | Paginated community remakes feed |
| `GET` | `/api/community/posts/{id}` | Retrieve community post by ID |
| `GET` | `/api/community/posts/{id}/comments` | List comments for a community post |
| `GET` | `/api/community/posts/{id}/rating` | Get current user's rating and post average |

### Authenticated Endpoints (`Authorization: Bearer <JWT>`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/auth/me` | Fetch authenticated user profile |
| `POST` | `/api/recipes` | Create custom recipe |
| `POST` | `/api/recipes/analyze-image` | Upload food photo for AI recipe extraction (`multipart/form-data`) |
| `PUT` | `/api/recipes/{id}` | Update existing recipe |
| `DELETE` | `/api/recipes/{id}` | Delete recipe |
| `POST` | `/api/recipes/{id}/substitutions` | Get AI ingredient substitutions |
| `POST` | `/api/recipes/{id}/spice-adjustment` | Calculate AI spice level adjustment |
| `GET` | `/api/pantry` | List user pantry inventory |
| `POST` | `/api/pantry` | Add pantry item |
| `PUT` | `/api/pantry/{id}` | Update pantry item quantity/unit |
| `DELETE` | `/api/pantry/{id}` | Remove pantry item |
| `GET` | `/api/pantry/recipes` | Find recipes matching available pantry ingredients |
| `POST` | `/api/community/posts` | Create community remake post with photo upload |
| `PUT` | `/api/community/posts/{id}` | Edit community post caption |
| `DELETE` | `/api/community/posts/{id}` | Delete community post |
| `POST` | `/api/community/posts/{id}/like` | Like community post |
| `DELETE` | `/api/community/posts/{id}/like` | Unlike community post |
| `POST` | `/api/community/posts/{id}/comments` | Add comment to community post |
| `DELETE` | `/api/community/comments/{commentId}` | Delete comment |
| `POST` | `/api/community/posts/{id}/rating` | Submit 1-5 star rating for a remake post |

---

## Testing

### Backend Tests
Execute the full JUnit 5 and Spring Boot test suite:
```bash
cd backend
mvn clean test
```
*Current test suite*: **126 tests, 0 failures, 0 errors**.

### Frontend Verification
Verify module imports and production bundle integrity:
```bash
cd frontend
npm run build
```

---

## Production Build

### Backend JAR Packaging
```bash
cd backend
mvn clean package -DskipTests
```
Generates the executable JAR in `backend/target/backend-0.0.1-SNAPSHOT.jar`. Run with:
```bash
java -jar backend/target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend Static Build
```bash
cd frontend
npm run build
```
Outputs optimized production assets to `frontend/dist/`.

---

## Deployment Architecture

* **Backend**: Packaged as a self-contained Spring Boot executable JAR running on Java 17+. Configurable completely via standard environment variables (12-factor application pattern). Connects to a managed MySQL 8.x database instance.
* **Frontend**: Standalone Single Page Application (SPA) compiled to static HTML, CSS, and JavaScript. Can be deployed to static hosting or CDN platforms (such as Vercel, Netlify, AWS S3 + CloudFront, or Nginx) with reverse proxying or CORS pointing to the backend API.
* **Storage & AI**: Media is hosted securely via Cloudinary CDN, while vision/text AI inference is performed through Google Gemini API.

