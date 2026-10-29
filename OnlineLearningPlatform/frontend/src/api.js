const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function request(path, options = {}) {
  const method = options.method || "GET";
  const headers = new Headers(options.headers || {});
  const requestOptions = {
    ...options,
    method,
    headers,
    credentials: "include",
  };

  if (!["GET", "HEAD", "OPTIONS"].includes(method.toUpperCase())) {
    const csrfResponse = await fetch(`${API_BASE_URL}/api/auth/csrf`, {
      credentials: "include",
      signal: options.signal,
    });
    if (!csrfResponse.ok) {
      throw new Error("Could not initialize a secure request. Please try again.");
    }
    const { token } = await csrfResponse.json();
    headers.set("X-XSRF-TOKEN", token);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, requestOptions);
  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json")
    ? await response.json()
    : null;

  if (!response.ok) {
    const error = new Error(
      data?.message || data?.detail || `Request failed (${response.status}).`,
    );
    error.status = response.status;
    throw error;
  }
  return data;
}

export { API_BASE_URL, request };
