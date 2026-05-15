# 🦁 Sticker Safari

Digital sticker album application built with Spring Boot and React.

## 🚀 Quick Start

Run the entire application stack with one command:

```bash
docker-compose up --build
```

### Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Database**: PostgreSQL on localhost:5432

### Test Users

Login with these usernames: Bob, Jack, Anne, Ema

---

## 🏗️ Project Structure

- **backend/** - Spring Boot REST API (Java 21)
- **front/sticker-safari-frontend/** - React frontend (TypeScript)
- **docker-compose.yml** - Full stack orchestration

## 🧪 Running Tests

**Backend tests:**
```bash
cd backend
mvn test
```

**Frontend tests (fast - local):**
```bash
cd front/sticker-safari-frontend
npm install --force
npm test -- --watchAll=false
```

## 📦 Technology Stack

- **Backend**: Spring Boot 4.0.5, Java 21, PostgreSQL, Hibernate ORM
- **Frontend**: React 18, TypeScript, TailwindCSS, Axios
- **Infrastructure**: Docker, Docker Compose, nginx

## 💾 Database Management

- **Hibernate ORM** automatically creates and updates database schema from JPA entities
- **Initial data** is loaded automatically on first startup via `DataInitializer`
- Includes 4 collectors, 2 albums, and 10 stickers with sample collection data

## ✅ Test Status

- Backend: 10/10 tests passing ✅
- Frontend: 37/37 tests passing ✅

---

For detailed assignment description, see [backend/README.md](backend/README.md) and [front/sticker-safari-frontend/README.md](front/sticker-safari-frontend/README.md).
- That's all! No Java, Maven, Node.js, or PostgreSQL installation needed! 🎉

---

## 🛠️ Detailed Commands

### Start the Application
```bash
# Build and start all services
docker-compose up --build

# Start in detached mode (runs in background)
docker-compose up -d

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Stop the Application
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```

### Restart Specific Services
```bash
# Restart backend only
docker-compose restart backend

# Rebuild and restart backend
docker-compose up --build backend
```

### Check Service Status
```bash
docker-compose ps
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│  Docker Network: sticker-safari-network     │
├─────────────────────────────────────────────┤
│                                             │
│  ┌──────────────┐      ┌──────────────┐   │
│  │  Frontend    │      │   Backend    │   │
│  │  (React)     │─────▶│ (Spring Boot)│   │
│  │  Port: 3000  │      │  Port: 8080  │   │
│  └──────────────┘      └───────┬──────┘   │
│                                 │          │
│                         ┌───────▼──────┐   │
│                         │  PostgreSQL  │   │
│                         │  Port: 5432  │   │
│                         └──────────────┘   │
│                                             │
└─────────────────────────────────────────────┘
```

### Services

1. **postgres** - PostgreSQL 15 database with persistent volume
2. **backend** - Spring Boot REST API with Flyway migrations
3. **frontend** - React SPA served by nginx

---

## 🔧 Configuration

### Environment Variables

**Backend** (`docker-compose.yml`):
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

**Frontend** (`.env`):
- `REACT_APP_API_URL`: Backend API URL (default: http://localhost:8080)

### Ports

Change ports in `docker-compose.yml` if defaults conflict:

```yaml
services:
  postgres:
    ports:
      - "5432:5432"  # Change left side: "YOUR_PORT:5432"
  backend:
    ports:
      - "8080:8080"  # Change left side: "YOUR_PORT:8080"
  frontend:
    ports:
      - "3000:80"    # Change left side: "YOUR_PORT:80"
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Check what's using the port (Windows)
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <PID> /F

# Or change port in docker-compose.yml
```

### Database Connection Issues
```bash
# Check if postgres is healthy
docker-compose ps

# View postgres logs
docker-compose logs postgres

# Reset database
docker-compose down -v
docker-compose up postgres
```

### Frontend Can't Connect to Backend
```bash
# Check backend is running
curl http://localhost:8080/album/1

# Check backend logs
docker-compose logs backend

# Rebuild frontend with correct API URL
docker-compose up --build frontend
```

### Clear Everything and Start Fresh
```bash
# Remove all containers, networks, and volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Rebuild from scratch
docker-compose up --build
```

---

## 📦 Data Persistence

Database data persists in a Docker volume named `postgres_data`. To view:

```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect sticker-safari_postgres_data

# Remove volume (deletes all data!)
docker volume rm sticker-safari_postgres_data
```

---

## 🔄 Development Workflow

### Making Changes

**Backend Changes:**
```bash
# Edit Java files
# Rebuild and restart backend
docker-compose up --build backend
```

**Frontend Changes:**
```bash
# Edit React files
# Rebuild and restart frontend
docker-compose up --build frontend
```

**Database Changes:**
```bash
# Add new Flyway migration in backend/src/main/resources/db/migration/
# Use format: V<version>__<description>.sql (e.g., V3__add_new_table.sql)
# Restart backend to apply migrations
docker-compose restart backend
```

---

## 🧪 Running Tests

Tests are not run during Docker build for faster startup. To run tests:

```bash
# Backend tests (requires JDK 21 locally)
cd backend
mvn test

# Or run tests in a container
docker-compose run --rm backend mvn test
```

---

## 📊 Health Checks

All services have health checks configured:

```bash
# Check health status
docker-compose ps

# Services show (healthy) when ready
```

- **postgres**: `pg_isready` check
- **backend**: Spring Boot Actuator health endpoint (if configured)
- **frontend**: nginx health endpoint

---

## 🚢 Production Deployment

For production:

1. **Update API URL**: Set `REACT_APP_API_URL` to production backend URL
2. **Use secrets**: Don't use default passwords
3. **Enable HTTPS**: Use reverse proxy (nginx/traefik)
4. **Configure CORS**: Update backend CORS settings
5. **Resource limits**: Add memory/CPU limits in docker-compose.yml

---

## 📝 Project Structure

```
Sticker safari/
├── docker-compose.yml          # Main orchestration file
├── backend/
│   ├── Dockerfile              # Backend build instructions
│   ├── .dockerignore           # Files to exclude
│   ├── pom.xml                 # Maven dependencies
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/           # Java source code
│   │   │   └── resources/
│   │   │       └── db/
│   │   │           └── migration/  # Flyway SQL migrations
│   │   └── test/               # Unit tests
└── front/
    └── sticker-safari-frontend/
        ├── Dockerfile          # Frontend build instructions
        ├── nginx.conf          # nginx configuration
        ├── .dockerignore       # Files to exclude
        ├── package.json        # Node dependencies
        └── src/                # React source code
```

---

## 🤝 Contributing

1. Make changes locally
2. Test with `docker-compose up --build`
3. Commit changes
4. Push to repository

---

## 📄 License

[Your License Here]

---

## 🎉 That's It!

Now everyone can run the entire Sticker Safari application with just one command:

```bash
docker-compose up --build
```

No need to install Java, Maven, Node.js, PostgreSQL, or any other dependencies! 🚀
