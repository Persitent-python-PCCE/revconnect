document.getElementById("registerForm").addEventListener("submit", async function (event) {
    event.preventDefault();
    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const accountType = document.getElementById("accountType").value;
    const message = document.getElementById("message");
    const userData = {
        username: username,
        email: email,
        password: password,
        accountType: accountType
    };
    try {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userData)
        });
        if (response.ok) {
            message.innerHTML = `<div class="alert alert-success">Registration successful!</div>`;
            document.getElementById("registerForm").reset();
        } else {
            const errorMessage = await response.text();
            message.innerHTML = `<div class="alert alert-danger">Registration failed: ${errorMessage}</div>`;
        }
    } catch (error) {
        console.error(error);
        message.innerHTML = `<div class="alert alert-danger">Unable to connect to the server.</div>`;
    }
});