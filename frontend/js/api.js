const API_BASE = "http://localhost:8080/api";
export async function request(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {}),
        },
        ...options,
    });
    const body = await response
        .json()
        .catch(() => ({ success: false, message: "Invalid server response" }));
    if (!response.ok || !body.success)
        throw new Error(body.message || "Request failed");
    return body;
}
