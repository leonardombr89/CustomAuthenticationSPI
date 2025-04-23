import React, { useState } from "react";
import jwtEncode from "jwt-encode";

const SECRET = "super-strong-key-that-is-very-secure!";
const BACKEND_URL = "http://localhost:8080/realms/test/authenticator/konneqt";

function App() {
  const [email, setEmail] = useState("");
  const [token, setToken] = useState("");
  const [error, setError] = useState("");

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
    const customToken = generateToken(email);
    setToken(customToken);

    try {
      const response = await fetch(BACKEND_URL, {
        method: "POST",
        headers: {
          "X-Konneqt-Token": customToken,
        },
      });

      const redirectUrl = response.headers.get("X-Konneqt-Redirect");

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
    } catch (err) {
      setError("Erro na requisição: " + err.message);
    }
  };

  return (
    <div style={{ padding: "2rem", fontFamily: "Arial" }}>
      <h2>Login via X-Konneqt-Token</h2>
      <input
        type="email"
        placeholder="Digite seu e-mail"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        style={{ padding: "0.5rem", width: "300px" }}
      />
      <button
        onClick={handleLogin}
        style={{ marginLeft: "1rem", padding: "0.5rem 1rem" }}
      >
        Entrar
      </button>

      {token && (
        <div style={{ marginTop: "1rem" }}>
          <p><strong>Token gerado:</strong></p>
          <textarea readOnly value={token} rows={3} style={{ width: "100%" }} />
        </div>
      )}

      {error && (
        <p style={{ color: "red", marginTop: "1rem" }}>{error}</p>
      )}
    </div>
  );
}

export default App;
