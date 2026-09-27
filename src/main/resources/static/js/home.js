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

    // =====================================================
    // 7. DYNAMIC SIDEBAR STATS
    // =====================================================
    async function loadSidebarStats() {
        try {
            const response = await fetch("/api/connections/stats", {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (response.ok) {
                const stats = await response.json();
                const followersEl = document.getElementById("sidebarFollowersCount");
                const followingEl = document.getElementById("sidebarFollowingCount");
                if (followersEl) followersEl.textContent = stats.followerCount || 0;
                if (followingEl) followingEl.textContent = stats.followingCount || 0;
            }
        } catch(e) {
            console.error("Failed to load sidebar stats", e);
        }
    }
    
    // Call it immediately
    loadSidebarStats();

    // =====================================================
    // 8. NOTIFICATIONS
    // =====================================================
    const navNotifBtn = document.getElementById("navNotificationsBtn");
    const headerNotifBtn = document.getElementById("headerNotificationsBtn");
    const navNotifBadge = document.getElementById("navNotificationsBadge");
    const headerNotifBadge = document.getElementById("headerNotificationsBadge");

    async function loadUnreadNotificationCount() {
        try {
            const response = await fetch("/api/notifications/unread-count", {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (response.ok) {
                const data = await response.json();
                const count = data.count || 0;
                
                if (count > 0) {
                    if (navNotifBadge) {
                        navNotifBadge.style.display = "block";
                        navNotifBadge.textContent = count > 99 ? "99+" : count;
                    }
                    if (headerNotifBadge) {
                        headerNotifBadge.style.display = "block";
                        headerNotifBadge.textContent = count > 99 ? "99+" : count;
                    }
                } else {
                    if (navNotifBadge) navNotifBadge.style.display = "none";
                    if (headerNotifBadge) headerNotifBadge.style.display = "none";
                }
            }
        } catch(e) {
            console.error("Failed to load notification count", e);
        }
    }

    loadUnreadNotificationCount();
    // Poll every 30 seconds for unread counts
    setInterval(loadUnreadNotificationCount, 30000);

    // Create notifications modal
    const notifModal = document.createElement("div");
    notifModal.style.cssText = `
        position: fixed;
        inset: 0;
        z-index: 99999;
        background: rgba(17, 24, 39, 0.4);
        backdrop-filter: blur(4px);
        display: none;
        align-items: flex-end;
        justify-content: flex-end;
        transition: all 0.3s ease;
    `;
    
    notifModal.innerHTML = `
        <div style="background: #FFFFFF; border-left: 1px solid #E5E7EB; width: 420px; max-width: 100%; height: 100vh; box-shadow: -10px 0 40px rgba(0,0,0,0.1); display: flex; flex-direction: column; animation: slideInRight 0.3s cubic-bezier(0.4, 0, 0.2, 1);">
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 24px; border-bottom: 1px solid #E5E7EB; background: #FFFFFF;">
                <h2 style="color: #111827; font-size: 1.25rem; font-weight: 800; margin: 0;">Notifications</h2>
                <div style="display: flex; gap: 12px;">
                    <button id="markAllReadBtn" style="background: none; border: none; color: #FF5A5F; cursor: pointer; font-size: 0.85rem; font-weight: 700; transition: all 0.2s;">Mark all read</button>
                    <button id="closeNotifModalBtn" style="background: #F3F4F6; border: none; color: #6B7280; width: 32px; height: 32px; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s;"><i data-lucide="x" style="width: 18px; height: 18px;"></i></button>
                </div>
            </div>
            <div id="notifListContainer" style="overflow-y: auto; flex: 1; padding: 20px 24px; background: #F9FAFB;">
                <div style="color: #6B7280; text-align: center; padding: 20px;">Loading...</div>
            </div>
        </div>
        <style>
            @keyframes slideInRight {
                from { transform: translateX(100%); }
                to { transform: translateX(0); }
            }
            #closeNotifModalBtn:hover { background: #E5E7EB; color: #111827; }
            #markAllReadBtn:hover { opacity: 0.8; }
        </style>
    `;
    document.body.appendChild(notifModal);

    const closeNotifModalBtn = notifModal.querySelector("#closeNotifModalBtn");
    const notifListContainer = notifModal.querySelector("#notifListContainer");
    const markAllReadBtn = notifModal.querySelector("#markAllReadBtn");

    closeNotifModalBtn.addEventListener("click", () => {
        notifModal.style.display = "none";
    });

    notifModal.addEventListener("click", (e) => {
        if (e.target === notifModal) {
            notifModal.style.display = "none";
        }
    });
    
    markAllReadBtn.addEventListener("click", async () => {
        try {
            await fetch("/api/notifications/read-all", {
                method: "PUT",
                headers: { "Authorization": `Bearer ${token}` }
            });
            loadUnreadNotificationCount();
            openNotifications();
        } catch(e) {
            console.error(e);
        }
    });

    async function openNotifications() {
        notifModal.style.display = "flex";
        if (window.lucide) lucide.createIcons();
        notifListContainer.innerHTML = `<div style="color: #6B7280; text-align: center; padding: 40px 20px; font-weight: 500;">
            <div style="width: 24px; height: 24px; border: 2px solid #E5E7EB; border-top-color: #FF5A5F; border-radius: 50%; animation: igSpin 0.7s linear infinite; margin: 0 auto 12px;"></div>
            Loading notifications...
        </div>`;
        
        try {
            const response = await fetch("/api/notifications", {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!response.ok) throw new Error("Failed to load");
            
            const pageData = await response.json();
            notifListContainer.innerHTML = "";
            
            if (!pageData.content || pageData.content.length === 0) {
                notifListContainer.innerHTML = `<div style="color: #6B7280; text-align: center; padding: 40px 20px; font-weight: 500;">
                    <i data-lucide="bell-off" style="width: 32px; height: 32px; color: #D1D5DB; margin-bottom: 12px;"></i><br>
                    No notifications yet.
                </div>`;
                if (window.lucide) lucide.createIcons();
                return;
            }
            
            pageData.content.forEach(n => {
                const item = document.createElement("div");
                item.style.cssText = `
                    display: flex;
                    padding: 16px;
                    border-bottom: 1px solid #E5E7EB;
                    align-items: flex-start;
                    background: ${n.read ? 'transparent' : '#FFFFFF'};
                    border-radius: 12px;
                    margin-bottom: 8px;
                    transition: all 0.2s ease;
                    cursor: pointer;
                    box-shadow: ${n.read ? 'none' : '0 1px 3px rgba(0,0,0,0.05)'};
                `;
                
                item.onmouseover = () => { item.style.transform = "translateY(-2px)"; item.style.boxShadow = "0 4px 12px rgba(0,0,0,0.05)"; };
                item.onmouseout = () => { item.style.transform = "none"; item.style.boxShadow = n.read ? "none" : "0 1px 3px rgba(0,0,0,0.05)"; };
                
                const avatarLetter = (n.actorUsername || "U").charAt(0).toUpperCase();
                
                let actionsHtml = "";
                if (n.type === "CONNECTION_REQUEST") {
                    actionsHtml = `
                        <div style="margin-top: 10px; display: flex; gap: 8px;">
                            <button class="accept-btn" data-id="${n.referenceId}" data-notif-id="${n.id}" style="background: #FF5A5F; color: white; border: none; padding: 8px 20px; border-radius: 8px; cursor: pointer; font-size: 0.85rem; font-weight: 600; transition: all 0.2s; box-shadow: 0 2px 4px rgba(255,90,95,0.2);">Accept</button>
                            <button class="reject-btn" data-id="${n.referenceId}" data-notif-id="${n.id}" style="background: #F3F4F6; color: #374151; border: 1px solid #E5E7EB; padding: 8px 20px; border-radius: 8px; cursor: pointer; font-size: 0.85rem; font-weight: 600; transition: all 0.2s;">Reject</button>
                        </div>
                    `;
                }
                
                item.innerHTML = `
                    <div style="width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, #FF5A5F, #FF8E53); color: white; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.1rem; margin-right: 14px; flex-shrink: 0;" onclick="window.location.href='/profile.html?userId=${n.actorId}'">
                        ${avatarLetter}
                    </div>
                    <div style="flex: 1;">
                        <div style="color: #111827; font-size: 0.95rem; line-height: 1.4;">
                            <strong>${n.actorName || n.actorUsername}</strong> ${n.message.replace(n.actorName || n.actorUsername, "")}
                        </div>
                        <div style="color: #6B7280; font-size: 0.8rem; margin-top: 6px; font-weight: 500;">
                            ${new Date(n.createdAt).toLocaleDateString()}
                        </div>
                        ${actionsHtml}
                    </div>
                    ${!n.read ? '<div class="unread-dot" style="width: 10px; height: 10px; border-radius: 50%; background: #FF5A5F; margin-left: 12px; margin-top: 6px; box-shadow: 0 0 0 4px rgba(255,90,95,0.1);"></div>' : ''}
                `;
                
                if (n.type === "CONNECTION_REQUEST") {
                    const acceptBtn = item.querySelector(".accept-btn");
                    const rejectBtn = item.querySelector(".reject-btn");
                    if (acceptBtn) {
                        acceptBtn.addEventListener("click", async (e) => {
                            e.stopPropagation();
                            acceptBtn.disabled = true;
                            rejectBtn.disabled = true;
                            try {
                                await fetch(`/api/connections/requests/${n.referenceId}/accept`, {
                                    method: "PUT",
                                    headers: { "Authorization": `Bearer ${token}` }
                                });
                                item.style.opacity = '0.5';
                                acceptBtn.textContent = "Accepted";
                                rejectBtn.style.display = "none";
                                loadSidebarStats(); // Refresh stats immediately
                            } catch(err) {
                                alert("Failed to accept");
                                acceptBtn.disabled = false;
                                rejectBtn.disabled = false;
                            }
                        });
                    }
                    if (rejectBtn) {
                        rejectBtn.addEventListener("click", async (e) => {
                            e.stopPropagation();
                            acceptBtn.disabled = true;
                            rejectBtn.disabled = true;
                            try {
                                await fetch(`/api/connections/requests/${n.referenceId}/reject`, {
                                    method: "PUT",
                                    headers: { "Authorization": `Bearer ${token}` }
                                });
                                item.style.display = "none";
                            } catch(err) {
                                alert("Failed to reject");
                                acceptBtn.disabled = false;
                                rejectBtn.disabled = false;
                            }
                        });
                    }
                }
                
                // Mark as read when clicked
                item.addEventListener("click", async () => {
                    if (!n.read) {
                        n.read = true;
                        item.style.background = 'transparent';
                        item.style.boxShadow = 'none';
                        const dot = item.querySelector(".unread-dot");
                        if (dot) dot.remove();
                        loadUnreadNotificationCount();
                        fetch(`/api/notifications/${n.id}/read`, {
                            method: "PUT",
                            headers: { "Authorization": `Bearer ${token}` }
                        });
                    }
                });
                
                notifListContainer.appendChild(item);
            });
            
        } catch(e) {
            console.error(e);
            notifListContainer.innerHTML = `<div style="color: #EF4444; text-align: center; padding: 40px 20px; font-weight: 500; background: #FEF2F2; border-radius: 12px; border: 1px solid #FCA5A5;">
                <i data-lucide="alert-circle" style="width: 32px; height: 32px; margin-bottom: 12px;"></i><br>
                Failed to load notifications
            </div>`;
            if (window.lucide) lucide.createIcons();
        }
    }

    if (navNotifBtn) navNotifBtn.addEventListener("click", (e) => { e.preventDefault(); openNotifications(); });
    if (headerNotifBtn) headerNotifBtn.addEventListener("click", (e) => { e.preventDefault(); openNotifications(); });

});