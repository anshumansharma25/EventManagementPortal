document.addEventListener("DOMContentLoaded", function() {
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
    document.getElementById("logout-btn").addEventListener("click", function() {
        localStorage.clear();
        window.location.reload();
    });

    // Event creation
    document.getElementById("event-form")?.addEventListener("submit", function(event) {
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

function generateEventDetails(event) {
    return `
<p class="event-detail">
<i class="fas fa-calendar-alt"></i>
${formatDate(event.dateTime)}
</p>
<p class="event-detail">
<i class="fas fa-map-marker-alt"></i>
${escapeHtml(event.location)}
</p>
<p class="event-detail">
<i class="fas fa-ticket-alt"></i>
${event.availableSlots}/${event.maxSlots} slots
</p>
`;
}

function generateEventActions(event) {
    const isCancelled = ['EVENT_CANCELLED', 'Event_cancelled'].includes(event.status) ||
        event.isCancelled === true;

    return `
        <button class="update-btn ${isCancelled ? 'disabled-btn' : ''}"
            onclick="${isCancelled ? '' : `openUpdateModal(${event.id})`}"
            ${isCancelled ? 'disabled' : ''}>
            <i class="fas fa-edit"></i> Update
        </button>
        ${!isCancelled ? `
            <button class="cancel-event-btn" onclick="openCancelConfirmation(${event.id})">
                <i class="fas fa-times"></i> Cancel Event
            </button>
        ` : ''}
    `;
}

function generateBookingDetails(booking) {
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
        <p class="event-description">${escapeHtml(booking.eventDescription)}</p>
        <div class="booking-details">
            <p class="booking-detail">
                <i class="fas fa-calendar-alt"></i>
                ${formattedDate}
            </p>
            <p class="booking-detail">
                <i class="fas fa-map-marker-alt"></i>
                ${escapeHtml(booking.location)}
            </p>
            <p class="booking-detail">
                <i class="fas fa-ticket-alt"></i>
                Booking ID: ${booking.id}
            </p>
        </div>
    `;
}

function fetchAvailableEvents(forceRefresh = false) {
    const eventList = document.getElementById("available-events");
    if (!eventList) return;

    eventList.innerHTML = '<div class="loading">Loading events...</div>';

    fetch("/api/events")
        .then(async response => {
            if (!response.ok) throw new Error(await response.text());
            return response.json();
        })
        .then(events => {
            fetch("/api/bookings/user", {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`
                }
            })
            .then(async bookingsResponse => {
                if (!bookingsResponse.ok) throw new Error(await bookingsResponse.text());
                return bookingsResponse.json();
            })
            .then(bookings => {
                const bookedEventIds = bookings.map(b => b.eventId);

                eventList.innerHTML = events.map(event => {
                    const isCancelled = event.isCancelled || event.cancelled;
                    const isBooked = bookedEventIds.includes(event.id);
                    const slotsFull = event.availableSlots <= 0;

                    const buttonDisabled = isCancelled || isBooked || slotsFull;
                    const buttonLabel = isCancelled
                        ? 'EVENT CANCELLED'
                        : isBooked
                            ? 'BOOKED'
                            : slotsFull
                                ? 'FULL'
                                : 'BOOK NOW';

                    return `
                    <div class="event-card">
                        <div class="event-header">
                            <h3 class="event-title">${escapeHtml(event.title)}</h3>
                            <span class="status-badge ${isCancelled ? 'cancelled' : 'active'}">
                                ${isCancelled ? 'CANCELLED' : 'ACTIVE'}
                            </span>
                        </div>
                        <div class="event-body">
                            <div class="event-details">
                                <p class="event-detail"><i class="fas fa-calendar-alt"></i> ${event.formattedDateTime || 'Date not available'}</p>
                                <p class="event-detail"><i class="fas fa-map-marker-alt"></i> ${escapeHtml(event.location)}</p>
                                <p class="event-detail"><i class="fas fa-users"></i> ${event.availableSlots}/${event.maxSlots} slots available</p>
                            </div>
                        </div>
                        <div class="event-actions">
                            <button class="book-btn"
                                onclick="bookEvent(${event.id})"
                                ${buttonDisabled ? 'disabled' : ''}>
                                ${buttonLabel}
                            </button>
                        </div>
                    </div>
                    `;
                }).join("");
            })
            .catch(error => {
                console.error("Error loading bookings:", error);
                eventList.innerHTML = events.map(event => {
                    const isCancelled = event.isCancelled || event.cancelled;
                    const slotsFull = event.availableSlots <= 0;

                    const buttonDisabled = isCancelled || slotsFull;
                    const buttonLabel = isCancelled ? 'EVENT CANCELLED' : slotsFull ? 'FULL' : 'BOOK';

                    return `
                    <div class="event-card">
                        <h3>${event.title}</h3>
                        <p class="event-date">📅 ${event.formattedDateTime || 'Date not available'}</p>
                        <p>📍 ${event.location}</p>
                        <p>🪑 ${event.availableSlots}/${event.maxSlots} slots available</p>
                        <button class="book-btn"
                            onclick="bookEvent(${event.id})"
                            ${buttonDisabled ? 'disabled' : ''}>
                            ${buttonLabel}
                        </button>
                    </div>
                    `;
                }).join("");
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

async function fetchBookedEvents(forceRefresh = false) {
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
            throw new Error(errorData?.message || `Failed to load bookings`);
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

        // Render bookings with all details
        eventList.innerHTML = bookings.map(booking => {
            const isCancelled = booking.status === 'Event_cancelled';
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
                        <div class="status-badge ${isCancelled ? 'event-cancelled' : 'confirmed'}">
                                            ${isCancelled ? 'EVENT CANCELLED' : 'CONFIRMED'}
                                        </div>
                    </div>
                    <div class="booking-body">
                        <p class="event-description">${escapeHtml(booking.eventDescription || 'No description available')}</p>
                        <div class="booking-details">
                            <p class="booking-detail">
                                <i class="fas fa-calendar-alt"></i>
                                ${formattedDate}
                            </p>
                            <p class="booking-detail">
                                <i class="fas fa-map-marker-alt"></i>
                                ${escapeHtml(booking.location || 'Location not specified')}
                            </p>
                            <p class="booking-detail">
                                <i class="fas fa-ticket-alt"></i>
                                Booking ID: ${booking.id}
                            </p>
                        </div>
                    </div>
                </div>
            `;
        }).join("");

    } catch (error) {
        console.error("Booking load error:", error);
        eventList.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Failed to load bookings</p>
                <p class="error-detail">${escapeHtml(error.message)}</p>
                <button onclick="fetchBookedEvents()" class="retry-btn">
                    <i class="fas fa-sync-alt"></i> Try Again
                </button>
            </div>
        `;
    }
}

function showError(element, message) {
    if (element) {
        element.textContent = `✗ ${message}`;
        element.style.color = "red";
    }
}

async function fetchOrganizerEvents(forceRefresh = false) {
    const eventList = document.getElementById('organizer-events');
    if (!eventList) return;

    try {
        const url = `/api/events/organizer${forceRefresh ? `?refresh=${Date.now()}` : ''}`;
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) throw new Error(await response.text());

        const events = await response.json();
        console.log('Organizer Events Data:', events); // Debugging

        eventList.innerHTML = events.map(event => {
            // Clear cached version if exists
            eventCache.set(event.id, event);
            return generateOrganizerEventCard(event);
        }).join('');

    } catch (error) {
        eventList.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Failed to load events</p>
                <p class="error-message">${escapeHtml(error.message)}</p>
                <button onclick="fetchOrganizerEvents()" class="retry-btn">
                    <i class="fas fa-sync-alt"></i> Try Again
                </button>
            </div>
        `;
    }
}

// Helper functions
function formatDate(dateString) {
    if (!dateString) return 'Date not set';
    const options = {
        weekday: 'short',
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

function escapeHtml(unsafe) {
    return unsafe?.replace(/[&<>"']/g, match => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    } [match])) || '';
}

function createEvent() {
    const form = document.getElementById("event-form");
    if (!form) return;

    // Get form elements
    const titleInput = document.getElementById("event-title");
    const descriptionInput = document.getElementById("event-description");
    const dateInput = document.getElementById("event-date");
    const timeInput = document.getElementById("event-time");
    const capacityInput = document.getElementById("event-capacity");
    const locationInput = document.getElementById("event-location");
    const categoryInput = document.getElementById("event-category");
    const messageElement = document.getElementById("event-message");

    // Clear previous messages
    if (messageElement) {
        messageElement.textContent = "";
        messageElement.style.color = "inherit";
    }

    // Validate inputs
    try {
        // Required field validation
        if (!titleInput.value.trim()) {
            showError(messageElement, "Event title is required");
            return;
        }
        if (!descriptionInput.value.trim()) {
            showError(messageElement, "Description is required");
            return;
        }
        if (!dateInput.value || !timeInput.value) {
            showError(messageElement, "Date and time are required");
            return;
        }
        if (!capacityInput.value) {
            showError(messageElement, "Capacity is required");
            return;
        }
        if (!locationInput.value.trim()) {
            showError(messageElement, "Location is required");
            return;
        }
        if (!categoryInput.value.trim()) {
            showError(messageElement, "Category is required");
            return;
        }

        // Numeric validation
        const maxSlots = parseInt(capacityInput.value);
        if (isNaN(maxSlots)) {
            showError(messageElement, "Capacity must be a number");
            return;
        }
        if (maxSlots <= 0) {
            showError(messageElement, "Capacity must be greater than 0");
            return;
        }

        // Date/time validation
        const eventDateTime = new Date(`${dateInput.value}T${timeInput.value}`);
        const now = new Date();
        if (eventDateTime <= now) {
            showError(messageElement, "Event date/time must be in the future");
            return;
        }

        // Prepare payload
        const eventDetails = {
            title: titleInput.value.trim(),
            description: descriptionInput.value.trim(),
            dateTime: eventDateTime.toISOString(),
            maxSlots: maxSlots,
            location: locationInput.value.trim(),
            category: categoryInput.value.trim()
        };

        // Show loading state
        if (messageElement) {
            messageElement.textContent = "Creating event...";
            messageElement.style.color = "inherit";
        }

        // API call
        fetch("/api/events/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${localStorage.getItem("token")}`
                },
                body: JSON.stringify(eventDetails)
            })
            .then(async response => {
                if (!response.ok) {
                    const error = await response.json().catch(() => ({
                        message: "Event creation failed"
                    }));
                    throw new Error(error.message || "Unknown error occurred");
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
                showError(messageElement, error.message);
                console.error("Event creation error:", error);
            });

    } catch (error) {
        showError(messageElement, error.message);
        console.error("Validation error:", error);
    }
}

// Open modal with event data
function openUpdateModal(eventId) {
    fetch(`/api/events/${eventId}`, {
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("token")}`
            }
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to fetch event');
            return response.json();
        })
        .then(event => {
            document.getElementById('update-event-id').value = event.id;
            document.getElementById('update-event-title').value = event.title;
            document.getElementById('update-event-description').value = event.description;

            if (event.dateTime) {
                const date = new Date(event.dateTime);
                document.getElementById('update-event-date').valueAsDate = date;
                document.getElementById('update-event-time').value =
                    `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
            }

            document.getElementById('update-event-capacity').value = event.maxSlots;
            document.getElementById('update-event-location').value = event.location;
            document.getElementById('update-event-category').value = event.category;

            // Show modal
            document.getElementById('update-event-modal').style.display = 'block';
        })
        .catch(error => {
            showMessage('update-event-message', `Error: ${error.message}`, 'error');
        });
}

