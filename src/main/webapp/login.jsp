<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion – Réservation de Salles</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            min-height: 100vh;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .container {
            width: 100%;
            max-width: 420px;
            padding: 20px;
        }

        .logo {
            text-align: center;
            margin-bottom: 30px;
        }

        .logo h1 {
            color: #e94560;
            font-size: 1.8rem;
            letter-spacing: 2px;
        }

        .logo p {
            color: #a8b2d8;
            font-size: 0.85rem;
            margin-top: 5px;
        }

        .card {
            background: rgba(255,255,255,0.05);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 16px;
            padding: 40px 36px;
        }

        .tabs {
            display: flex;
            margin-bottom: 28px;
            background: rgba(0,0,0,0.2);
            border-radius: 8px;
            padding: 4px;
        }

        .tab {
            flex: 1;
            text-align: center;
            padding: 10px;
            color: #a8b2d8;
            cursor: pointer;
            border-radius: 6px;
            font-size: 0.9rem;
            transition: all 0.3s;
        }

        .tab.active {
            background: #e94560;
            color: white;
            font-weight: 600;
        }

        .form-group {
            margin-bottom: 18px;
        }

        label {
            display: block;
            color: #a8b2d8;
            font-size: 0.85rem;
            margin-bottom: 6px;
        }

        input, select {
            width: 100%;
            padding: 12px 14px;
            background: rgba(255,255,255,0.07);
            border: 1px solid rgba(255,255,255,0.15);
            border-radius: 8px;
            color: #e0e0e0;
            font-size: 0.95rem;
            outline: none;
            transition: border-color 0.3s;
        }

        input::placeholder { color: #555; }

        input:focus, select:focus {
            border-color: #e94560;
        }

        select option { background: #1a1a2e; }

        .btn {
            width: 100%;
            padding: 13px;
            background: #e94560;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            margin-top: 8px;
            transition: background 0.3s, transform 0.1s;
        }

        .btn:hover { background: #c73652; }
        .btn:active { transform: scale(0.98); }

        .alert {
            padding: 12px 14px;
            border-radius: 8px;
            margin-bottom: 18px;
            font-size: 0.9rem;
            display: none;
        }

        .alert.error { background: rgba(233,69,96,0.2); color: #e94560; border: 1px solid rgba(233,69,96,0.3); }
        .alert.success { background: rgba(76,175,80,0.2); color: #66bb6a; border: 1px solid rgba(76,175,80,0.3); }

        .row { display: flex; gap: 12px; }
        .row .form-group { flex: 1; }

        #registerForm { display: none; }
    </style>
</head>
<body>

<div class="container">
    <div class="logo">
        <h1>🏢 SalleBook</h1>
        <p>Système de réservation de salles de conférence</p>
    </div>

    <div class="card">
        <div class="tabs">
            <div class="tab active" onclick="showTab('login')">Connexion</div>
            <div class="tab" onclick="showTab('register')">Inscription</div>
        </div>

        <div id="alert" class="alert"></div>

        <!-- FORMULAIRE CONNEXION -->
        <div id="loginForm">
            <div class="form-group">
                <label>Nom d'utilisateur</label>
                <input type="text" id="loginUsername" placeholder="admin / gestionnaire / client1" />
            </div>
            <div class="form-group">
                <label>Mot de passe</label>
                <input type="password" id="loginPassword" placeholder="••••••••" />
            </div>
            <button class="btn" onclick="login()">Se connecter</button>
        </div>

        <!-- FORMULAIRE INSCRIPTION -->
        <div id="registerForm">
            <div class="row">
                <div class="form-group">
                    <label>Nom</label>
                    <input type="text" id="nom" placeholder="Dupont" />
                </div>
                <div class="form-group">
                    <label>Prénom</label>
                    <input type="text" id="prenom" placeholder="Jean" />
                </div>
            </div>
            <div class="form-group">
                <label>Nom d'utilisateur</label>
                <input type="text" id="regUsername" placeholder="jean.dupont" />
            </div>
            <div class="form-group">
                <label>Mot de passe</label>
                <input type="password" id="regPassword" placeholder="••••••••" />
            </div>
            <div class="form-group">
                <label>Entreprise</label>
                <input type="text" id="nomEntreprise" placeholder="Tech Corp" />
            </div>
            <div class="form-group">
                <label>Email</label>
                <input type="email" id="email" placeholder="jean@email.com" />
            </div>
            <div class="form-group">
                <label>Téléphone</label>
                <input type="tel" id="telephone" placeholder="0612345678" />
            </div>
            <button class="btn" onclick="register()">Créer mon compte</button>
        </div>
    </div>
</div>

<script>
    // NB: on utilise des URLs relatives (ex: "api/auth/login") pour que ça marche
    // quel que soit le context-root (ex: /SalleReservation).

    function showTab(tab) {
        document.querySelectorAll('.tab').forEach((t, i) => {
            t.classList.toggle('active', (tab === 'login' && i === 0) || (tab === 'register' && i === 1));
        });
        document.getElementById('loginForm').style.display = tab === 'login' ? 'block' : 'none';
        document.getElementById('registerForm').style.display = tab === 'register' ? 'block' : 'none';
        hideAlert();
    }

    function showAlert(message, type) {
        const alert = document.getElementById('alert');
        alert.textContent = message;
        alert.className = 'alert ' + type;
        alert.style.display = 'block';
    }

    function hideAlert() {
        document.getElementById('alert').style.display = 'none';
    }

    async function login() {
        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value.trim();

        if (!username || !password) {
            showAlert('Veuillez remplir tous les champs', 'error');
            return;
        }

        const formBody = new URLSearchParams();
        formBody.append('username', username);
        formBody.append('password', password);

        try {
            const res = await fetch('api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: formBody.toString()
            });
            const text = await res.text();
            let data = {};
            try { data = JSON.parse(text); } catch (_) { /* réponse non JSON */ }

            if (res.ok) {
                showAlert('Connexion réussie ! Rôle : ' + (data.role || '?'), 'success');
                // Redirection selon le rôle (adaptez selon votre structure)
                setTimeout(() => {
                    if (data.role === 'ADMIN') window.location.href = 'api/admin/salles';
                    else if (data.role === 'GESTIONNAIRE') window.location.href = 'api/gestionnaire/reservations';
                    else window.location.href = 'api/client/salles';
                }, 1000);
            } else {
                showAlert((data.error || text || 'Identifiants incorrects') + ` (HTTP ${res.status})`, 'error');
            }
        } catch (e) {
            showAlert('Erreur de connexion au serveur: ' + (e && e.message ? e.message : e), 'error');
        }
    }

    async function register() {
        const data = {
            username: document.getElementById('regUsername').value.trim(),
            password: document.getElementById('regPassword').value.trim(),
            nom: document.getElementById('nom').value.trim(),
            prenom: document.getElementById('prenom').value.trim(),
            nomEntreprise: document.getElementById('nomEntreprise').value.trim(),
            email: document.getElementById('email').value.trim(),
            telephone: document.getElementById('telephone').value.trim()
        };

        if (!data.username || !data.password || !data.nom || !data.prenom || !data.email || !data.telephone) {
            showAlert('Tous les champs obligatoires doivent être remplis', 'error');
            return;
        }

        const formBody = new URLSearchParams();
        Object.entries(data).forEach(([k, v]) => formBody.append(k, v));

        try {
            const res = await fetch('api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: formBody.toString()
            });
            const text = await res.text();
            let result = {};
            try { result = JSON.parse(text); } catch (_) { /* réponse non JSON */ }

            if (res.ok) {
                showAlert('Compte créé avec succès ! Vous pouvez vous connecter.', 'success');
                setTimeout(() => showTab('login'), 1500);
            } else {
                showAlert((result.error || text || 'Erreur lors de la création du compte') + ` (HTTP ${res.status})`, 'error');
            }
        } catch (e) {
            showAlert('Erreur de connexion au serveur: ' + (e && e.message ? e.message : e), 'error');
        }
    }

    // Login avec touche Entrée
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            if (document.getElementById('registerForm').style.display === 'block') register();
            else login();
        }
    });
</script>

</body>
</html>
