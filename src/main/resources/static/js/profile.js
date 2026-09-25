document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "/login.html";
        return;
    }
    const username = document.getElementById("username");
    const email = document.getElementById("email");
    const fullName = document.getElementById("fullName");
    const bio = document.getElementById("bio");
    const accountType = document.getElementById("accountType");
    const profileAvatar = document.getElementById("profileAvatar");
    const editProfileBtn = document.getElementById("editProfileBtn");
    const editProfileForm = document.getElementById("editProfileForm");
    const cancelEditBtn = document.getElementById("cancelEditBtn");
    const saveProfileBtn = document.getElementById("saveProfileBtn");
    const logoutBtn = document.getElementById("logoutBtn");

    async function loadProfile() {
        try {
            const response = await fetch("/api/user/me", {
                method: "GET",
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (response.status === 401 || response.status === 403) {
                localStorage.removeItem("token");
                window.location.href = "/login.html";
                return;
            }
            if (!response.ok) {
                throw new Error("Failed to load profile");
            }
            const user = await response.json();
            console.log("Profile:", user);
            username.textContent = user.username;
            email.textContent = user.email;
            fullName.textContent = user.fullName || "Add your full name";
            bio.textContent = user.bio || "Add a bio";
            accountType.textContent = user.accountType;
            profileAvatar.textContent = user.username.charAt(0).toUpperCase();
            document.getElementById("editFullName").value = user.fullName || "";
            document.getElementById("editBio").value = user.bio || "";
            document.getElementById("editProfilePicture").value = user.profilePicture || "";
            document.getElementById("editPrivacy").value = user.privacy || "PUBLIC";
        } catch (error) {
            console.error("Profile loading error:", error);
        }
    }

    editProfileBtn.addEventListener("click", () => {
        editProfileForm.style.display = "block";
        editProfileBtn.style.display = "none";
    });

    cancelEditBtn.addEventListener("click", () => {
        editProfileForm.style.display = "none";
        editProfileBtn.style.display = "block";
    });

    saveProfileBtn.addEventListener("click", async () => {
        const updatedProfile = {
            fullName: document.getElementById("editFullName").value,
            bio: document.getElementById("editBio").value,
            profilePicture: document.getElementById("editProfilePicture").value,
            privacy: document.getElementById("editPrivacy").value
        };
        try {
            const response = await fetch("/api/user/profile", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(updatedProfile)
            });
            if (!response.ok) {
                throw new Error("Failed to update profile");
            }
            const updatedUser = await response.json();
            fullName.textContent = updatedUser.fullName || "Add your full name";
            bio.textContent = updatedUser.bio || "Add a bio";
            editProfileForm.style.display = "none";
            editProfileBtn.style.display = "block";
            alert("Profile updated successfully!");
        } catch (error) {
            console.error("Profile update error:", error);
            alert("Failed to update profile.");
        }
    });

    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("token");
        window.location.href = "/login.html";
    });

    loadProfile();
});