// Handle form submission
document.getElementById('update-event-form').addEventListener('submit', function(e) {
    e.preventDefault();
    updateEvent();
});

function updateEvent() {
    const form = document.getElementById("update-event-form");
    const messageEl = document.getElementById("update-event-message");
    const eventId = document.getElementById("update-event-id").value;

    // Get form values
    const eventData = {
        title: document.getElementById("update-event-title").value.trim(),
        description: document.getElementById("update-event-description").value.trim(),
        dateTime: `${document.getElementById("update-event-date").value}T${document.getElementById("update-event-time").value}:00`,
        maxSlots: parseInt(document.getElementById("update-event-capacity").value),
        location: document.getElementById("update-event-location").value.trim(),
        category: document.getElementById("update-event-category").value.trim()
    };

    // Clear previous messages
    messageEl.textContent = '';
    messageEl.className = 'message';

    // Show loading
    showMessage('update-event-message', 'Updating event...', 'info');

    fetch(`/api/events/${eventId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem("token")}`
            },
            body: JSON.stringify(eventData)
        })
        .then(async response => {
            const data = await response.json();
            if (!response.ok) {
                throw new Error(data.error || 'Update failed');
            }
            return data;
        })
        .then(updatedEvent => {
            showMessage('update-event-message', 'Event updated successfully!', 'success');
            // Refresh events list and close modal after delay
            setTimeout(() => {
                document.getElementById('update-event-modal').style.display = 'none';
                fetchOrganizerEvents(); // Or whichever function refreshes your events list
            }, 1500);
        })
        .catch(error => {
            showMessage('update-event-message', `Error: ${error.message}`, 'error');
        });
}

