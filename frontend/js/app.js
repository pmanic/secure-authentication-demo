import { request } from "./api.js";
const csrfTokens = { secure: null };
const setResult = (side, message, ok = true) => {
    const out = side.querySelector(".result");
    out.textContent = message;
    out.className = `result ${ok ? "success" : "error"}`;
};
const setLoading = (button, loading) => {
    if (!button.dataset.label) button.dataset.label = button.textContent;
    button.disabled = loading;
    button.textContent = loading ? "PLEASE WAIT…" : button.dataset.label;
};

document.querySelectorAll(".global-switcher button").forEach((button) =>
    button.addEventListener("click", () => {
        document
            .querySelectorAll(".global-switcher button")
            .forEach((item) =>
                item.classList.toggle("active", item === button),
            );
        document
            .querySelectorAll(".auth-form")
            .forEach((form) =>
                form.classList.toggle(
                    "hidden",
                    !form.classList.contains(button.dataset.view),
                ),
            );
    }),
);

document.querySelectorAll(".side").forEach((side) => {
    const kind = side.dataset.side;
    side.querySelectorAll(".auth-form").forEach((form) =>
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const button = form.querySelector("button");
            setLoading(button, true);
            try {
                const payload = Object.fromEntries(new FormData(form));
                const action = form.classList.contains("login")
                    ? "login"
                    : "register";
                const response = await request(`/${kind}/${action}`, {
                    method: "POST",
                    body: JSON.stringify(payload),
                });
                setResult(side, response.message);
                if (action === "login") {
                    const data =
                        kind === "secure"
                            ? response.data.profile
                            : response.data;
                    if (kind === "secure")
                        csrfTokens.secure = response.data.csrfToken;
                    showProfile(side, data, kind);
                }
            } catch (error) {
                setResult(side, error.message, false);
            } finally {
                setLoading(button, false);
            }
        }),
    );
    side.querySelector(".save-message").addEventListener("click", () =>
        mutate(side, kind, "message", {
            message: side.querySelector(".message").value,
        }),
    );
    side.querySelector(".change-email").addEventListener("click", () =>
        mutate(side, kind, "email", {
            email: side.querySelector(".new-email").value,
        }),
    );
});
async function mutate(side, kind, action, payload) {
    try {
        const headers =
            kind === "secure" ? { "X-CSRF-Token": csrfTokens.secure } : {};
        const r = await request(`/${kind}/profile/${action}`, {
            method: "POST",
            headers,
            body: JSON.stringify(payload),
        });
        setResult(side, r.message);
        if (action === "message") renderMessage(side, payload.message, kind);
    } catch (e) {
        setResult(side, e.message, false);
    }
}
function showProfile(side, data, kind) {
    side.querySelector(".dashboard").classList.remove("hidden");
    side.querySelector(".identity").textContent =
        `${data.displayName} (${data.email})`;
    side.querySelector(".message").value = data.message || "";
    renderMessage(side, data.message || "", kind);
}
function renderMessage(side, value, kind) {
    const output = side.querySelector(".message-output");
    if (kind === "unsafe") output.innerHTML = value;
    else output.textContent = value;
}
