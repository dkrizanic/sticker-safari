# Sticker Safari - User Credentials

## User Accounts

The application now has **proper password authentication**. Here are the available accounts:

### Regular Users (Collectors)
- **Username:** Bob | **Password:** collector
- **Username:** Jack | **Password:** collector
- **Username:** Anne | **Password:** collector
- **Username:** Ema | **Password:** collector

### Admin Account
- **Username:** admin | **Password:** admin

## Security Features

✅ **Password field added** to login form  
✅ **Password validation** - Wrong passwords are rejected with error message  
✅ **Secure storage** - Passwords stored in sessionStorage (cleared on logout)  
✅ **Basic Authentication** - Uses Spring Security with BCrypt password hashing  
✅ **Role-based access** - Collectors and Admins have different permissions  

## How to Login

1. Navigate to http://localhost:3000
2. Click "Login" button in the top navigation bar (no scrolling needed!)
3. Enter username (e.g., "Bob")
4. Enter password (e.g., "collector")
5. Click "Login"

## Registration

New users can register with:
- Username (minimum 3 characters)
- Password (minimum 6 characters)
- New accounts are automatically assigned the password "collector" on the backend

## Logout

Click the "Logout" button in the top right corner to clear your session.
