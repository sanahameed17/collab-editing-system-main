// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';
const FALLBACK_URLS = {
    users: 'http://localhost:8081',
    documents: 'http://localhost:8082',
    versions: 'http://localhost:8083'
};

// Connection status
let connectionStatus = {
    gateway: false,
    userService: false,
    documentService: false,
    versionService: false
};

// Application State
let currentUser = null;
let currentDocument = null;
let eventSource = null;
let documents = [];
let users = [];
let versions = [];

// Initialize Application
document.addEventListener('DOMContentLoaded', () => {
    checkConnectionStatus();
    checkAuth();
    setupEventListeners();
    loadDocuments();
    setInterval(checkConnectionStatus, 5000); // Check every 5 seconds
});

// Authentication
function checkAuth() {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        showEditor();
        updateUserUI();
    } else {
        showAuthModal();
    }
}

function showAuthModal() {
    const modal = document.getElementById('authModal');
    if (modal) {
        modal.style.display = 'flex';
        // Reset to login tab
        switchAuthTab('login');
        // Clear all form fields
        document.getElementById('loginForm')?.reset();
        document.getElementById('registerForm')?.reset();
        clearErrors();
    }
}

function hideAuthModal() {
    const modal = document.getElementById('authModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function setupEventListeners() {
    // Auth tabs
    document.querySelectorAll('.auth-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            const tabName = e.target.closest('.auth-tab').dataset.tab;
            switchAuthTab(tabName);
        });
    });

    // Auth forms
    document.getElementById('loginForm').addEventListener('submit', (e) => {
        e.preventDefault();
        handleLogin();
    });
    
    document.getElementById('registerForm').addEventListener('submit', (e) => {
        e.preventDefault();
        handleRegister();
    });

    // Password toggles
    setupPasswordToggle('loginPassword', 'loginPasswordToggle');
    setupPasswordToggle('registerPassword', 'registerPasswordToggle');

    // Real-time validation
    setupFormValidation();

    // Menu
    document.getElementById('menuBtn').addEventListener('click', toggleSidebar);
    document.getElementById('closeSidebarBtn').addEventListener('click', toggleSidebar);

    // Navigation
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            const view = e.currentTarget.dataset.view;
            switchView(view);
            document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
            e.currentTarget.classList.add('active');
        });
    });

    // Toolbar
    document.getElementById('boldBtn').addEventListener('click', () => document.execCommand('bold', false, null));
    document.getElementById('italicBtn').addEventListener('click', () => document.execCommand('italic', false, null));
    document.getElementById('underlineBtn').addEventListener('click', () => document.execCommand('underline', false, null));
    document.getElementById('undoBtn').addEventListener('click', () => document.execCommand('undo', false, null));
    document.getElementById('redoBtn').addEventListener('click', () => document.execCommand('redo', false, null));

    // Font controls
    document.getElementById('fontFamily').addEventListener('change', (e) => {
        document.execCommand('fontName', false, e.target.value);
    });
    document.getElementById('fontSize').addEventListener('change', (e) => {
        document.execCommand('fontSize', false, e.target.value);
    });

    // Editor
    const editor = document.getElementById('editor');
    editor.addEventListener('input', debounce(handleEditorChange, 500));
    editor.addEventListener('paste', handlePaste);

    // Document title
    document.getElementById('documentTitle').addEventListener('blur', handleTitleChange);

    // Version history
    document.getElementById('historyBtn').addEventListener('click', showVersionHistory);
    document.getElementById('contributorsBtn').addEventListener('click', showContributors);
    document.getElementById('documentSelect').addEventListener('change', loadVersions);

    // User menu
    document.getElementById('userAvatarBtn').addEventListener('click', (e) => {
        e.stopPropagation();
    });
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);

    // Modals
    document.getElementById('closeVersionModal').addEventListener('click', () => {
        document.getElementById('versionModal').style.display = 'none';
    });
    document.getElementById('closeContributorsModal').addEventListener('click', () => {
        document.getElementById('contributorsModal').style.display = 'none';
    });

    // Refresh users
    document.getElementById('refreshUsersBtn').addEventListener('click', loadUsers);

    // Admin panel
    document.getElementById('refreshAdminBtn')?.addEventListener('click', loadAdminData);
}

