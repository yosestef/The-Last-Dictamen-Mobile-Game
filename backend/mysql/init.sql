-- Crear las 3 bases de datos del proyecto
CREATE DATABASE IF NOT EXISTS codeslasher CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS razarun CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS codemerge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Otorgar permisos al usuario de la app
GRANT ALL PRIVILEGES ON codeslasher.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON razarun.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON codemerge.* TO 'user'@'%';
CREATE DATABASE IF NOT EXISTS lastdictamen CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON lastdictamen.* TO 'user'@'%';
FLUSH PRIVILEGES;
