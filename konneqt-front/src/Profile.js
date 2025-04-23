import React from "react";
import { jwtDecode } from "jwt-decode";
import konneqtLogo from "./assets/logo1.png";

function Profile() {
  const query = new URLSearchParams(window.location.search);
  const token = query.get("token");

  if (!token) {
    return (
      <div style={styles.container}>
        <img src={konneqtLogo} alt="Konneqt Logo" style={styles.logo} />
        <p style={styles.error}>❌ Token not found in URL.</p>
      </div>
    );
  }

  let decoded;
  try {
    decoded = jwtDecode(token);
  } catch (err) {
    return (
      <div style={styles.container}>
        <img src={konneqtLogo} alt="Logo" style={styles.logo} />
        <p style={styles.error}>❌ Error decoding token: {err.message}</p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <img src={konneqtLogo} alt="Logo" style={styles.logo} />
      <h2 style={styles.heading}>User Profile</h2>

      <div style={styles.card}>
        <p><strong>Email:</strong> {decoded.sub}</p>
        <p><strong>Username:</strong> {decoded.username}</p>
        <p><strong>Issued at:</strong> {new Date(decoded.iat * 1000).toLocaleString()}</p>
        <p><strong>Expires at:</strong> {new Date(decoded.exp * 1000).toLocaleString()}</p>
      </div>

      <div style={styles.tokenBox}>
        <p style={{ marginBottom: "0.5rem" }}><strong>Token Data:</strong></p>
        <pre style={styles.token}>
          {JSON.stringify(decoded, null, 2)}
        </pre>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    backgroundColor: "#0e1117",
    color: "#fff",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    padding: "2rem",
    fontFamily: "'Segoe UI', sans-serif",
  },
  logo: {
    width: "160px",
    marginBottom: "2rem",
  },
  heading: {
    fontSize: "2rem",
    marginBottom: "1.5rem",
  },
  card: {
    backgroundColor: "#1f2937",
    padding: "1.5rem",
    borderRadius: "10px",
    width: "100%",
    maxWidth: "600px",
    marginBottom: "2rem",
    boxShadow: "0 4px 10px rgba(0,0,0,0.3)",
  },
  tokenBox: {
    width: "100%",
    maxWidth: "600px",
  },
  token: {
    backgroundColor: "#111827",
    padding: "1rem",
    borderRadius: "6px",
    overflowX: "auto",
    color: "#93c5fd",
  },
  error: {
    color: "#f87171",
    fontWeight: "bold",
    fontSize: "1.2rem",
  },
};

export default Profile;
