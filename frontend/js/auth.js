document.getElementById("registerForm").addEventListener("submit", async event => {
    event.preventDefault();
    const message = document.getElementById("message");
    const body = {
        username: document.getElementById("username").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        accountType: document.getElementById("accountType").value
    };
    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        if (response.ok) {
            message.innerHTML = '<div class="alert alert-success">Registration successful! You can now login.</div>';
            document.getElementById("registerForm").reset();
        } else {
            message.innerHTML = `<div class="alert alert-danger">Registration failed: ${await response.text()}</div>`;
        }
    } catch (error) {
        message.innerHTML = '<div class="alert alert-danger">Unable to connect to the server.</div>';
    }
});