// Helper function to show messages
function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    if (!element) return;

    element.textContent = message;
    element.className = `message ${type}`;
    element.style.display = 'block';

    // Auto-hide success messages after 5 seconds
    if (type === 'success') {
        setTimeout(() => {
            element.style.display = 'none';
        }, 5000);
    }
}

// Close modal when clicking X
document.querySelector('.close').addEventListener('click', function() {
    document.getElementById('update-event-modal').style.display = 'none';
});

// Close modal when clicking outside
window.addEventListener('click', function(event) {
    const modal = document.getElementById('update-event-modal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
});

function renderBooking(booking) {
    // Determine status class and text
    const isCancelled = booking.status === 'Event_cancelled' || booking.isCancelled;
    const statusClass = isCancelled ? 'status-cancelled' : 'status-confirmed';
    const statusText = booking.status === 'Event_cancelled' ?
        'Event Cancelled' :
        booking.isCancelled ?
        'Booking Cancelled' :
        'Confirmed';

    // Create booking card HTML
    return `
        <div class="booking-card ${statusClass}">
            <h3>${booking.eventTitle || 'Untitled Event'}</h3>
            <p class="event-description">${escapeHtml(booking.eventDescription || 'No description available')}</p>
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

// Global variable to store event ID being canceled
let currentEventIdToCancel = null;

// Function to open cancel confirmation
function openCancelConfirmation(eventId) {
    currentEventIdToCancel = eventId;
    document.getElementById('confirmation-modal').classList.add('active');
}

async function cancelEvent() {
    if (!currentEventIdToCancel) return;

    const messageElement = document.getElementById('event-message');
    try {
        showMessage('event-message', 'Cancelling event...', 'info');

        // Clear cached data first
        eventCache.delete(currentEventIdToCancel);
        eventCache.delete("organizer");
        eventCache.delete("bookings");

        const response = await fetch(`/api/events/${currentEventIdToCancel}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to cancel event');
        }



        // Force refresh with cache busting
        await Promise.all([
            fetchOrganizerEvents(true),
            fetchBookedEvents(true)
        ]);

        showMessage('event-message', 'Event & bookings cancelled successfully', 'success');
        document.getElementById('confirmation-modal').classList.remove('active');

    } catch (error) {
        console.error('Cancellation failed:', error);
        showMessage('event-message', `Error: ${error.message}`, 'error');
    } finally {
        currentEventIdToCancel = null;
    }
}

