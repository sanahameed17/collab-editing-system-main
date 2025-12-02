# Collaborative Editor Frontend

A modern, Google Docs-like web interface for the Collaborative Editing System.

## Features

- **Clean, Modern UI** - Google Docs-inspired design with smooth animations
- **Real-time Collaboration** - Server-Sent Events (SSE) for live document updates
- **User Authentication** - Sign in and registration
- **Document Management** - Create, edit, and manage documents
- **Version History** - View and revert to previous document versions
- **User Management** - View all users in the system
- **Contributor Tracking** - See who contributed to each document
- **Rich Text Editing** - Bold, italic, underline, font selection
- **Word Count** - Real-time word count display
- **Responsive Design** - Works on desktop and mobile devices

## Getting Started

### Prerequisites

1. All backend services must be running:
   - User Service (port 8081)
   - Document Service (port 8082)
   - Version Service (port 8083)
   - API Gateway (port 8080)

2. A web server to serve the frontend files (or use a simple HTTP server)

### Running the Frontend

#### Option 1: Using Python (if installed)
```bash
cd frontend
python -m http.server 3000
```

#### Option 2: Using Node.js (if installed)
```bash
cd frontend
npx http-server -p 3000
```

#### Option 3: Using PHP (if installed)
```bash
cd frontend
php -S localhost:3000
```

#### Option 4: Using VS Code Live Server
1. Install "Live Server" extension in VS Code
2. Right-click on `index.html`
3. Select "Open with Live Server"

### Access the Application

Open your browser and navigate to:
- `http://localhost:3000` (or the port you chose)

## Usage

### 1. Sign Up / Sign In
- Click "Sign Up" to create a new account
- Or use "Sign In" if you already have an account
- After authentication, you'll be taken to the editor

### 2. Create and Edit Documents
- The editor opens automatically after login
- Start typing to create content
- Use the toolbar to format text (bold, italic, underline)
- Change font family and size from the toolbar
- Document title can be edited in the top bar
- Changes are saved automatically

### 3. Real-time Collaboration
- The connection status shows if you're connected
- Changes are broadcasted to all connected users
- Multiple users can edit the same document simultaneously

### 4. Version History
- Click the clock icon in the toolbar to view version history
- Select a document from the dropdown to see its versions
- Click on a version to revert to it

### 5. View Contributors
- Click the users icon in the toolbar
- See who contributed to the current document
- View contribution counts per user

### 6. User Management
- Click the menu button (☰) to open the sidebar
- Select "Users" to view all registered users
- Click "Refresh" to reload the user list

## API Integration

The frontend connects to the backend via the API Gateway at `http://localhost:8080/api`.

### Endpoints Used:
- `POST /api/users/register` - User registration
- `POST /api/users/login` - User authentication
- `GET /api/users` - Get all users
- `POST /api/documents` - Create document
- `GET /api/documents/owner/{id}` - Get user's documents
- `PUT /api/documents/{id}` - Update document
- `GET /api/documents/{id}/subscribe` - Subscribe to real-time updates
- `GET /api/versions/document/{id}` - Get document versions
- `POST /api/versions/revert/{docId}/{versionId}` - Revert to version
- `GET /api/versions/document/{id}/contributions` - Get contributions

## File Structure

```
frontend/
├── index.html      # Main HTML structure
├── styles.css      # All styling and CSS
├── app.js          # Application logic and API integration
└── README.md       # This file
```

## Browser Compatibility

- Chrome/Edge (recommended)
- Firefox
- Safari
- Opera

## Troubleshooting

### CORS Errors
If you see CORS errors, ensure:
1. API Gateway CORS is configured correctly
2. All services are running
3. You're accessing the frontend via HTTP (not file://)

### Connection Issues
- Check that all backend services are running
- Verify API Gateway is accessible at `http://localhost:8080`
- Check browser console for error messages

### Real-time Updates Not Working
- Ensure Server-Sent Events are supported by your browser
- Check that the document service is running
- Verify the subscription endpoint is accessible

## Customization

### Change API URL
Edit `app.js` and modify:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

### Change Colors
Edit `styles.css` and modify the CSS variables in `:root`:
```css
:root {
    --primary-color: #4285F4;
    --secondary-color: #34A853;
    /* ... */
}
```

## Future Enhancements

- [ ] Document sharing with permissions
- [ ] Comments and suggestions
- [ ] Export to PDF/Word
- [ ] Offline support
- [ ] Dark mode
- [ ] Keyboard shortcuts
- [ ] Collaborative cursors
- [ ] Chat functionality

