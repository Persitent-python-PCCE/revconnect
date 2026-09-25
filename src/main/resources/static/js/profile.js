document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "/login.html";
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const rawTargetUserId = urlParams.get("userId");
    let targetUserId = rawTargetUserId ? Number(rawTargetUserId) : null;
    if (targetUserId !== null && isNaN(targetUserId)) {
        alert("Invalid user ID");
        window.location.href = "/index.html";
        return;
    }
    
    // UI Elements
    const usernameEl = document.getElementById("username");
    const emailEl = document.getElementById("email");
    const fullNameEl = document.getElementById("fullName");
    const bioEl = document.getElementById("bio");
    const accountTypeEl = document.getElementById("accountType");
    const profileAvatarEl = document.getElementById("profileAvatar");
    const editProfileBtn = document.getElementById("editProfileBtn");
    const editProfileForm = document.getElementById("editProfileForm");
    const cancelEditBtn = document.getElementById("cancelEditBtn");
    const saveProfileBtn = document.getElementById("saveProfileBtn");
    const logoutBtn = document.getElementById("logoutBtn");
    const relationshipActions = document.getElementById("relationshipActions");
    const statPending = document.getElementById("statPending");
    const statSent = document.getElementById("statSent");

    // Modal
    const listModal = document.getElementById("listModal");
    const listModalTitle = document.getElementById("listModalTitle");
    const listModalBody = document.getElementById("listModalBody");
    const listModalCloseBtn = document.getElementById("listModalCloseBtn");

    listModalCloseBtn.addEventListener("click", () => {
        listModal.style.display = "none";
    });

    listModal.addEventListener("click", (e) => {
        if (e.target === listModal) {
            listModal.style.display = "none";
        }
    });

    let isMe = false;
    let currentUserProfile = null;
    let currentRelationship = null;

    async function initProfile() {
        try {
            // Determine if viewing own profile
            const meRes = await fetch("/api/user/me", {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!meRes.ok) throw new Error("Failed to load session");
            const meData = await meRes.json();
            
            if (!targetUserId || parseInt(targetUserId) === meData.id) {
                isMe = true;
                targetUserId = meData.id;
                currentUserProfile = meData;
            } else {
                const userRes = await fetch(`/api/user/${targetUserId}`, {
                    headers: { "Authorization": `Bearer ${token}` }
                });
                if (!userRes.ok) throw new Error("Failed to load user");
                currentUserProfile = await userRes.json();
            }

            renderProfileInfo();

            if (isMe) {
                editProfileBtn.style.display = "block";
                statPending.style.display = "block";
                statSent.style.display = "block";
                relationshipActions.style.setProperty("display", "none", "important");
            } else {
                editProfileBtn.style.display = "none";
                statPending.style.display = "none";
                statSent.style.display = "none";
                relationshipActions.style.setProperty("display", "flex", "important");
                await loadRelationshipStatus();
            }

            await loadStats();
        } catch (error) {
            console.error(error);
            alert("Error loading profile");
        }
    }

    function renderProfileInfo() {
        const user = currentUserProfile;
        usernameEl.textContent = user.username;
        emailEl.textContent = user.email;
        fullNameEl.textContent = user.fullName || "No name set";
        bioEl.textContent = user.bio || "No bio";
        accountTypeEl.textContent = user.accountType;
        profileAvatarEl.textContent = user.username.charAt(0).toUpperCase();
        
        if (isMe) {
            document.getElementById("editFullName").value = user.fullName || "";
            document.getElementById("editBio").value = user.bio || "";
            document.getElementById("editProfilePicture").value = user.profilePicture || "";
            document.getElementById("editPrivacy").value = user.privacy || "PUBLIC";
        }
    }

    async function loadStats() {
        if (!isMe) return; // Only load stats for current user
        try {
            const stats = await ConnectionApi.getConnectionStats();
            document.getElementById("countFollowers").textContent = stats.followerCount;
            document.getElementById("countFollowing").textContent = stats.followingCount;
            document.getElementById("countConnections").textContent = stats.connectionCount;
            document.getElementById("countPending").textContent = stats.pendingReceivedCount;
            document.getElementById("countSent").textContent = stats.pendingSentCount;
        } catch (e) {
            console.error("Failed to load stats", e);
        }
    }

    async function loadRelationshipStatus() {
        try {
            currentRelationship = await ConnectionApi.getRelationshipStatus(targetUserId);
            renderRelationshipButtons();
        } catch (e) {
            console.error("Failed to load relationship status", e);
        }
    }

    function renderRelationshipButtons() {
        relationshipActions.innerHTML = "";
        
        // Connect Button logic
        if (currentRelationship.connectionStatus === "CONNECTED") {
            const connectBtn = document.createElement("button");
            connectBtn.className = "btn btn-outline-success";
            connectBtn.textContent = "Connected";
            connectBtn.onclick = async () => {
                connectBtn.disabled = true;
                try {
                    await ConnectionApi.removeConnection(targetUserId);
                    currentRelationship.connectionStatus = "NOT_CONNECTED";
                    renderRelationshipButtons();
                    loadStats();
                } catch(e) { alert(e.message); connectBtn.disabled = false; }
            };
            relationshipActions.appendChild(connectBtn);
        } else if (currentRelationship.connectionStatus === "PENDING_SENT") {
            const connectBtn = document.createElement("button");
            connectBtn.className = "btn btn-outline-secondary";
            connectBtn.textContent = "Cancel Request";
            connectBtn.onclick = async () => {
                connectBtn.disabled = true;
                connectBtn.textContent = "Canceling...";
                try {
                    const sentPage = await ConnectionApi.getSentRequests(0, 50);
                    const req = sentPage.content.find(r => r.receiverId === targetUserId);
                    if (req) {
                        await ConnectionApi.cancelConnectionRequest(req.requestId);
                        currentRelationship.connectionStatus = "NOT_CONNECTED";
                        renderRelationshipButtons();
                        loadStats();
                    } else {
                        alert("Request not found");
                    }
                } catch(e) { alert(e.message); }
                connectBtn.disabled = false;
            };
            relationshipActions.appendChild(connectBtn);
        } else if (currentRelationship.connectionStatus === "PENDING_RECEIVED") {
            const acceptBtn = document.createElement("button");
            acceptBtn.className = "btn btn-primary me-2";
            acceptBtn.textContent = "Accept Request";
            acceptBtn.onclick = async () => {
                acceptBtn.disabled = true;
                acceptBtn.textContent = "Accepting...";
                try {
                    const receivedPage = await ConnectionApi.getReceivedRequests(0, 50);
                    const req = receivedPage.content.find(r => r.requesterId === targetUserId);
                    if (req) {
                        await ConnectionApi.acceptConnectionRequest(req.requestId);
                        currentRelationship.connectionStatus = "CONNECTED";
                        renderRelationshipButtons();
                        loadStats();
                    } else {
                        alert("Request not found");
                    }
                } catch(e) { alert(e.message); }
                acceptBtn.disabled = false;
            };
            
            const rejectBtn = document.createElement("button");
            rejectBtn.className = "btn btn-outline-secondary";
            rejectBtn.textContent = "Reject Request";
            rejectBtn.onclick = async () => {
                rejectBtn.disabled = true;
                rejectBtn.textContent = "Rejecting...";
                try {
                    const receivedPage = await ConnectionApi.getReceivedRequests(0, 50);
                    const req = receivedPage.content.find(r => r.requesterId === targetUserId);
                    if (req) {
                        await ConnectionApi.rejectConnectionRequest(req.requestId);
                        currentRelationship.connectionStatus = "NOT_CONNECTED";
                        renderRelationshipButtons();
                        loadStats();
                    } else {
                        alert("Request not found");
                    }
                } catch(e) { alert(e.message); }
                rejectBtn.disabled = false;
            };

            relationshipActions.appendChild(acceptBtn);
            relationshipActions.appendChild(rejectBtn);
        } else {
            const connectBtn = document.createElement("button");
            connectBtn.className = "btn btn-primary";
            connectBtn.textContent = "Connect";
            connectBtn.onclick = async () => {
                connectBtn.disabled = true;
                connectBtn.textContent = "Connecting...";
                try {
                    await ConnectionApi.sendConnectionRequest(targetUserId);
                    currentRelationship.connectionStatus = "PENDING_SENT";
                    renderRelationshipButtons();
                    loadStats();
                } catch(e) { alert(e.message); connectBtn.disabled = false; }
            };
            relationshipActions.appendChild(connectBtn);
        }
        
        // Follow Button logic
        const followBtn = document.createElement("button");
        if (currentRelationship.following) {
            followBtn.className = "btn btn-outline-primary";
            followBtn.textContent = "Following";
            followBtn.onclick = async () => {
                followBtn.disabled = true;
                try {
                    await ConnectionApi.unfollowUser(targetUserId);
                    currentRelationship.following = false;
                    renderRelationshipButtons();
                    loadStats();
                } catch(e) { alert(e.message); followBtn.disabled = false; }
            };
        } else {
            followBtn.className = "btn btn-primary";
            followBtn.textContent = "Follow";
            followBtn.onclick = async () => {
                followBtn.disabled = true;
                followBtn.textContent = "Following...";
                try {
                    await ConnectionApi.followUser(targetUserId);
                    currentRelationship.following = true;
                    renderRelationshipButtons();
                    loadStats();
                } catch(e) { alert(e.message); followBtn.disabled = false; }
            };
        }
        
        relationshipActions.appendChild(followBtn);
    }

    // Modal List Renderer
    function openListModal(title, fetcher, renderer) {
        listModalTitle.textContent = title;
        listModalBody.innerHTML = "<p>Loading...</p>";
        listModal.style.display = "flex";
        
        fetcher().then(pageData => {
            listModalBody.innerHTML = "";
            if (!pageData.content || pageData.content.length === 0) {
                listModalBody.innerHTML = "<p class='text-muted text-center mt-3'>No users found.</p>";
                return;
            }
            pageData.content.forEach(item => {
                listModalBody.appendChild(renderer(item));
            });
        }).catch(e => {
            listModalBody.innerHTML = `<p class="text-danger">Failed to load: ${e.message}</p>`;
        });
    }

    function createUserRow(userId, username, actionHtml, actionCallback) {
        const div = document.createElement("div");
        div.className = "d-flex justify-content-between align-items-center border-bottom py-2";
        div.innerHTML = `
            <div class="d-flex align-items-center">
                <div class="profile-avatar bg-secondary text-white rounded-circle d-flex justify-content-center align-items-center me-2" style="width: 32px; height: 32px; cursor: pointer;">
                    ${username.charAt(0).toUpperCase()}
                </div>
                <strong style="cursor: pointer;">${username}</strong>
            </div>
            <div>${actionHtml}</div>
        `;
        const clickToProfile = () => window.location.href = `/profile.html?userId=${userId}`;
        div.querySelector(".profile-avatar").onclick = clickToProfile;
        div.querySelector("strong").onclick = clickToProfile;
        if (actionCallback) {
            const btnContainer = div.lastElementChild;
            btnContainer.addEventListener("click", (e) => {
                const btn = e.target.closest("button");
                if (btn) actionCallback(btn, userId);
            });
        }
        return div;
    }

    // Bind Stat Clicks
    document.getElementById("statFollowers").addEventListener("click", () => {
        if (!isMe) return;
        openListModal("Followers", () => ConnectionApi.getFollowers(0, 50), (f) => {
            return createUserRow(f.userId, f.username, "", null);
        });
    });

    document.getElementById("statFollowing").addEventListener("click", () => {
        if (!isMe) return;
        openListModal("Following", () => ConnectionApi.getFollowing(0, 50), (f) => {
            return createUserRow(f.userId, f.username, "", null);
        });
    });

    document.getElementById("statConnections").addEventListener("click", () => {
        if (!isMe) return;
        openListModal("Connections", () => ConnectionApi.getConnections(0, 50), (c) => {
            const btnHtml = isMe ? `<button class="btn btn-sm btn-outline-danger" data-action="remove">Remove</button>` : "";
            return createUserRow(c.userId, c.username, btnHtml, async (btn, uId) => {
                if (btn.dataset.action === "remove") {
                    btn.disabled = true;
                    try {
                        await ConnectionApi.removeConnection(uId);
                        btn.closest(".d-flex").remove();
                        loadStats();
                    } catch(e) { alert(e.message); btn.disabled = false; }
                }
            });
        });
    });

    statPending.addEventListener("click", () => {
        if (!isMe) return;
        openListModal("Pending Requests", () => ConnectionApi.getReceivedRequests(0, 50), (req) => {
            const btnHtml = `
                <button class="btn btn-sm btn-primary me-1" data-action="accept">Accept</button>
                <button class="btn btn-sm btn-outline-secondary" data-action="reject">Reject</button>
            `;
            return createUserRow(req.requesterId, req.requesterUsername, btnHtml, async (btn, uId) => {
                btn.disabled = true;
                try {
                    if (btn.dataset.action === "accept") {
                        await ConnectionApi.acceptConnectionRequest(req.requestId);
                    } else if (btn.dataset.action === "reject") {
                        await ConnectionApi.rejectConnectionRequest(req.requestId);
                    }
                    btn.closest(".d-flex").remove();
                    loadStats();
                } catch(e) { alert(e.message); btn.disabled = false; }
            });
        });
    });

    statSent.addEventListener("click", () => {
        if (!isMe) return;
        openListModal("Sent Requests", () => ConnectionApi.getSentRequests(0, 50), (req) => {
            const btnHtml = `<button class="btn btn-sm btn-outline-danger" data-action="cancel">Cancel</button>`;
            return createUserRow(req.receiverId, req.receiverUsername, btnHtml, async (btn, uId) => {
                btn.disabled = true;
                try {
                    if (btn.dataset.action === "cancel") {
                        await ConnectionApi.cancelConnectionRequest(req.requestId);
                    }
                    btn.closest(".d-flex").remove();
                    loadStats();
                } catch(e) { alert(e.message); btn.disabled = false; }
            });
        });
    });

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
            if (!response.ok) throw new Error("Failed to update profile");
            const updatedUser = await response.json();
            fullNameEl.textContent = updatedUser.fullName || "Add your full name";
            bioEl.textContent = updatedUser.bio || "Add a bio";
            editProfileForm.style.display = "none";
            editProfileBtn.style.display = "block";
            alert("Profile updated successfully!");
        } catch (error) {
            console.error(error);
            alert("Failed to update profile.");
        }
    });

    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("token");
        window.location.href = "/login.html";
    });

    initProfile();
});