// Auth Tab Switching
function switchAuthTab(tabName) {
    document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => {
        f.classList.remove('active');
        f.style.display = 'none';
    });
    
    const activeTab = document.querySelector(`[data-tab="${tabName}"]`);
    const activeForm = document.getElementById(`${tabName}Form`);
    
    if (activeTab) activeTab.classList.add('active');
    if (activeForm) {
        activeForm.style.display = 'flex';
        activeForm.classList.add('active');
    }
    
    // Update header text
    const title = document.getElementById('authTitle');
    const subtitle = document.getElementById('authSubtitle');
    
    if (tabName === 'login') {
        title.textContent = 'Welcome Back';
        subtitle.textContent = 'Sign in to continue to your workspace';
    } else {
        title.textContent = 'Create Account';
        subtitle.textContent = 'Join us and start collaborating today';
    }
    
    // Clear errors
    clearErrors();
}

// Password Toggle
function setupPasswordToggle(inputId, toggleId) {
    const input = document.getElementById(inputId);
    const toggle = document.getElementById(toggleId);
    
    if (!input || !toggle) return;
    
    toggle.addEventListener('click', () => {
        const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
        input.setAttribute('type', type);
        
        const eyeIcon = toggle.querySelector('.eye-icon');
        const eyeOffIcon = toggle.querySelector('.eye-off-icon');
        
        if (type === 'password') {
            eyeIcon.style.display = 'block';
            eyeOffIcon.style.display = 'none';
        } else {
            eyeIcon.style.display = 'none';
            eyeOffIcon.style.display = 'block';
        }
    });
}

// Form Validation
function setupFormValidation() {
    // Login form validation
    const loginEmail = document.getElementById('loginEmail');
    const loginPassword = document.getElementById('loginPassword');
    
    loginEmail?.addEventListener('blur', () => validateEmail(loginEmail, 'loginEmailError'));
    loginPassword?.addEventListener('blur', () => validateRequired(loginPassword, 'loginPasswordError', 'Password is required'));
    
    // Register form validation
    const registerUsername = document.getElementById('registerUsername');
    const registerEmail = document.getElementById('registerEmail');
    const registerPassword = document.getElementById('registerPassword');
    
    registerUsername?.addEventListener('blur', () => validateUsername(registerUsername, 'registerUsernameError'));
    registerEmail?.addEventListener('blur', () => validateEmail(registerEmail, 'registerEmailError'));
    registerPassword?.addEventListener('input', () => {
        validatePassword(registerPassword, 'registerPasswordError');
        checkPasswordStrength(registerPassword.value);
    });
}

function validateEmail(input, errorId) {
    const error = document.getElementById(errorId);
    const email = input.value.trim();
    
    if (!email) {
        showFieldError(error, 'Email is required');
        return false;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showFieldError(error, 'Please enter a valid email address');
        return false;
    }
    
    hideFieldError(error);
    return true;
}

function validateUsername(input, errorId) {
    const error = document.getElementById(errorId);
    const username = input.value.trim();
    
    if (!username) {
        showFieldError(error, 'Username is required');
        return false;
    }
    
    if (username.length < 3) {
        showFieldError(error, 'Username must be at least 3 characters');
        return false;
    }
    
    hideFieldError(error);
    return true;
}

function validatePassword(input, errorId) {
    const error = document.getElementById(errorId);
    const password = input.value;
    
    if (!password) {
        showFieldError(error, 'Password is required');
        return false;
    }
    
    if (password.length < 6) {
        showFieldError(error, 'Password must be at least 6 characters');
        return false;
    }
    
    hideFieldError(error);
    return true;
}

