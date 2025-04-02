document.addEventListener("DOMContentLoaded", function () {
    // Check for JWT token before proceeding
    const jwtToken = localStorage.getItem("jwtToken");
    if (!jwtToken || isTokenExpired(jwtToken)) {
        window.location.href = "/login.html"; // Redirect to login if no token or token expired
        return;
    }

    // Decode the JWT token and check if the user has 'ORGANIZER' role
    const userRole = getRoleFromToken(jwtToken);
    if (userRole !== 'ORGANIZER') {
        window.location.href = "/unauthorized.html"; // Redirect to unauthorized page if not an organizer
        return;
    }

    // Continue with the page logic if the user has 'ORGANIZER' role
    loadOrganizerDashboard();

    // Function to load the organizer dashboard content
    function loadOrganizerDashboard() {
        // Event creation logic
        const createEventForm = document.getElementById("createEventForm");
        createEventForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const formData = new FormData(createEventForm);
            const eventData = {
                title: formData.get("title"),
                description: formData.get("description"),
                category: formData.get("category"),
                dateTime: formData.get("dateTime"),
                maxSlots: formData.get("maxSlots"),
                availableSlots: formData.get("maxSlots"), // Initially same as max slots
                location: formData.get("location"),
            };

            // Get the email from the JWT token
            const email = getEmailFromToken();
            if (!email) {
                document.getElementById("createEventMessage").textContent = "Authentication error. Please log in again.";
                return;
            }

            // Fetch organizer_id from backend
            const organizerId = await getOrganizerIdByEmail(email);
            if (!organizerId) {
                document.getElementById("createEventMessage").textContent = "Could not retrieve organizer ID.";
                return;
            }

            eventData.organizer_id = organizerId; // Include the organizer_id in the event data

            try {
                // Send POST request to create event
                const response = await fetch("/api/events", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": "Bearer " + jwtToken,
                    },
                    body: JSON.stringify(eventData),
                });

                const responseData = await response.json();
                if (response.ok) {
                    document.getElementById("createEventMessage").textContent = "Event created successfully!";
                    createEventForm.reset();
                } else {
                    document.getElementById("createEventMessage").textContent = "Event creation failed: " + responseData.message;
                }
            } catch (error) {
                console.error("Error:", error);
                document.getElementById("createEventMessage").textContent = "An error occurred. Please try again later.";
            }
        });
    }

    // Function to extract the email from the JWT token
    function getEmailFromToken() {
        const token = localStorage.getItem("jwtToken");
        if (!token) return null;

        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.sub; // "sub" contains the email in the token
        } catch (error) {
            console.error("Error decoding JWT:", error);
            return null;
        }
    }

    // Function to fetch organizer_id from backend using email
    async function getOrganizerIdByEmail(email) {
        try {
            const response = await fetch(`/api/users/email/${email}`, {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + localStorage.getItem("jwtToken"),
                },
            });

            if (!response.ok) {
                console.error("Error fetching organizer ID: HTTP", response.status);
                return null;
            }

            const data = await response.json();
            return data.id; // Return the organizer's user_id
        } catch (error) {
            console.error("Error fetching organizer ID:", error);
            return null;
        }
    }

    // Function to check if the JWT token is expired
    function isTokenExpired(token) {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const currentTime = Math.floor(Date.now() / 1000);
        return payload.exp < currentTime; // Check if the token is expired
    }

    // Function to extract the user's role from the JWT token
    function getRoleFromToken(token) {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.role; // "role" contains the role of the user in the token
        } catch (error) {
            console.error("Error decoding JWT:", error);
            return null;
        }
    }
});
