const ConnectionApi = (function () {
    
    function getToken() {
        return localStorage.getItem("token");
    }

    async function apiRequest(endpoint, method = "GET", body = null) {
        const token = getToken();
        if (!token) {
            throw new Error("No authentication token found");
        }

        const headers = {
            "Authorization": `Bearer ${token}`
        };

        if (body) {
            headers["Content-Type"] = "application/json";
        }

        const options = {
            method,
            headers
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(endpoint, options);
        
        if (!response.ok) {
            let errorMessage = `HTTP error! status: ${response.status}`;
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            } catch (e) {
                // Ignore json parsing error if response is not json
            }
            const error = new Error(errorMessage);
            error.status = response.status;
            throw error;
        }

        if (response.status === 204) {
            return null;
        }

        return await response.json();
    }

    return {
        getRelationshipStatus: (targetUserId) => apiRequest(`/api/connections/status/${targetUserId}`),
        sendConnectionRequest: (targetUserId) => apiRequest(`/api/connections/requests/${targetUserId}`, "POST"),
        getReceivedRequests: (page = 0, size = 20) => apiRequest(`/api/connections/requests/received?page=${page}&size=${size}`),
        getSentRequests: (page = 0, size = 20) => apiRequest(`/api/connections/requests/sent?page=${page}&size=${size}`),
        acceptConnectionRequest: (requestId) => apiRequest(`/api/connections/requests/${requestId}/accept`, "PUT"),
        rejectConnectionRequest: (requestId) => apiRequest(`/api/connections/requests/${requestId}/reject`, "PUT"),
        cancelConnectionRequest: (requestId) => apiRequest(`/api/connections/requests/${requestId}`, "DELETE"),
        getConnections: (page = 0, size = 20) => apiRequest(`/api/connections?page=${page}&size=${size}`),
        removeConnection: (otherUserId) => apiRequest(`/api/connections/${otherUserId}`, "DELETE"),
        followUser: (targetUserId) => apiRequest(`/api/connections/follow/${targetUserId}`, "POST"),
        unfollowUser: (targetUserId) => apiRequest(`/api/connections/follow/${targetUserId}`, "DELETE"),
        getFollowers: (page = 0, size = 20) => apiRequest(`/api/connections/followers?page=${page}&size=${size}`),
        getFollowing: (page = 0, size = 20) => apiRequest(`/api/connections/following?page=${page}&size=${size}`),
        getConnectionStats: (targetUserId = null) => apiRequest(targetUserId ? `/api/connections/stats/${targetUserId}` : `/api/connections/stats`)
    };
})();