function validateRequired(input, errorId, message) {
    const error = document.getElementById(errorId);
    
    if (!input.value.trim()) {
        showFieldError(error, message);
        return false;
    }
    
    hideFieldError(error);
    return true;
}

function showFieldError(errorElement, message) {
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.add('show');
    }
}

function hideFieldError(errorElement) {
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.classList.remove('show');
    }
}

function clearErrors() {
    document.querySelectorAll('.input-error, .error-message').forEach(el => {
        el.textContent = '';
        el.classList.remove('show');
    });
}

// Password Strength Checker
function checkPasswordStrength(password) {
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');
    
    if (!strengthFill || !strengthText) return;
    
    let strength = 0;
    let text = 'Password strength';
    
    if (password.length >= 6) strength++;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[^a-zA-Z\d]/.test(password)) strength++;
    
    strengthFill.className = 'strength-fill';
    
    if (strength <= 2) {
        strengthFill.classList.add('weak');
        text = 'Weak password';
    } else if (strength <= 3) {
        strengthFill.classList.add('medium');
        text = 'Medium strength';
    } else {
        strengthFill.classList.add('strong');
        text = 'Strong password';
    }
    
    strengthText.textContent = text;
}

// Authentication Functions
async function handleLogin() {
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');
    const loginBtn = document.getElementById('loginBtn');
    
    // Clear previous errors
    clearErrors();
    
    // Validate
    if (!validateEmail(document.getElementById('loginEmail'), 'loginEmailError')) return;
    if (!validateRequired(document.getElementById('loginPassword'), 'loginPasswordError', 'Password is required')) return;
    
    // Show loading state
    setButtonLoading(loginBtn, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok) {
            currentUser = data;
            localStorage.setItem('currentUser', JSON.stringify(data));
            
            // Show success animation
            loginBtn.style.background = 'linear-gradient(135deg, #34A853 0%, #2E7D32 100%)';
            loginBtn.querySelector('.btn-text').textContent = 'Success!';
            
            setTimeout(() => {
                hideAuthModal();
                showEditor();
                updateUserUI();
                createNewDocument();
            }, 500);
        } else {
            showError(errorDiv, data.message || 'Invalid email or password');
        }
    } catch (error) {
        console.error('Login error:', error);
        // Try fallback to direct service
        try {
            const fallbackResponse = await fetch(`${FALLBACK_URLS.users}/users/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            const fallbackData = await fallbackResponse.json();
            if (fallbackResponse.ok) {
                currentUser = fallbackData;
                localStorage.setItem('currentUser', JSON.stringify(fallbackData));
                loginBtn.style.background = 'linear-gradient(135deg, #34A853 0%, #2E7D32 100%)';
                loginBtn.querySelector('.btn-text').textContent = 'Success!';
                setTimeout(() => {
                    hideAuthModal();
                    showEditor();
                    updateUserUI();
                    createNewDocument();
                }, 500);
            } else {
                showError(errorDiv, fallbackData.message || 'Invalid email or password');
            }
        } catch (fallbackError) {
            showError(errorDiv, `Connection error. Gateway: ${error.message}. Direct service also failed. Please ensure services are running on ports 8080, 8081, 8082, 8083.`);
        }
    } finally {
        setButtonLoading(loginBtn, false);
    }
}

async function handleRegister() {
    const username = document.getElementById('registerUsername').value.trim();
    const email = document.getElementById('registerEmail').value.trim();
    const password = document.getElementById('registerPassword').value;
    const agreeTerms = document.getElementById('agreeTerms').checked;
    const errorDiv = document.getElementById('registerError');
    const registerBtn = document.getElementById('registerBtn');
    
    // Clear previous errors
    clearErrors();
    
    // Validate
    if (!validateUsername(document.getElementById('registerUsername'), 'registerUsernameError')) return;
    if (!validateEmail(document.getElementById('registerEmail'), 'registerEmailError')) return;
    if (!validatePassword(document.getElementById('registerPassword'), 'registerPasswordError')) return;
    
    if (!agreeTerms) {
        showError(errorDiv, 'Please agree to the Terms of Service and Privacy Policy');
        return;
    }
    
    // Show loading state
    setButtonLoading(registerBtn, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            // Show success
            registerBtn.style.background = 'linear-gradient(135deg, #34A853 0%, #2E7D32 100%)';
            registerBtn.querySelector('.btn-text').textContent = 'Account Created!';
            
            // Auto login after registration
            setTimeout(() => {
                document.getElementById('loginEmail').value = email;
                document.getElementById('loginPassword').value = password;
                switchAuthTab('login');
                setTimeout(() => handleLogin(), 300);
            }, 1000);
        } else {
            showError(errorDiv, data.message || 'Registration failed. Email may already be in use.');
        }
    } catch (error) {
        console.error('Register error:', error);
        // Try fallback to direct service
        try {
            const fallbackResponse = await fetch(`${FALLBACK_URLS.users}/users/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });
            const fallbackData = await fallbackResponse.json();
            if (fallbackResponse.ok) {
                registerBtn.style.background = 'linear-gradient(135deg, #34A853 0%, #2E7D32 100%)';
                registerBtn.querySelector('.btn-text').textContent = 'Account Created!';
                setTimeout(() => {
                    document.getElementById('loginEmail').value = email;
                    document.getElementById('loginPassword').value = password;
                    switchAuthTab('login');
                    setTimeout(() => handleLogin(), 300);
                }, 1000);
            } else {
                showError(errorDiv, fallbackData.message || 'Registration failed. Email may already be in use.');
            }
        } catch (fallbackError) {
            showError(errorDiv, `Connection error. Gateway: ${error.message}. Direct service also failed. Please ensure User Service is running on port 8081.`);
        }
    } finally {
        setButtonLoading(registerBtn, false);
    }
}

