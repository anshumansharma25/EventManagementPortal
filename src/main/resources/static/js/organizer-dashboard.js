document.addEventListener("DOMContentLoaded", function () {
    // Create Event Form Handling
    const createEventForm = document.getElementById("createEventForm");
    createEventForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        // Collect form data
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

        try {
            // Fetch organizer_id from backend
            const organizerId = await getOrganizerIdByEmail(email);
            if (!organizerId) {
                document.getElementById("createEventMessage").textContent = "Could not retrieve organizer ID.";
                return;
            }

            // Include the organizer_id in the event data
            eventData.organizer_id = organizerId;

            // Send POST request to create event
            const response = await fetch("/api/events", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + localStorage.getItem("jwtToken"),
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
});
