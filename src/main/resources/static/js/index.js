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
        document.getElementById("guest-section").style.display = "block";
    } else if (role === "ATTENDEE") {
        document.getElementById("attendee-section").style.display = "block";
        fetchAvailableEvents();
        fetchBookedEvents();
        fetchEventHistory();
    } else if (role === "ORGANIZER") {
        document.getElementById("organizer-section").style.display = "block";
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

function fetchAvailableEvents() {
    fetch("/api/events")
        .then(res => res.json())
        .then(data => {
            const eventList = document.getElementById("available-events");
            eventList.innerHTML = data.map(event => `<p>${event.title} - ${event.location} <button onclick="bookEvent(${event.id})">Book</button></p>`).join("");
        });
}

function fetchBookedEvents() {
    fetch("/api/bookings", {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
    })
        .then(res => res.json())
        .then(data => {
            const eventList = document.getElementById("booked-events");
            eventList.innerHTML = data.map(event => `<p>${event.title} - ${event.bookingStatus}</p>`).join("");
        });
}

function fetchEventHistory() {
    fetch("/api/bookings/history", {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
    })
        .then(res => res.json())
        .then(data => {
            const eventList = document.getElementById("event-history");
            eventList.innerHTML = data.map(event => `<p>${event.title} - ${event.date}</p>`).join("");
        });
}

function fetchOrganizerEvents() {
    fetch("/api/events/organizer", {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
    })
        .then(res => res.json())
        .then(data => {
            const eventList = document.getElementById("organizer-events");
            eventList.innerHTML = data.map(event =>
                `<p>${event.title} - ${event.date}
                <button onclick="updateEvent(${event.id})">Update</button>
                <button onclick="cancelEvent(${event.id})">Cancel</button></p>`).join("");
        });
}

function createEvent() {
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
    }).then(() => fetchOrganizerEvents());
}
