#!/bin/bash
echo "============================================"
echo "  Starting Sticker Safari Application"
echo "============================================"
echo ""
echo "This will start:"
echo "  - PostgreSQL Database (port 5432)"
echo "  - Spring Boot Backend (port 8080)"
echo "  - React Frontend (port 3000)"
echo ""
echo "Please wait, this may take a few minutes on first run..."
echo ""

docker-compose up --build
