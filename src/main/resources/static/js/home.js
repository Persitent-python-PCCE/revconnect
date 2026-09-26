document.addEventListener("DOMContentLoaded", async () => {

    console.log("RevConnect home.js initialized");

    // =====================================================
    // 1. AUTHENTICATION CHECK
    // =====================================================

    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/login.html";
        return;
    }


    // =====================================================
    // JWT EXPIRY HELPERS
    // =====================================================

    function isTokenExpired(jwt) {

        try {

            const base64Url = jwt.split(".")[1];

            const base64 = base64Url
                .replace(/-/g, "+")
                .replace(/_/g, "/");

            const payload = JSON.parse(
                atob(base64)
            );

            return payload.exp * 1000 < Date.now();

        } catch (e) {

            return false;
        }
    }


    function handleSessionExpired() {

        localStorage.removeItem("token");

        const overlay = document.createElement("div");

        overlay.style.cssText = [
            "position:fixed",
            "inset:0",
            "z-index:99999",
            "background:rgba(0,0,0,0.75)",
            "backdrop-filter:blur(6px)",
            "display:flex",
            "align-items:center",
            "justify-content:center"
        ].join(";");

        overlay.innerHTML = `
            <div style="
                background:#1a1a2e;
                border:1px solid rgba(255,255,255,0.12);
                border-radius:16px;
                padding:40px 48px;
                text-align:center;
                max-width:360px;
                width:90%;
                box-shadow:0 24px 64px rgba(0,0,0,0.6);
            ">

                <div style="
                    font-size:2.5rem;
                    margin-bottom:16px;
                ">
                    🔒
                </div>

                <h2 style="
                    color:#fff;
                    font-size:1.25rem;
                    margin:0 0 10px;
                    font-family:inherit;
                ">
                    Session Expired
                </h2>

                <p style="
                    color:rgba(255,255,255,0.6);
                    font-size:0.9rem;
                    margin:0 0 28px;
                    line-height:1.5;
                ">
                    Your session has timed out.<br>
                    Please log in again to continue.
                </p>

                <button
                    onclick="window.location.href='/login.html'"
                    style="
                        background:linear-gradient(
                            135deg,
                            #ff6b35,
                            #f7c59f
                        );
                        color:#fff;
                        border:none;
                        border-radius:8px;
                        padding:12px 32px;
                        font-size:0.95rem;
                        font-weight:600;
                        cursor:pointer;
                        width:100%;
                        transition:opacity .2s;
                    "
                    onmouseover="this.style.opacity='.85'"
                    onmouseout="this.style.opacity='1'"
                >
                    Log In Again
                </button>

            </div>
        `;

        document.body.appendChild(overlay);
    }


    if (isTokenExpired(token)) {

        handleSessionExpired();
        return;
    }


    // =====================================================
    // 2. STATE VARIABLES
    // =====================================================

    let loggedInUser = null;
    let selectedPhotoFile = null;


    // =====================================================
    // 3. DOM ELEMENTS
    // =====================================================

    // Profile Elements

    const profileUsername =
        document.getElementById("profileUsername");

    const profileAccountType =
        document.getElementById("profileAccountType");

    const sidebarAvatar =
        document.querySelector(".sidebar-avatar");

    const quickCreateAvatar =
        document.getElementById("quickCreateAvatar");

    const logoutBtn =
        document.getElementById("logoutBtn");


    // Modal Triggers

    const navCreateBtn =
        document.getElementById("navCreateBtn");

    const headerAddPhotoBtn =
        document.getElementById("headerAddPhotoBtn");

    const quickCreateTrigger =
        document.getElementById("quickCreateTrigger");

    const quickCreatePhotoBtn =
        document.getElementById("quickCreatePhotoBtn");


    // Modal & Dialog

    const createPostModal =
        document.getElementById("createPostModal");

    const modalDialog =
        document.getElementById("igModalDialog");

    const modalCloseBtn =
        document.getElementById("modalCloseBtn");

    const modalBackBtn =
        document.getElementById("modalBackBtn");

    const modalTitle =
        document.getElementById("modalTitle");

    const modalHeaderShareBtn =
        document.getElementById("modalHeaderShareBtn");


    // Step 1: Upload / Dropzone

    const modalSelectStep =
        document.getElementById("modalSelectStep");

    const igDropzone =
        document.getElementById("igDropzone");

    const modalSelectFileBtn =
        document.getElementById("modalSelectFileBtn");

    const modalFileInput =
        document.getElementById("modalFileInput");


    // Step 2: Split View

    const modalDetailsStep =
        document.getElementById("modalDetailsStep");

    const modalPreviewImg =
        document.getElementById("modalPreviewImg");

    const modalChangePhotoBtn =
        document.getElementById("modalChangePhotoBtn");

    const modalUserAvatar =
        document.getElementById("modalUserAvatar");

    const modalUsername =
        document.getElementById("modalUsername");

    const modalAccountType =
        document.getElementById("modalAccountType");

    const modalCaptionText =
        document.getElementById("modalCaptionText");

    const modalCharCounter =
        document.getElementById("modalCharCounter");

    const modalShareSubmitBtn =
        document.getElementById("modalShareSubmitBtn");

    const modalStatusAlert =
        document.getElementById("modalStatusAlert");

    const quickEmojis =
        document.querySelectorAll(".ig-quick-emoji");


    // Feed

    const feedGrid =
        document.getElementById("feedGrid");

    const feedLoadMoreBtn =
        document.getElementById("feedLoadMoreBtn");


    let feedPage = 0;

    const FEED_PAGE_SIZE = 10;

    let feedLoading = false;

    let feedHasMore = true;


    // =====================================================
    // 4. LOAD CURRENT USER PROFILE
    // =====================================================

    try {

        const response = await fetch(
            "/api/user/me",
            {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }
        );


        if (
            response.status === 401
            || response.status === 403
        ) {

            handleSessionExpired();
            return;
        }


        if (response.ok) {

            loggedInUser =
                await response.json();


            const initial =
                (loggedInUser.username || "U")
                    .charAt(0)
                    .toUpperCase();


            if (profileUsername) {

                profileUsername.textContent =
                    loggedInUser.username;
            }


            if (profileAccountType) {

                profileAccountType.textContent =
                    loggedInUser.accountType;
            }


            if (sidebarAvatar) {

                sidebarAvatar.textContent =
                    initial;
            }


            if (quickCreateAvatar) {

                quickCreateAvatar.textContent =
                    initial;
            }


            if (modalUserAvatar) {

                modalUserAvatar.textContent =
                    initial;
            }


            if (modalUsername) {

                modalUsername.textContent =
                    loggedInUser.username;
            }


            if (modalAccountType) {

                modalAccountType.textContent =
                    loggedInUser.accountType;
            }
        }

    } catch (err) {

        console.error(
            "Failed to load user profile:",
            err
        );
    }


    // =====================================================
    // 5. LOAD FEED POSTS
// GET /api/feed?page=0&size=10
// =====================================================

    async function loadFeedPosts(reset = true) {

        if (feedLoading) {
            return;
        }


        if (!reset && !feedHasMore) {
            return;
        }


        feedLoading = true;


        if (reset) {

            feedPage = 0;

            feedHasMore = true;


            if (feedGrid) {

                feedGrid.innerHTML = "";
            }
        }


        if (feedLoadMoreBtn) {

            feedLoadMoreBtn.disabled = true;

            feedLoadMoreBtn.textContent =
                "Loading...";
        }


        try {

            const response = await fetch(
                `/api/feed?page=${feedPage}&size=${FEED_PAGE_SIZE}`,
                {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`
                    }
                }
            );


            if (!response.ok) {

                console.warn(
                    "Could not load feed posts: HTTP",
                    response.status
                );

                return;
            }


            const data =
                await response.json();


            console.log(
                "Fetched feed page:",
                data
            );


            const posts =
                data.content || [];


            if (
                posts.length > 0
                && feedGrid
            ) {

                posts.forEach(post => {

                    renderPostCard(
                        post,
                        loggedInUser,
                        false
                    );
                });


                if (window.lucide) {

                    lucide.createIcons();
                }

            } else if (
                reset
                && feedGrid
            ) {

                feedGrid.innerHTML = `
                    <div class="feed-empty-state">
                        <p>No posts yet.</p>
                    </div>
                `;
            }


            feedHasMore =
                !data.last;


            feedPage++;


            if (feedLoadMoreBtn) {

                feedLoadMoreBtn.style.display =
                    feedHasMore
                        ? "inline-block"
                        : "none";

                feedLoadMoreBtn.disabled =
                    false;

                feedLoadMoreBtn.textContent =
                    "Load more";
            }

        } catch (error) {

            console.error(
                "Error loading feed posts:",
                error
            );


            if (feedLoadMoreBtn) {

                feedLoadMoreBtn.disabled =
                    false;

                feedLoadMoreBtn.textContent =
                    "Load more";
            }

        } finally {

            feedLoading = false;
        }
    }


    // Load first feed page

    loadFeedPosts();


    // Load more button

    if (feedLoadMoreBtn) {

        feedLoadMoreBtn.addEventListener(
            "click",
            () => {
                loadFeedPosts(false);
            }
        );
    }


    // =====================================================
    // 6. INSTAGRAM-STYLE MODAL CONTROL FLOW
    // =====================================================

    function openModal() {

        resetModal();

        createPostModal.style.display =
            "flex";

        document.body.style.overflow =
            "hidden";


        if (window.lucide) {

            lucide.createIcons();
        }
    }


    function closeModal() {

        createPostModal.style.display =
            "none";

        document.body.style.overflow =
            "";

        resetModal();
    }


    function resetModal() {

        selectedPhotoFile = null;


        if (modalFileInput) {

            modalFileInput.value =
                "";
        }


        if (modalCaptionText) {

            modalCaptionText.value =
                "";
        }


        if (modalPreviewImg) {

            modalPreviewImg.src =
                "";
        }


        if (modalCharCounter) {

            modalCharCounter.textContent =
                "0/2,200";
        }


        if (modalStatusAlert) {

            modalStatusAlert.style.display =
                "none";

            modalStatusAlert.textContent =
                "";

            modalStatusAlert.className =
                "ig-status-alert";
        }


        modalSelectStep.style.display =
            "flex";

        modalDetailsStep.style.display =
            "none";

        modalBackBtn.style.display =
            "none";

        modalHeaderShareBtn.style.display =
            "none";

        modalTitle.textContent =
            "Create new post";


        setShareLoading(false);
    }


    function goToStep2(file) {

        selectedPhotoFile = file;


        const reader =
            new FileReader();


        reader.onload = (e) => {

            modalPreviewImg.src =
                e.target.result;

            modalSelectStep.style.display =
                "none";

            modalDetailsStep.style.display =
                "flex";

            modalBackBtn.style.display =
                "flex";

            modalHeaderShareBtn.style.display =
                "block";

            modalTitle.textContent =
                "Create new post";


            if (modalCaptionText) {

                modalCaptionText.focus();
            }


            if (window.lucide) {

                lucide.createIcons();
            }
        };


        reader.readAsDataURL(file);
    }


    const MAX_FILE_SIZE_MB = 10;

    const MAX_FILE_SIZE_BYTES =
        MAX_FILE_SIZE_MB * 1024 * 1024;


    function handleFileSelection(file) {

        if (
            !file
            || !file.type.startsWith("image/")
        ) {

            alert(
                "Please select a valid image file (JPG, PNG, WEBP, etc.)"
            );

            return;
        }


        if (
            file.size
            > MAX_FILE_SIZE_BYTES
        ) {

            alert(
                `Image is too large (${(
                    file.size / 1024 / 1024
                ).toFixed(1)} MB). Please choose an image under ${MAX_FILE_SIZE_MB} MB.`
            );

            return;
        }


        goToStep2(file);
    }


    // Modal Trigger Listeners

    if (navCreateBtn) {

        navCreateBtn.addEventListener(
            "click",
            openModal
        );
    }


    if (headerAddPhotoBtn) {

        headerAddPhotoBtn.addEventListener(
            "click",
            openModal
        );
    }


    if (quickCreateTrigger) {

        quickCreateTrigger.addEventListener(
            "click",
            openModal
        );
    }


    if (quickCreatePhotoBtn) {

        quickCreatePhotoBtn.addEventListener(
            "click",
            (e) => {

                e.stopPropagation();

                openModal();
            }
        );
    }


    // Close Listeners

    if (modalCloseBtn) {

        modalCloseBtn.addEventListener(
            "click",
            closeModal
        );
    }


    createPostModal.addEventListener(
        "click",
        (e) => {

            if (
                e.target === createPostModal
            ) {

                closeModal();
            }
        }
    );


    document.addEventListener(
        "keydown",
        (e) => {

            if (
                e.key === "Escape"
                && createPostModal.style.display !== "none"
            ) {

                closeModal();
            }
        }
    );


    // Back to Photo Selection

    if (modalBackBtn) {

        modalBackBtn.addEventListener(
            "click",
            () => {

                modalSelectStep.style.display =
                    "flex";

                modalDetailsStep.style.display =
                    "none";

                modalBackBtn.style.display =
                    "none";

                modalHeaderShareBtn.style.display =
                    "none";

                modalTitle.textContent =
                    "Create new post";
            }
        );
    }


    // File Selector

    if (modalSelectFileBtn) {

        modalSelectFileBtn.addEventListener(
            "click",
            () => modalFileInput.click()
        );
    }


    if (modalChangePhotoBtn) {

        modalChangePhotoBtn.addEventListener(
            "click",
            () => modalFileInput.click()
        );
    }


    if (modalFileInput) {

        modalFileInput.addEventListener(
            "change",
            (e) => {

                if (
                    e.target.files
                    && e.target.files[0]
                ) {

                    handleFileSelection(
                        e.target.files[0]
                    );
                }
            }
        );
    }


    // Drag and Drop

    if (igDropzone) {

        [
            "dragenter",
            "dragover"
        ].forEach(eventName => {

            igDropzone.addEventListener(
                eventName,
                (e) => {

                    e.preventDefault();
                    e.stopPropagation();

                    igDropzone.classList.add(
                        "dragover"
                    );
                },
                false
            );
        });


        [
            "dragleave",
            "drop"
        ].forEach(eventName => {

            igDropzone.addEventListener(
                eventName,
                (e) => {

                    e.preventDefault();
                    e.stopPropagation();

                    igDropzone.classList.remove(
                        "dragover"
                    );
                },
                false
            );
        });


        igDropzone.addEventListener(
            "drop",
            (e) => {

                const dt =
                    e.dataTransfer;


                if (
                    dt
                    && dt.files
                    && dt.files[0]
                ) {

                    handleFileSelection(
                        dt.files[0]
                    );
                }
            }
        );
    }


    // Caption character counter

    if (modalCaptionText) {

        modalCaptionText.addEventListener(
            "input",
            () => {

                const length =
                    modalCaptionText.value.length;


                modalCharCounter.textContent =
                    `${length.toLocaleString()}/2,200`;
            }
        );
    }


    // Quick emojis

    quickEmojis.forEach(
        emojiBtn => {

            emojiBtn.addEventListener(
                "click",
                () => {

                    const emoji =
                        emojiBtn.getAttribute(
                            "data-emoji"
                        );


                    if (
                        emoji
                        && modalCaptionText
                    ) {

                        modalCaptionText.value +=
                            emoji;

                        modalCaptionText.dispatchEvent(
                            new Event("input")
                        );

                        modalCaptionText.focus();
                    }
                }
            );
        }
    );


    // =====================================================
    // 7. SHARE POST SUBMISSION
    // POST /api/posts
    // =====================================================

    function setShareLoading(loading) {

        const btns = [
            modalShareSubmitBtn,
            modalHeaderShareBtn
        ];


        btns.forEach(btn => {

            if (!btn) {
                return;
            }


            btn.disabled =
                loading;


            const text =
                btn.querySelector(
                    ".btn-text"
                );


            const spinner =
                btn.querySelector(
                    ".btn-spinner"
                );


            if (text) {

                text.style.display =
                    loading
                        ? "none"
                        : "inline";
            }


            if (spinner) {

                spinner.style.display =
                    loading
                        ? "inline-block"
                        : "none";
            }


            if (
                btn ===
                modalHeaderShareBtn
            ) {

                btn.textContent =
                    loading
                        ? "Sharing..."
                        : "Share";
            }
        });
    }


    function showModalStatus(
        message,
        isError
    ) {

        if (!modalStatusAlert) {
            return;
        }


        modalStatusAlert.textContent =
            message;


        modalStatusAlert.className =
            "ig-status-alert "
            + (
                isError
                    ? "error"
                    : "success"
            );


        modalStatusAlert.style.display =
            "block";
    }


    async function submitPost() {

        const caption =
            modalCaptionText
                ? modalCaptionText.value.trim()
                : "";


        if (!caption) {

            showModalStatus(
                "Please enter a caption for your post.",
                true
            );


            if (modalCaptionText) {

                modalCaptionText.focus();
            }


            return;
        }


        // Client-side size guard

        if (
            selectedPhotoFile
            && selectedPhotoFile.size
            > MAX_FILE_SIZE_BYTES
        ) {

            showModalStatus(
                `Image too large (${(
                    selectedPhotoFile.size
                    / 1024
                    / 1024
                ).toFixed(1)} MB). Max allowed is ${MAX_FILE_SIZE_MB} MB.`,
                true
            );


            return;
        }


        setShareLoading(true);


        if (modalStatusAlert) {

            modalStatusAlert.style.display =
                "none";
        }


        try {

            const formData =
                new FormData();


            if (selectedPhotoFile) {

                formData.append(
                    "photo",
                    selectedPhotoFile
                );
            }


            formData.append(
                "caption",
                caption
            );


            console.log(
                "Submitting post with photo:",
                !!selectedPhotoFile,
                "caption:",
                caption
            );


            const response =
                await fetch(
                    "/api/posts",
                    {
                        method: "POST",
                        headers: {
                            "Authorization":
                                `Bearer ${token}`
                        },
                        body: formData
                    }
                );


            if (
                response.status === 401
                || response.status === 403
            ) {

                handleSessionExpired();
                return;
            }


            if (
                response.status === 413
            ) {

                showModalStatus(
                    `Image is too large. Please use an image under ${MAX_FILE_SIZE_MB} MB.`,
                    true
                );


                setShareLoading(false);

                return;
            }


            if (!response.ok) {

                const errorText =
                    await response.text();


                throw new Error(
                    errorText
                    || "Failed to publish post"
                );
            }


            const newPost =
                await response.json();


            console.log(
                "Post published successfully:",
                newPost
            );


            showModalStatus(
                "Post published successfully!",
                false
            );


            // Backend now provides the actual post username.

            renderPostCard(
                newPost,
                loggedInUser,
                true
            );


            if (window.lucide) {

                lucide.createIcons();
            }


            setTimeout(
                () => {

                    closeModal();
                },
                600
            );

        } catch (error) {

            console.error(
                "Post creation error:",
                error
            );


            showModalStatus(
                error.message
                || "Failed to create post. Please try again.",
                true
            );

        } finally {

            setShareLoading(false);
        }
    }


    if (modalShareSubmitBtn) {

        modalShareSubmitBtn.addEventListener(
            "click",
            submitPost
        );
    }


    if (modalHeaderShareBtn) {

        modalHeaderShareBtn.addEventListener(
            "click",
            submitPost
        );
    }


    // =====================================================
    // 8. RENDER POST CARD IN FEED
    // =====================================================

    function renderPostCard(
        post,
        user,
        prepend = true
    ) {

        if (!feedGrid) {
            return;
        }


        const card =
            document.createElement("article");


        card.className =
            "feed-card";


        card.setAttribute(
            "data-post-id",
            post.id || ""
        );


        // =================================================
        // IMPORTANT:
        // Use the username belonging to THIS post.
        // Do not automatically use the logged-in user.
        // =================================================

        let authorUsername = null;


        if (post.username) {

            authorUsername =
                post.username;

        } else if (
            user
            && user.username
            && user.id
            && post.userId
            && Number(user.id) === Number(post.userId)
        ) {

            authorUsername =
                user.username;

        } else {

            authorUsername =
                `user_${post.userId}`;
        }


        const usernameText =
            `@${authorUsername}`;


        const avatarInitial =
            authorUsername
                .charAt(0)
                .toUpperCase();


        const captionText =
            post.caption || "";


        const photoUrl =
            post.photoUrl
            || "/uploads/default.jpg";


        card.innerHTML = `

            <div class="post-photo-wrapper">

                <img
                    src="${photoUrl}"
                    alt="Post image"
                    class="post-photo-img"
                    onerror="this.src='/uploads/default.jpg'"
                >

            </div>


            <div class="card-caption-text">

                <strong>
                    ${escapeHtml(usernameText)}
                </strong>

                ${escapeHtml(captionText)}

            </div>


            <div class="card-info">

                <div class="card-user">

                    <div class="mini-avatar">
                        ${escapeHtml(avatarInitial)}
                    </div>

                    <span>
                        ${escapeHtml(usernameText)}
                    </span>

                </div>


                <div class="card-actions">

                    <span
                        class="action-btn"
                        title="Like"
                    >

                        <i data-lucide="heart"></i>

                        <span>
                            0
                        </span>

                    </span>


                    <span
                        class="action-btn"
                        title="Comment"
                    >

                        <i data-lucide="message-circle"></i>

                        <span>
                            0
                        </span>

                    </span>


                    <span
                        class="action-btn"
                        title="Share"
                    >

                        <i data-lucide="share-2"></i>

                    </span>

                </div>

            </div>

        `;


        if (prepend) {

            feedGrid.prepend(card);

        } else {

            feedGrid.appendChild(card);
        }
    }


    // =====================================================
    // 9. ESCAPE HTML
    // =====================================================

    function escapeHtml(text) {

        const div =
            document.createElement("div");


        div.textContent =
            text;


        return div.innerHTML;
    }


    // =====================================================
    // 10. LOGOUT HANDLER
    // =====================================================

    if (logoutBtn) {

        logoutBtn.addEventListener(
            "click",
            () => {

                localStorage.removeItem(
                    "token"
                );

                window.location.href =
                    "/login.html";
            }
        );
    }

    // --- Search Functionality ---
    const searchBoxContainer = document.querySelector(".search-box");
    const searchInput = document.querySelector(".search-box input");
    
    if (searchBoxContainer && searchInput) {
        searchBoxContainer.style.position = "relative";
        
        const dropdown = document.createElement("div");
        dropdown.style.cssText = `
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: #1a1a2e;
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 8px;
            margin-top: 8px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.4);
            z-index: 1000;
            display: none;
            max-height: 300px;
            overflow-y: auto;
        `;
        searchBoxContainer.appendChild(dropdown);
        
        let debounceTimer;
        let myUserId = null;
        
        // Fetch my user ID so we can filter ourselves out
        fetch("/api/user/me", { headers: { "Authorization": `Bearer ${token}` } })
            .then(res => {
                if (res.ok) return res.json();
            })
            .then(me => {
                if (me) myUserId = me.userId || me.id;
            })
            .catch(err => console.error("Error fetching me for search filter", err));
        
        searchInput.addEventListener("input", (e) => {
            clearTimeout(debounceTimer);
            const query = e.target.value.trim();
            
            if (!query) {
                dropdown.style.display = "none";
                return;
            }
            
            dropdown.style.display = "block";
            dropdown.innerHTML = `<div style="padding: 12px; color: rgba(255,255,255,0.6); text-align: center;">Searching...</div>`;
            
            debounceTimer = setTimeout(async () => {
                try {
                    const response = await fetch(`/api/user/search?q=${encodeURIComponent(query)}`, {
                        headers: { "Authorization": `Bearer ${token}` }
                    });
                    
                    if (!response.ok) {
                        throw new Error("API error");
                    }
                    
                    const users = await response.json();
                    dropdown.innerHTML = "";
                    
                    // Filter out my own profile and ensure we have results
                    const filteredUsers = users.filter(u => u.userId !== myUserId);
                    
                    if (filteredUsers.length === 0) {
                        dropdown.innerHTML = `<div style="padding: 12px; color: rgba(255,255,255,0.6); text-align: center;">No users found</div>`;
                        return;
                    }
                    
                    filteredUsers.forEach(u => {
                        const row = document.createElement("div");
                        row.style.cssText = `
                            display: flex;
                            align-items: center;
                            padding: 12px;
                            cursor: pointer;
                            border-bottom: 1px solid rgba(255,255,255,0.05);
                            transition: background 0.2s;
                        `;
                        row.onmouseover = () => row.style.background = "rgba(255,255,255,0.05)";
                        row.onmouseout = () => row.style.background = "transparent";
                        
                        // Use actual userId from backend, which avoids undefined resulting in NaN
                        row.onclick = () => {
                            window.location.href = `/profile.html?userId=${u.userId}`;
                        };
                        
                        const avatarLetter = (u.username || "U").charAt(0).toUpperCase();
                        
                        row.innerHTML = `
                            <div style="width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, #ff6b35, #f7c59f); color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 12px; flex-shrink: 0;">
                                ${avatarLetter}
                            </div>
                            <div style="display: flex; flex-direction: column; overflow: hidden;">
                                <strong style="color: #fff; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; font-size: 0.95rem;">${u.fullName || u.username}</strong>
                                <span style="color: rgba(255,255,255,0.5); font-size: 0.85rem;">@${u.username}</span>
                            </div>
                        `;
                        dropdown.appendChild(row);
                    });
                    
                } catch(err) {
                    console.error("Search failed", err);
                    dropdown.innerHTML = `<div style="padding: 12px; color: #ff6b35; text-align: center;">Error loading results</div>`;
                }
            }, 300);
        });
        
        document.addEventListener("click", (e) => {
            if (!searchBoxContainer.contains(e.target)) {
                dropdown.style.display = "none";
            }
        });
        
        searchInput.addEventListener("focus", () => {
            if (searchInput.value.trim()) {
                dropdown.style.display = "block";
            }
        });
    }
});