// Login Form Submission
if (document.getElementById('loginForm')) {
    document.getElementById('loginForm').addEventListener('submit', async function (e) {
        e.preventDefault(); // Prevent form from submitting normally

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        const userData = { email, password };

        const messageElement = document.getElementById('loginMessage');

        try {
            // Attempt to get login response
            const response = await loginUser(userData); // Ensure response is properly returned

            console.log("Login Response:", response); // Log the response to verify the structure

            // Check if login was successful and handle response accordingly
            if (response && response.token) {
                // Store JWT token in localStorage
                localStorage.setItem('token', response.token);

                const decodedToken = decodeJWT(response.token);
                console.log("Decoded Token:", decodedToken);

                // Extract and store role
                const role = decodedToken.role.replace('ROLE_', ''); // Remove 'ROLE_' prefix if present
                localStorage.setItem('userRole', role);

                console.log("User Role:", role);

                // Redirect based on role
                window.location.href = 'index.html';

            } else {
                // Handle invalid credentials or failed login
                messageElement.innerHTML = 'Invalid credentials, please try again.';
                messageElement.style.color = 'red';
            }
        } catch (error) {
            // Catch any errors during the login process
            console.error('Login error:', error);
            messageElement.innerHTML = 'An error occurred. Please try again later.';
            messageElement.style.color = 'red';
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
        console.error('Login error:', error);
        throw new Error('Login failed. Please try again later.');
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

        if (password !== confirmPassword) {
            messageElement.innerHTML = 'Passwords do not match!';
            messageElement.style.color = 'red';
            return;
        }

        const userData = { name, username, email, password, role };

        try {
            const response = await registerUser(userData);
            console.log("Register Response:", response);  // Log the full response to check its structure

            // Check if the response contains the user data, indicating success
            if (response && response.id) {  // Response contains user data
                messageElement.innerHTML = 'Registration successful! Redirecting to login...';
                messageElement.style.color = 'green';
                setTimeout(() => {
                    window.location.href = 'login.html';  // Redirect to login page
                }, 2000);
            } else {
                // Handle the case where registration failed (unexpected response)
                messageElement.innerHTML = 'Registration failed, please try again.';
                messageElement.style.color = 'red';
            }
        } catch (error) {
            console.error('Registration error:', error);
            messageElement.innerHTML = error.message || 'An error occurred. Please try again later.';
            messageElement.style.color = 'red';
        }
    });
}

// Function to handle the register API call
async function registerUser(userData) {
    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const errorText = await response.text();  // Capture the raw error message from response
            throw new Error(`Registration failed: ${errorText}`);
        }

        const data = await response.json();
        return data; // Return the user data as JSON
    } catch (error) {
        console.error('Registration error:', error);
        throw new Error('Network error. Please try again later.');
    }
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
