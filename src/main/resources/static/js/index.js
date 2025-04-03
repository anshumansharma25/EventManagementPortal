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
                        <p class="event-date">📅 ${event.formattedDateTime || 'Date not available'}</p>
                        <p>📍 ${event.location}</p>
                        <p>🪑 ${event.availableSlots}/${event.maxSlots} slots available</p>
                        <button class="book-btn"
                            onclick="bookEvent(${event.id})"
                            ${event.availableSlots <= 0 || bookedEventIds.includes(event.id) ? 'disabled' : ''}>
                            ${event.availableSlots <= 0 ? 'FULL' :
                             bookedEventIds.includes(event.id) ? 'BOOKED' : 'BOOK'}
                        </button>
                    </div>
                `).join("");
            })
            .catch(error => {
                console.error("Error loading bookings:", error);
                // Still show events even if bookings failed to load
                eventList.innerHTML = events.map(event => `
                    <div class="event-card">
                        <h3>${event.title}</h3>
                        <p class="event-date">📅 ${event.formattedDateTime || 'Date not available'}</p>
                        <p>📍 ${event.location}</p>
                        <p>🪑 ${event.availableSlots}/${event.maxSlots} slots available</p>
                        <button class="book-btn"
                            onclick="bookEvent(${event.id})"
                            ${event.availableSlots <= 0 ? 'disabled' : ''}>
                            ${event.availableSlots <= 0 ? 'FULL' : 'BOOK'}
                        </button>
                    </div>
                `).join("");
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

    try {
        eventList.innerHTML = `
            <div class="loading-state">
                <div class="spinner"></div>
                <p>Loading your bookings...</p>
            </div>
        `;

        const response = await fetch("/api/bookings/user", {
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => null);
            throw new Error(errorData?.message || `HTTP error! Status: ${response.status}`);
        }

        const bookings = await response.json();

        if (!Array.isArray(bookings)) {
            throw new Error("Invalid bookings data received");
        }

        // Handle empty state
        if (bookings.length === 0) {
            eventList.innerHTML = `
                <div class="empty-state">
                    <p>You haven't booked any events yet</p>
                    <a href="/events" class="btn">Browse Events</a>
                </div>
            `;
            return;
        }

        // Render bookings
        eventList.innerHTML = bookings.map(booking => {
            const status = booking.status?.toUpperCase() || 'CONFIRMED';
            const isCancelled = status === 'EVENT_CANCELLED';
            const eventDate = booking.eventDate ? new Date(booking.eventDate) : null;
            const formattedDate = eventDate ?
                eventDate.toLocaleDateString('en-US', {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                }) : 'Date not specified';

            return `
                <div class="booking-card ${isCancelled ? 'cancelled' : ''}">
                    <div class="booking-header">
                        <h3 class="event-title">${escapeHtml(booking.eventTitle || 'Untitled Event')}</h3>
                        <div class="status-badge ${isCancelled ? 'cancelled' : 'confirmed'}">
                            ${isCancelled ? 'Event Cancelled' : 'Confirmed'}
                        </div>
                    </div>
                    <div class="booking-details">
                        <p class="booking-date">📅 ${formattedDate}</p>
                        <p class="booking-location">📍 ${escapeHtml(booking.location || 'Location not specified')}</p>
                    </div>
                </div>
            `;
        }).join("");

    } catch (error) {
        console.error("Booking load error:", error);
        eventList.innerHTML = `
            <div class="error-state">
                <img src="/images/error-icon.svg" alt="Error">
                <p>Failed to load bookings</p>
                <p class="error-detail">${escapeHtml(error.message)}</p>
                <button onclick="fetchBookedEvents()" class="retry-btn">
                    Retry
                </button>
            </div>
        `;
    }
}

// Helper function to prevent XSS
function escapeHtml(unsafe) {
    return unsafe?.replace(/[&<>"']/g, match => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[match])) || '';
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

function renderBooking(booking) {
    // Determine status class and text
    const isCancelled = booking.status === 'EVENT_CANCELLED' || booking.isCancelled;
    const statusClass = isCancelled ? 'status-cancelled' : 'status-confirmed';
    const statusText = booking.status === 'EVENT_CANCELLED'
        ? 'Event Cancelled'
        : booking.isCancelled
            ? 'Booking Cancelled'
            : 'Confirmed';

    // Create booking card HTML
    return `
        <div class="booking-card ${statusClass}">
            <h3>${booking.eventTitle || 'Untitled Event'}</h3>
            <p class="booking-date">📅 ${booking.eventDate ? new Date(booking.eventDate).toLocaleString() : 'Date not available'}</p>
            <p class="booking-location">📍 ${booking.location || 'Location not specified'}</p>
            <div class="status-badge ${statusClass}">${statusText}</div>

            ${!isCancelled ? `
                <button onclick="cancelBooking('${booking.id}')" class="cancel-btn">
                    Cancel Booking
                </button>
            ` : ''}
        </div>
    `;
}