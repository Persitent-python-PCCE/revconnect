document.addEventListener("DOMContentLoaded", () => {
    const f = document.getElementById("loginForm");
    const m = document.getElementById("message");

    f.addEventListener("submit", async (e) => {
        e.preventDefault();

        try {
            const r = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: document.getElementById("email").value,
                    password: document.getElementById("password").value
                })
            });

            const d = await r.json();

            if (r.ok) {
                localStorage.setItem("token", d.token);
                m.innerHTML = '<div class="alert alert-success">Login successful! Redirecting...</div>';
                setTimeout(() => location.href = "index.html", 500);
            } else {
                m.innerHTML = `<div class="alert alert-danger">${d.message || "Invalid email or password"}</div>`;
            }
        } catch (e) {
            m.innerHTML = '<div class="alert alert-danger">Unable to connect to the server.</div>';
        }
    });
});