function setButtonLoading(button, loading) {
    if (!button) return;
    
    const btnText = button.querySelector('.btn-text');
    const btnLoader = button.querySelector('.btn-loader');
    
    if (loading) {
        button.disabled = true;
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline-block';
    } else {
        button.disabled = false;
        btnText.style.display = 'inline-block';
        btnLoader.style.display = 'none';
    }
}

function showError(errorDiv, message) {
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.classList.add('show');
    }
}

// Connection Status Checking
async function checkConnectionStatus() {
    const services = [
        { name: 'gateway', url: 'http://localhost:8080/api/users', key: 'gateway' },
        { name: 'userService', url: 'http://localhost:8081/users', key: 'userService' },
        { name: 'documentService', url: 'http://localhost:8082/documents', key: 'documentService' },
        { name: 'versionService', url: 'http://localhost:8083/versions', key: 'versionService' }
    ];

    for (const service of services) {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 2000);
            
            const response = await fetch(service.url, { 
                method: 'GET',
                signal: controller.signal
            });
            
            clearTimeout(timeoutId);
            connectionStatus[service.key] = response.status < 500;
        } catch (error) {
            connectionStatus[service.key] = false;
        }
    }
    
    updateConnectionIndicator();
}

function updateConnectionIndicator() {
    const indicator = document.getElementById('connectionStatus');
    if (!indicator) return;
    
    const allConnected = Object.values(connectionStatus).every(status => status);
    const gatewayConnected = connectionStatus.gateway;
    
    if (allConnected) {
        indicator.textContent = '● All Services Connected';
        indicator.className = 'status-connected';
    } else if (gatewayConnected) {
        indicator.textContent = '● Gateway Connected';
        indicator.className = 'status-connected';
    } else {
        indicator.textContent = '● Services Offline';
        indicator.className = 'status-disconnected';
    }
    
    // Update admin panel status
    updateAdminStatus();
}

