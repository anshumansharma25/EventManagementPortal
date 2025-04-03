document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("userRole");

    // Get sections
    const guestSection = document.getElementById('guest-section');
    const attendeeSection = document.getElementById('attendee-section');
    const organizerSection = document.getElementById('organizer-section');

    guestSection.style.display = 'none';
    attendeeSection.style.display = 'none';
    organizerSection.style.display = 'none';

    // Show sections based on role
    if (!token || !role) {
        guestSection.style.display = "block";
    } else if (role === "ATTENDEE") {
        attendeeSection.style.display = "block";
        fetchAvailableEvents();
        fetchBookedEvents();
    } else if (role === "ORGANIZER") {
        organizerSection.style.display = "block";
        fetchOrganizerEvents();
    }

    // Logout
    document.getElementById("logout-btn").style.display = token ? "block" : "none";
    document.getElementById("logout-btn").addEventListener("click", function () {
        localStorage.clear();
        window.location.reload();
    });

    // Event creation
    document.getElementById("event-form")?.addEventListener("submit", function (event) {
        event.preventDefault();
        createEvent();
    });
});

// Event cache for optimization
const eventCache = new Map();

async function getEventDetails(eventId) {
    if (eventCache.has(eventId)) {
        return eventCache.get(eventId);
    }
    const response = await fetch(`/api/events/${eventId}`);
    if (!response.ok) throw new Error("Failed to fetch event details");
    const event = await response.json();
    eventCache.set(eventId, event);
    return event;
}

function fetchAvailableEvents() {
    const eventList = document.getElementById("available-events");
    if (!eventList) return;

    eventList.innerHTML = '<div class="loading">Loading events...</div>';

    // First fetch all events
    fetch("/api/events")
        .then(async response => {
            if (!response.ok) throw new Error(await response.text());
            return response.json();
        })
        .then(events => {
            // Then fetch user's bookings to check which events are booked
            fetch("/api/bookings/user", {
                headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
            })
            .then(async bookingsResponse => {
                if (!bookingsResponse.ok) throw new Error(await bookingsResponse.text());
                return bookingsResponse.json();
            })
            .then(bookings => {
                const bookedEventIds = bookings.map(b => b.eventId);

                eventList.innerHTML = events.map(event => `
                    <div class="event-card">
                        <h3>${event.title}</h3>
                        <p>📍 Location: ${event.location}</p>
                        <p>🗓 Date: ${new Date(event.dateTime).toLocaleString()}</p>
                        <p>🪑 Slots: ${event.availableSlots} / ${event.maxSlots}</p>
                        <button class="book-btn"
                            onclick="bookEvent(${event.id})"
                            ${event.availableSlots === 0 || bookedEventIds.includes(event.id) ? 'disabled' : ''}>
                            ${event.availableSlots === 0 ? 'FULL' :
                             bookedEventIds.includes(event.id) ? 'BOOKED' : 'BOOK'}
                        </button>
                    </div>
                `).join("");
            })
            .catch(error => {
                console.error("Error loading bookings:", error);
                eventList.innerHTML = `<div class="error">Failed to load events</div>`;
            });
        })
        .catch(error => {
            console.error("Error loading events:", error);
            eventList.innerHTML = `<div class="error">Failed to load events: ${error.message}</div>`;
        });
}

async function bookEvent(eventId) {
    const messageElement = document.getElementById("booking-message");
    if (!messageElement) {
        console.error("Error: Could not find booking-message element");
        return;
    }

    try {
        // Clear previous messages and set loading state
        messageElement.textContent = "Processing booking...";
        messageElement.style.color = "blue";
        messageElement.style.display = "block";

        const response = await fetch("/api/bookings", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem("token")}`
            },
            body: JSON.stringify({ eventId })
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: "Booking failed" }));
            throw new Error(error.message);
        }

        const bookingData = await response.json();

        // Show success message
        messageElement.textContent = "✓ Booking successful!";
        messageElement.style.color = "green";

        // Hide message after 3 seconds
        setTimeout(() => {
            messageElement.style.display = "none";
        }, 3000);

        // Refresh both available and booked events
        fetchAvailableEvents();
        fetchBookedEvents();

    } catch (error) {
        messageElement.textContent = `✗ ${error.message}`;
        messageElement.style.color = "red";
        console.error("Booking error:", error);
    }
}

async function fetchBookedEvents() {
    const eventList = document.getElementById("booked-events");
    if (!eventList) return;

    eventList.innerHTML = '<div class="loading">Loading your bookings...</div>';

    try {
        const response = await fetch("/api/bookings/user", {
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        const bookings = await response.json();

        // Get event details for each booking
        const events = await Promise.all(
            bookings.map(booking => getEventDetails(booking.eventId))
        ).catch(() => []);

        if (events.length === 0) {
            eventList.innerHTML = '<div class="empty">No bookings found</div>';
            return;
        }

        eventList.innerHTML = events.map(event => `
            <div class="event-card booked">
                <h3>${event.title}</h3>
                <p>📅 Date: ${new Date(event.dateTime).toLocaleString()}</p>
                <p>📍 Location: ${event.location}</p>
                <p>🪑 Slots: ${event.availableSlots} / ${event.maxSlots}</p>
            </div>
        `).join("");

    } catch (error) {
        console.error("Failed to fetch booked events:", error);
        eventList.innerHTML = `<div class="error">Failed to load bookings: ${error.message}</div>`;
    }
}

function fetchOrganizerEvents() {
    const eventList = document.getElementById("organizer-events");
    if (!eventList) return;

    eventList.innerHTML = '<div class="loading">Loading your events...</div>';

    fetch("/api/events/organizer", {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
    })
    .then(async response => {
        if (!response.ok) throw new Error(await response.text());
        return response.json();
    })
    .then(data => {
        eventList.innerHTML = data.map(event => `
            <div class="event-card">
                <h3>${event.title}</h3>
                <p>📅 Date: ${new Date(event.dateTime).toLocaleString()}</p>
                <p>🪑 Slots: ${event.availableSlots} / ${event.maxSlots}</p>
                <button onclick="updateEvent(${event.id})">Update</button>
                <button onclick="cancelEvent(${event.id})">Cancel</button>
            </div>
        `).join("");
    })
    .catch(error => {
        console.error("Error loading organizer events:", error);
        eventList.innerHTML = `<div class="error">Failed to load events: ${error.message}</div>`;
    });
}

function createEvent() {
    const form = document.getElementById("event-form");
    if (!form) return;

    const messageElement = document.getElementById("event-message");
    if (messageElement) {
        messageElement.textContent = "Creating event...";
        messageElement.style.color = "inherit";
    }

    const eventDetails = {
        title: document.getElementById("event-title").value,
        description: document.getElementById("event-description").value,
        dateTime: document.getElementById("event-date").value,
        maxSlots: document.getElementById("event-capacity").value,
        location: document.getElementById("event-location").value
    };

    fetch("/api/events/create", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`
        },
        body: JSON.stringify(eventDetails)
    })
    .then(async response => {
        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: "Unknown error" }));
            throw new Error(error.message || "Event creation failed");
        }
        return response.json();
    })
    .then(() => {
        if (messageElement) {
            messageElement.textContent = "✓ Event created successfully!";
            messageElement.style.color = "green";
        }
        fetchOrganizerEvents();
        form.reset();
    })
    .catch(error => {
        if (messageElement) {
            messageElement.textContent = `✗ ${error.message}`;
            messageElement.style.color = "red";
        }
        console.error("Event creation error:", error);
    });
}