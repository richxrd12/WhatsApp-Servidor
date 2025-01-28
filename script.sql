USE whatsapp;

CREATE TABLE usuarios (
	id int auto_increment primary key,
	nombre varchar(50),
	estado varchar(255),
	usuario varchar(255),
	password varchar(255)
);

CREATE TABLE mensajes (
	id int auto_increment primary key,
	mensaje varchar(255),
	idDestinatario int,
	idRemitente int,
	fecha datetime,
	foreign key (idDestinatario) references usuarios(id),
	foreign key (idRemitente) references usuarios(id)
);

CREATE TABLE sesiones (
	idSesion int auto_increment primary key,
	idUsuario int,
	foreign key (idUsuario) references usuarios(id)
);

INSERT INTO usuarios (nombre, estado, usuario, password) VALUES
('Richard', 'Im not God, but I have her on her knees', 'richard', '81dc9bdb52d04dc20036dbd8313ed055'),
('Betsaida', '¿No? ¿No?', 'betsaida', '81dc9bdb52d04dc20036dbd8313ed055'),
('Luca', 'Teta', 'luca', '81dc9bdb52d04dc20036dbd8313ed055'),
('Víctor', 'Me gusta el zumito', 'victor', '81dc9bdb52d04dc20036dbd8313ed055'),
('Saúl', ':D', 'saul', '81dc9bdb52d04dc20036dbd8313ed055'),
('Álvaro', 'Autobús', 'alvaro', '81dc9bdb52d04dc20036dbd8313ed055'),
('Óscar', 'Cogito ergo sum', 'oscar', '81dc9bdb52d04dc20036dbd8313ed055'),
('Javier', 'Disturbing the peaceeeee', 'javier', '81dc9bdb52d04dc20036dbd8313ed055'),
('Eliazar', 'Perdí la guagua profe', 'eliazar', '81dc9bdb52d04dc20036dbd8313ed055'),
('Lin', 'Me estoy sacando el B1 profe', 'lin', '81dc9bdb52d04dc20036dbd8313ed055'),
('Jazael', ':|', 'jazael', '81dc9bdb52d04dc20036dbd8313ed055'),
('Daniel', 'De autobuses no sé, de guaguas sí hay huelga', 'daniel', '81dc9bdb52d04dc20036dbd8313ed055'),
('Antonio', 'Antooooonio', 'antonio', '81dc9bdb52d04dc20036dbd8313ed055'),
('Enrique', ':|', 'enrique', '81dc9bdb52d04dc20036dbd8313ed055'),
('Ángel', 'Que fue jefa', 'angel', '81dc9bdb52d04dc20036dbd8313ed055'),
('Flavio', 'En verdad me llamo Fabio', 'flavio', '81dc9bdb52d04dc20036dbd8313ed055'),
('Edu', 'Delegado el que tengo aquí colgado', 'edu', '81dc9bdb52d04dc20036dbd8313ed055'),
('Luis', 'Van a suspender todos!!', 'luis', '81dc9bdb52d04dc20036dbd8313ed055');

INSERT INTO mensajes (mensaje, idDestinatario, idRemitente, fecha) VALUES
('Hola, ¿cómo estás?', 2, 1, '2025-01-14 10:00:00'),
('¡Hola! Estoy bien, ¿y tú?', 1, 2, '2025-01-14 10:05:00'),
('Todo bien, gracias. ¿Listo para la reunión?', 2, 1, '2025-01-14 10:10:00'),
('Sí, estoy revisando los últimos detalles.', 1, 2, '2025-01-14 10:15:00'),
('Perfecto, nos vemos a las 11.', 2, 1, '2025-01-14 10:20:00');