function updateAdminStatus() {
    const statusMap = {
        gateway: document.getElementById('gatewayStatus'),
        userService: document.getElementById('userServiceStatus'),
        documentService: document.getElementById('documentServiceStatus'),
        versionService: document.getElementById('versionServiceStatus')
    };
    
    Object.keys(statusMap).forEach(key => {
        const element = statusMap[key];
        if (element) {
            if (connectionStatus[key]) {
                element.textContent = 'Online';
                element.className = 'status-badge online';
            } else {
                element.textContent = 'Offline';
                element.className = 'status-badge offline';
            }
        }
    });
}

// Admin Panel Functions
async function loadAdminData() {
    await Promise.all([
        loadAdminUsers(),
        loadAdminDocuments(),
        loadAdminVersions(),
        updateAdminStats()
    ]);
}

async function loadAdminUsers() {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '<tr><td colspan="4" class="loading-cell">Loading...</td></tr>';
    
    try {
        let response = await fetch(`${API_BASE_URL}/users`);
        if (!response.ok) {
            response = await fetch(`${FALLBACK_URLS.users}/users`);
        }
        
        if (response.ok) {
            const users = await response.json();
            tbody.innerHTML = '';
            
            if (users.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="empty-cell">No users found</td></tr>';
            } else {
                users.forEach(user => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${user.id || 'N/A'}</td>
                        <td>${user.username || 'N/A'}</td>
                        <td>${user.email || 'N/A'}</td>
                        <td>
                            <button class="btn-action" onclick="viewUser(${user.id})">View</button>
                            <button class="btn-action delete" onclick="deleteUserAdmin(${user.id})">Delete</button>
                        </td>
                    `;
                    tbody.appendChild(row);
                });
            }
        }
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="4" class="error-cell">Error loading users</td></tr>';
    }
}

async function loadAdminDocuments() {
    const tbody = document.getElementById('documentsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '<tr><td colspan="5" class="loading-cell">Loading...</td></tr>';
    
    try {
        let response = await fetch(`${API_BASE_URL}/documents`);
        if (!response.ok) {
            response = await fetch(`${FALLBACK_URLS.documents}/documents`);
        }
        
        if (response.ok) {
            const documents = await response.json();
            tbody.innerHTML = '';
            
            if (documents.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="empty-cell">No documents found</td></tr>';
            } else {
                documents.forEach(doc => {
                    const row = document.createElement('tr');
                    const created = doc.createdAt ? new Date(doc.createdAt).toLocaleString() : 'N/A';
                    row.innerHTML = `
                        <td>${doc.id || 'N/A'}</td>
                        <td>${(doc.title || 'Untitled').substring(0, 30)}${(doc.title || '').length > 30 ? '...' : ''}</td>
                        <td>${doc.ownerId || 'N/A'}</td>
                        <td>${created}</td>
                        <td>
                            <button class="btn-action" onclick="viewDocument(${doc.id})">View</button>
                        </td>
                    `;
                    tbody.appendChild(row);
                });
            }
        }
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="5" class="error-cell">Error loading documents</td></tr>';
    }
}

async function loadAdminVersions() {
    const tbody = document.getElementById('versionsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '<tr><td colspan="5" class="loading-cell">Loading...</td></tr>';
    
    try {
        // Get all documents first, then get versions for each
        let docsResponse = await fetch(`${API_BASE_URL}/documents`);
        if (!docsResponse.ok) {
            docsResponse = await fetch(`${FALLBACK_URLS.documents}/documents`);
        }
        
        if (docsResponse.ok) {
            const documents = await docsResponse.json();
            const allVersions = [];
            
            for (const doc of documents) {
                try {
                    let versionsResponse = await fetch(`${API_BASE_URL}/versions/document/${doc.id}`);
                    if (!versionsResponse.ok) {
                        versionsResponse = await fetch(`${FALLBACK_URLS.versions}/versions/document/${doc.id}`);
                    }
                    if (versionsResponse.ok) {
                        const versions = await versionsResponse.json();
                        allVersions.push(...versions);
                    }
                } catch (e) {
                    console.error(`Error loading versions for doc ${doc.id}:`, e);
                }
            }
            
            tbody.innerHTML = '';
            
            if (allVersions.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="empty-cell">No versions found</td></tr>';
            } else {
                allVersions.forEach(version => {
                    const row = document.createElement('tr');
                    const timestamp = version.timestamp ? new Date(version.timestamp).toLocaleString() : 'N/A';
                    row.innerHTML = `
                        <td>${version.id || 'N/A'}</td>
                        <td>${version.documentId || 'N/A'}</td>
                        <td>${version.editedByUserId || 'N/A'}</td>
                        <td>${version.versionNumber || 'N/A'}</td>
                        <td>${timestamp}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
        }
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="5" class="error-cell">Error loading versions</td></tr>';
    }
}

async function updateAdminStats() {
    try {
        // Users count
        let usersResponse = await fetch(`${API_BASE_URL}/users`);
        if (!usersResponse.ok) {
            usersResponse = await fetch(`${FALLBACK_URLS.users}/users`);
        }
        if (usersResponse.ok) {
            const users = await usersResponse.json();
            document.getElementById('totalUsers').textContent = users.length || 0;
        }
        
        // Documents count
        let docsResponse = await fetch(`${API_BASE_URL}/documents`);
        if (!docsResponse.ok) {
            docsResponse = await fetch(`${FALLBACK_URLS.documents}/documents`);
        }
        if (docsResponse.ok) {
            const docs = await docsResponse.json();
            document.getElementById('totalDocuments').textContent = docs.length || 0;
        }
        
        // Versions count (approximate)
        let totalVersions = 0;
        try {
            let docsResponse = await fetch(`${API_BASE_URL}/documents`);
            if (!docsResponse.ok) {
                docsResponse = await fetch(`${FALLBACK_URLS.documents}/documents`);
            }
            if (docsResponse.ok) {
                const documents = await docsResponse.json();
                for (const doc of documents) {
                    try {
                        let versionsResponse = await fetch(`${API_BASE_URL}/versions/document/${doc.id}`);
                        if (!versionsResponse.ok) {
                            versionsResponse = await fetch(`${FALLBACK_URLS.versions}/versions/document/${doc.id}`);
                        }
                        if (versionsResponse.ok) {
                            const versions = await versionsResponse.json();
                            totalVersions += versions.length;
                        }
                    } catch (e) {}
                }
            }
        } catch (e) {}
        document.getElementById('totalVersions').textContent = totalVersions;
    } catch (error) {
        console.error('Error updating stats:', error);
    }
}

// Global functions for admin panel
window.refreshUsersTable = loadAdminUsers;
window.refreshDocumentsTable = loadAdminDocuments;
window.refreshVersionsTable = loadAdminVersions;

window.viewUser = (id) => {
    alert(`View user ${id} - Feature coming soon!`);
};

window.viewDocument = (id) => {
    alert(`View document ${id} - Feature coming soon!`);
};

window.deleteUserAdmin = async (id) => {
    if (!confirm(`Are you sure you want to delete user ${id}?`)) return;
    
    try {
        let response = await fetch(`${API_BASE_URL}/users/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            response = await fetch(`${FALLBACK_URLS.users}/users/${id}`, { method: 'DELETE' });
        }
        if (response.ok) {
            alert('User deleted successfully');
            loadAdminUsers();
            updateAdminStats();
        } else {
            alert('Failed to delete user');
        }
    } catch (error) {
        alert('Error deleting user: ' + error.message);
    }
};

function handleLogout() {
    currentUser = null;
    currentDocument = null;
    localStorage.removeItem('currentUser');
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }
    showAuthModal();
    document.getElementById('editorView').style.display = 'none';
}

// Document Functions
async function createNewDocument() {
    if (!currentUser) return;

    const title = 'Untitled Document';
    const content = document.getElementById('editor').innerHTML;

    try {
        const response = await fetch(`${API_BASE_URL}/documents`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title,
                content,
                ownerId: currentUser.userId
            })
        });

        const data = await response.json();
        if (response.ok) {
            currentDocument = data.document;
            document.getElementById('documentTitle').value = currentDocument.title;
            subscribeToDocument(currentDocument.id);
        }
    } catch (error) {
        console.error('Error creating document:', error);
    }
}

async function loadDocuments() {
    if (!currentUser) return;

    try {
        const response = await fetch(`${API_BASE_URL}/documents/owner/${currentUser.userId}`);
        if (response.ok) {
            documents = await response.json();
            updateDocumentSelect();
        }
    } catch (error) {
        console.error('Error loading documents:', error);
    }
}

function updateDocumentSelect() {
    const select = document.getElementById('documentSelect');
    select.innerHTML = '<option value="">Select Document</option>';
    documents.forEach(doc => {
        const option = document.createElement('option');
        option.value = doc.id;
        option.textContent = doc.title;
        select.appendChild(option);
    });
}

async function handleTitleChange() {
    if (!currentDocument) return;

    const newTitle = document.getElementById('documentTitle').value;
    
    try {
        const response = await fetch(`${API_BASE_URL}/documents/${currentDocument.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: newTitle,
                content: document.getElementById('editor').innerHTML
            })
        });

        if (response.ok) {
            const data = await response.json();
            currentDocument = data.document;
        }
    } catch (error) {
        console.error('Error updating document:', error);
    }
}

