<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Seu Código de Verificação 🎉</title>
    <style>
        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #ff9a9e, #fad0c4);
            padding: 20px;
            text-align: center;
        }
        .container {
            max-width: 600px;
            background: #ffffff;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
            text-align: center;
            margin: auto;
        }
        .title {
            font-size: 26px;
            font-weight: bold;
            color: #ff4081;
        }
        .message {
            font-size: 18px;
            color: #333;
            margin: 15px 0;
        }
        .pin {
            font-size: 28px;
            font-weight: bold;
            color: #ffffff;
            background: #ff4081;
            padding: 15px;
            display: inline-block;
            border-radius: 8px;
            letter-spacing: 5px;
        }
        .footer {
            margin-top: 20px;
            font-size: 14px;
            color: #555;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="title">Hey, ${user.firstName}! 🎨</div>
    <p class="message">Aqui está seu código secreto para continuar:</p>
    <div class="pin">${pin}</div>
    <p class="message">Este código expira em <strong>${expiration_time}</strong> minutos! ⏳</p>
    <p class="message">Se não foi você que solicitou, pode ignorar este e-mail! 😉</p>
    <div class="footer">
        <p>Com carinho,</p>
        <p><strong>Equipe Topzera 🚀</strong></p>
    </div>
</div>
</body>
</html>
