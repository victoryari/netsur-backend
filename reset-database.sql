-- Reset completo de la base de datos
DROP DATABASE IF EXISTS ticket_app;
CREATE DATABASE ticket_app;
USE ticket_app;

-- Tabla de usuarios
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    role ENUM('admin', 'agent', 'user') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de tickets
CREATE TABLE tickets (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('open', 'in_progress', 'resolved', 'closed') DEFAULT 'open',
    priority ENUM('low', 'medium', 'high', 'urgent') DEFAULT 'medium',
    category VARCHAR(255),
    created_by VARCHAR(36),
    assigned_to VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de comentarios
CREATE TABLE comments (
    id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(36),
    author_id VARCHAR(36),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de sesiones
CREATE TABLE sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de notificaciones (avisos de ticket nuevo y de ticket asignado)
CREATE TABLE notifications (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    body VARCHAR(400) NOT NULL,
    ticket_id CHAR(36),
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_notif_user (user_id, is_read, created_at)
);

-- Usuarios de prueba.
-- Las contrasenas se guardan como SHA-256 en hexadecimal, que es lo que
-- calcula la app antes de enviarlas. Si cambias una, recalcula el hash:
--   SHA-256 de 'Admin123'   -> 3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2
INSERT INTO users (id, email, password_hash, display_name, role) VALUES
(UUID(), 'admin@ticketapp.com',   '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'Administrador', 'admin'),
(UUID(), 'tecnico@ticketapp.com', '9aa3c98ffbadb9247e2be2182ed6cabd991ad32053fc1d154d8c542a444e03a7', 'Técnico Demo', 'agent'),
(UUID(), 'usuario@ticketapp.com', '66d4fca6f91a71a033d2369ad7a302bf83b925364cb73bcc9677e378c4685437', 'Usuario Demo', 'user');

-- Verificación
SELECT 'Base de datos creada correctamente' AS status;
SHOW TABLES;
SELECT id, email, display_name, role FROM users;