async function handleEditorChange() {
    if (!currentDocument) return;

    const content = document.getElementById('editor').innerHTML;
    updateWordCount();

    try {
        await fetch(`${API_BASE_URL}/documents/${currentDocument.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                content,
                title: currentDocument.title
            })
        });
    } catch (error) {
        console.error('Error updating document:', error);
    }
}

function handlePaste(e) {
    e.preventDefault();
    const text = (e.clipboardData || window.clipboardData).getData('text');
    document.execCommand('insertText', false, text);
}

// Real-time Collaboration
function subscribeToDocument(documentId) {
    if (eventSource) {
        eventSource.close();
    }

    eventSource = new EventSource(`${API_BASE_URL}/documents/${documentId}/subscribe`);
    
    eventSource.addEventListener('document-update', (event) => {
        const doc = JSON.parse(event.data);
        if (doc.id === currentDocument.id) {
            // Only update if content changed (avoid infinite loop)
            const editorContent = document.getElementById('editor').innerHTML;
            if (editorContent !== doc.content) {
                document.getElementById('editor').innerHTML = doc.content;
                updateWordCount();
            }
        }
    });

    eventSource.onerror = () => {
        document.getElementById('connectionStatus').textContent = '● Disconnected';
        document.getElementById('connectionStatus').classList.remove('status-connected');
        document.getElementById('connectionStatus').classList.add('status-disconnected');
    };

    document.getElementById('connectionStatus').textContent = '● Connected';
    document.getElementById('connectionStatus').classList.add('status-connected');
    document.getElementById('connectionStatus').classList.remove('status-disconnected');
}

// Version History
async function loadVersions() {
    const documentId = document.getElementById('documentSelect').value;
    if (!documentId) return;

    try {
        const response = await fetch(`${API_BASE_URL}/versions/document/${documentId}`);
        if (response.ok) {
            versions = await response.json();
            displayVersions();
        }
    } catch (error) {
        console.error('Error loading versions:', error);
    }
}

function displayVersions() {
    const container = document.getElementById('versionsList');
    container.innerHTML = '';

    versions.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    versions.forEach(version => {
        const item = document.createElement('div');
        item.className = 'version-item';
        item.innerHTML = `
            <div class="version-header">
                <span class="version-number">Version ${version.versionNumber || 'N/A'}</span>
                <span class="version-date">${formatDate(version.timestamp)}</span>
            </div>
            <div class="version-content">${version.content ? version.content.substring(0, 100) + '...' : 'No content'}</div>
        `;
        item.addEventListener('click', () => revertToVersion(version));
        container.appendChild(item);
    });
}

async function revertToVersion(version) {
    if (!confirm('Are you sure you want to revert to this version?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/versions/revert/${version.documentId}/${version.id}`, {
            method: 'POST'
        });

        if (response.ok) {
            alert('Document reverted successfully!');
            loadVersions();
        }
    } catch (error) {
        console.error('Error reverting version:', error);
    }
}

