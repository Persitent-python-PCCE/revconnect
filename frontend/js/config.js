// When served by Spring Boot, keep this empty.
// When running the standalone frontend with `python -m http.server 5500`,
// point it to the backend.
const API_BASE_URL = window.REVCONNECT_API_URL || (
    location.port === "5500" ? "http://localhost:9000" : ""
);
