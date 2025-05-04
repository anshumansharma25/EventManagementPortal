// Login Form Submission
if (document.getElementById('loginForm')) {
    document.getElementById('loginForm').addEventListener('submit', async function (e) {
        e.preventDefault(); // Prevent form from submitting normally

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        const userData = { email, password };

        const messageElement = document.getElementById('auth-message');

        try {
            // Attempt to get login response
            const response = await loginUser(userData);

            // Check if login was successful and handle response accordingly
            if (response && response.token) {
                // Store JWT token in localStorage
                localStorage.setItem('token', response.token);

                // Decode and extract role using your helper
                const decodedToken = decodeJWT(response.token);
                const role = decodedToken.role.replace('ROLE_', '');
                localStorage.setItem('userRole', role);

                // Redirect to home
                window.location.href = 'index.html';
            }
            else {
                messageElement.textContent = 'Invalid credentials, please try again.';
                messageElement.className = 'auth-message error';

                // Auto-hide after 3 seconds
                setTimeout(() => {
                    messageElement.textContent = '';
                    messageElement.className = 'auth-message'; // Reset to base class
                }, 3000);
            }
        } catch (error) {
            // Catch any errors during the login process
           messageElement.textContent = 'Invalid credentials, please try again.';
           messageElement.className = 'auth-message error';

           // Auto-hide after 3 seconds
           setTimeout(() => {
               messageElement.textContent = '';
               messageElement.className = 'auth-message'; // Reset to base class
           }, 3000);
        }
    });
}

// Function to handle the login API call
async function loginUser(userData) {
    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        const data = await response.json();
        return data; // Return the parsed response
    } catch (error) {
        messageElement.textContent = error.message || 'An error occurred. Please try again later.';
        messageElement.className = 'auth-message error';

        setTimeout(() => {
            messageElement.textContent = '';
            messageElement.className = 'auth-message';
        }, 3000);

    }
}

// Helper function to decode JWT
function decodeJWT(token) {
    const base64Url = token.split('.')[1]; // JWT structure is: header.payload.signature
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = JSON.parse(window.atob(base64));
    return decoded;
}

// Function to get role from the decoded token
function getRoleFromToken(token) {
    const decodedToken = decodeJWT(token);
    const role = decodedToken.role; // Assuming the role is in 'role' field
    return role.replace('ROLE_', ''); // Remove 'ROLE_' prefix
}


// Register Form Submission
if (document.getElementById('registerForm')) {
    document.getElementById('registerForm').addEventListener('submit', async function (e) {
        e.preventDefault(); // Prevent form from submitting normally

        const name = document.getElementById('name').value;
        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const role = document.getElementById('role').value;

        const messageElement = document.getElementById('registerMessage');

        messageElement.textContent = '';
        messageElement.className = 'message';

        if (password !== confirmPassword) {
                messageElement.textContent = 'Passwords do not match!';
                messageElement.className = 'message error';

                setTimeout(() => {
                        messageElement.textContent = '';
                        messageElement.className = 'message';
                    }, 3000);

            return;
        }

        const userData = { name, username, email, password, role };

        try {
            const response = await registerUser(userData);
            // Check if the response contains the user data, indicating success
            if (response && response.id) {  // Response contains user data
                 messageElement.textContent = 'Registration successful! Redirecting to login...';
                 messageElement.className = 'message success';
                 setTimeout(() => {
                    window.location.href = 'login.html';
                 }, 2000);
            }
        } catch (error) {
            // Display the error message from the server
            messageElement.textContent = error.message || 'Registration failed. Please try again.';
            messageElement.className = 'message error';

            setTimeout(() => {
                    messageElement.textContent = '';
                    messageElement.className = 'message';
                }, 3000);
        }
    });
}

// Function to handle the register API call
async function registerUser(userData) {

        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const errorText = await response.text();  // Capture the raw error message from response
            throw new Error(errorText || 'Registration failed');
        }
        const data = await response.json();
        return data; // Return the user data as JSON

    return await response.json()
}

// Helper function to get JWT token from localStorage
function getAuthToken() {
    return localStorage.getItem('token');
}

// Add Authorization Header to API requests
function addAuthHeader(request) {
    const token = getAuthToken();
    if (token) {
        request.headers['Authorization'] = 'Bearer ' + token;
    }
    return request;
}