async function showVersionHistory() {
    if (!currentDocument) {
        alert('Please create or open a document first');
        return;
    }

    document.getElementById('documentSelect').value = currentDocument.id;
    await loadVersions();
    document.getElementById('versionModal').style.display = 'flex';
}

async function showContributors() {
    if (!currentDocument) {
        alert('Please create or open a document first');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/versions/document/${currentDocument.id}/contributions`);
        if (response.ok) {
            const data = await response.json();
            displayContributors(data);
            document.getElementById('contributorsModal').style.display = 'flex';
        }
    } catch (error) {
        console.error('Error loading contributors:', error);
    }
}

function displayContributors(data) {
    const container = document.getElementById('contributorsModalBody');
    container.innerHTML = `
        <div class="contributions-panel">
            <h3>Total Versions: ${data.totalVersions}</h3>
            <div class="contributions-list">
                ${Object.entries(data.userContributions).map(([userId, count]) => `
                    <div class="contribution-item">
                        <span class="contribution-user">User ID: ${userId}</span>
                        <span class="contribution-count">${count} contributions</span>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

// User Management
async function loadUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/users`);
        if (response.ok) {
            users = await response.json();
            displayUsers();
        }
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

function displayUsers() {
    const container = document.getElementById('usersList');
    container.innerHTML = '';

    users.forEach(user => {
        const card = document.createElement('div');
        card.className = 'user-card';
        card.innerHTML = `
            <div class="user-card-header">
                <div class="user-avatar" style="width: 40px; height: 40px;">
                    ${user.username ? user.username.charAt(0).toUpperCase() : 'U'}
                </div>
                <div>
                    <div class="user-card-name">${user.username || 'Unknown'}</div>
                    <div class="user-card-email">${user.email || 'No email'}</div>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

// UI Functions
function showEditor() {
    document.getElementById('authModal').style.display = 'none';
    document.getElementById('editorView').style.display = 'block';
}

function switchView(viewName) {
    document.querySelectorAll('.view').forEach(v => v.style.display = 'none');
    
    switch(viewName) {
        case 'editor':
            document.getElementById('editorView').style.display = 'block';
            break;
        case 'users':
            document.getElementById('usersView').style.display = 'block';
            loadUsers();
            break;
        case 'versions':
            document.getElementById('versionsView').style.display = 'block';
            loadDocuments();
            break;
        case 'admin':
            document.getElementById('adminView').style.display = 'block';
            loadAdminData();
            break;
    }
}

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
}

function updateUserUI() {
    if (currentUser) {
        const initials = currentUser.username ? currentUser.username.charAt(0).toUpperCase() : 'U';
        document.getElementById('userInitials').textContent = initials;
        document.getElementById('userName').textContent = currentUser.username || 'User';
        document.getElementById('userEmail').textContent = currentUser.email || '';
    }
}

function updateWordCount() {
    const text = document.getElementById('editor').innerText;
    const words = text.trim().split(/\s+/).filter(word => word.length > 0);
    document.getElementById('wordCount').textContent = `${words.length} words`;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString();
}

// Utility Functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