// Event listeners for confirmation modal
document.getElementById('confirm-cancel-btn').addEventListener('click', cancelEvent);
document.getElementById('cancel-confirm-btn').addEventListener('click', function() {
    document.getElementById('confirmation-modal').classList.remove('active');
    currentEventIdToCancel = null;
});


function generateOrganizerEventCard(event) {
    const isCancelled = ['EVENT_CANCELLED', 'Event_cancelled'].includes(event.status) ||
        event.isCancelled === true ||
        event.cancelled === true;

    return `
        <div class="event-card" data-event-id="${event.id}">
            <div class="event-header">
                <h3 class="event-title">${escapeHtml(event.title)}</h3>
                <span class="status-badge ${isCancelled ? 'event-cancelled' : 'event-active'}">
                    ${isCancelled ? 'EVENT CANCELLED' : 'ACTIVE'}
                </span>
            </div>
            <div class="event-body">
                <p class="event-description">${escapeHtml(event.description || 'No description available')}</p>
                <div class="event-details">
                    ${generateEventDetails(event)}
                </div>
            </div>
            <div class="event-actions">
                ${generateEventActions(event)}
            </div>
        </div>
    `;
}

function generateBookingCard(booking) {
    const isCancelled = ['EVENT_CANCELLED', 'Event_cancelled'].includes(booking.eventStatus) ||
        booking.isCancelled === true;

    return `
<div class="booking-card ${isCancelled ? 'cancelled' : ''}">
<div class="booking-header">
<h3>${escapeHtml(booking.eventTitle)}</h3>
<div class="status-badge ${isCancelled ? 'event-cancelled' : 'confirmed'}">
${isCancelled ? 'EVENT CANCELLED' : 'CONFIRMED'}
</div>
</div>
<div class="booking-body">
${generateBookingDetails(booking)}
</div>
</div>
`;
}