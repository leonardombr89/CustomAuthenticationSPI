import React from "react";
import { jwtDecode } from "jwt-decode";

function Profile() {
  const query = new URLSearchParams(window.location.search);
  const token = query.get("token");

  if (!token) return <p>Token não encontrado na URL.</p>;

  let decoded;
  try {
    decoded = jwtDecode(token);
  } catch (err) {
    return <p>Erro ao decodificar o token: {err.message}</p>;
  }

  return (
    <div style={{ padding: "2rem", fontFamily: "Arial" }}>
      <h2>Perfil do Usuário</h2>
      <p><strong>Email:</strong> {decoded.sub}</p>
      <p><strong>Username:</strong> {decoded.username}</p>
      <p><strong>Emitido em:</strong> {new Date(decoded.iat * 1000).toLocaleString()}</p>
      <p><strong>Expira em:</strong> {new Date(decoded.exp * 1000).toLocaleString()}</p>
      <pre style={{ marginTop: "1rem", background: "#f6f6f6", padding: "1rem" }}>
        {JSON.stringify(decoded, null, 2)}
      </pre>
    </div>
  );
}

export default Profile;
