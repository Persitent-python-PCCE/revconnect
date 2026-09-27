document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    const message = document.getElementById("message");
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: email, password: password })
            });
            const data = await response.json();
            if (response.ok) {
                localStorage.setItem("token", data.token);
                message.innerHTML = `<div class="alert alert-success">Login successful! Redirecting...</div>`;
                setTimeout(() => { window.location.href = "/index.html"; }, 500);
            } else {
                message.innerHTML = `<div class="alert alert-danger">${data.message || "Invalid email or password"}</div>`;
            }
        } catch (error) {
            console.error("Login error:", error);
            message.innerHTML = `<div class="alert alert-danger">Something went wrong. Please try again.</div>`;
        }
    });
});