import React, { useState } from "react";
import jwtEncode from "jwt-encode";
import konneqtLogo from "./assets/logo1.png";

const SECRET = "super-strong-key-that-is-very-secure!";
const BACKEND_URL = "http://localhost:8080/realms/test/authenticator/konneqt";

function App() {
  const [email, setEmail] = useState("");
  const [token, setToken] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const generateToken = (email) => {
    const iat = Math.floor(Date.now() / 1000);
    const exp = iat + 20 * 60;
    const payload = {
      iss: "Online JWT Builder",
      sub: email,
      aud: "",
      iat,
      exp,
    };
    return jwtEncode(payload, SECRET, "HS256");
  };

  const handleLogin = async () => {
    setError("");
    setLoading(true);
    const customToken = generateToken(email);
    setToken(customToken);

    const start = Date.now();

    try {
      const response = await fetch(BACKEND_URL, {
        method: "POST",
        headers: {
          "X-Konneqt-Token": customToken,
        },
      });

      const redirectUrl = response.headers.get("X-Konneqt-Redirect");
      const elapsed = Date.now() - start;
      const delay = Math.max(2000 - elapsed, 0); // mínimo de 2s

      setTimeout(async () => {
        setLoading(false);
        if (redirectUrl) {
          window.location.href = redirectUrl;
        } else {
          const body = await response.json().catch(() => null);
          if (body?.redirect) {
            window.location.href = body.redirect;
          } else {
            const text = await response.text();
            setError(`Erro ${response.status}: ${text}`);
          }
        }
      }, delay);
    } catch (err) {
      setLoading(false);
      setError("Erro na requisição: " + err.message);
    }
  };

  return (
    <div style={{
      minHeight: "100vh",
      backgroundColor: "#0e1117",
      color: "#fff",
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      fontFamily: "'Segoe UI', sans-serif",
      flexDirection: "column",
      padding: "2rem"
    }}>
      <img src={konneqtLogo} alt="Logo" style={{ width: "35vw", maxWidth: "250px", marginBottom: "2rem" }} />

      <h2 style={{ fontSize: "2rem", marginBottom: "1rem" }}>Secure Login with SPI</h2>

      <div style={{ display: "flex", gap: "1rem", marginBottom: "1rem" }}>
        <input
          type="email"
          placeholder="Enter your email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          style={{
            padding: "0.7rem 1rem",
            borderRadius: "6px",
            border: "1px solid #333",
            background: "#1c1f26",
            color: "#fff",
            width: "300px"
          }}
        />
        <button
          onClick={handleLogin}
          disabled={loading}
          style={{
            background: "linear-gradient(135deg, #3b82f6, #6366f1)",
            color: "#fff",
            padding: "0.7rem 1.5rem",
            border: "none",
            borderRadius: "6px",
            cursor: loading ? "not-allowed" : "pointer",
            fontWeight: "600",
            opacity: loading ? 0.6 : 1
          }}
        >
          {loading ? "Autenticando..." : "Login"}
        </button>
      </div>

      {loading && (
        <div style={{ marginTop: "1rem" }}>
          <div style={{
            border: "4px solid #1f2937",
            borderTop: "4px solid #3b82f6",
            borderRadius: "50%",
            width: "32px",
            height: "32px",
            animation: "spin 1s linear infinite",
            margin: "0 auto"
          }} />
          <style>
            {`@keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }`}
          </style>
        </div>
      )}

      {token && !loading && (
        <div style={{
          background: "#1f2937",
          padding: "1rem",
          borderRadius: "8px",
          marginTop: "1rem",
          width: "100%",
          maxWidth: "600px"
        }}>
          <p><strong>Generated Token:</strong></p>
          <textarea
            readOnly
            value={token}
            rows={4}
            style={{
              width: "100%",
              background: "#111827",
              border: "none",
              color: "#fff",
              padding: "0.5rem",
              borderRadius: "4px",
              fontFamily: "monospace"
            }}
          />
        </div>
      )}

      {error && !loading && (
        <p style={{ color: "#f87171", marginTop: "1rem" }}>{error}</p>
      )}
    </div>
  );
}

export default App;
