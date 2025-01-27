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
('Richard', 'Im not God, but I have her on her knees', 'richard', '1234'),
('Betsaida', '¿No? ¿No?', 'betsaida', '1234'),
('Luca', 'Teta', 'luca', '1234'),
('Víctor', 'Me gusta el zumito', 'victor', '1234'),
('Saúl', ':D', 'saul', '1234'),
('Álvaro', 'Autobús', 'alvaro', '1234'),
('Óscar', 'Cogito ergo sum', 'oscar', '1234'),
('Javier', 'Disturbing the peaceeeee', 'javier', '1234'),
('Eliazar', 'Perdí la guagua profe', 'eliazar', '1234'),
('Lin', 'Me estoy sacando el B1 profe', 'lin', '1234'),
('Jazael', ':|', 'jazael', '1234'),
('Daniel', 'De autobuses no sé, de guaguas sí hay huelga', 'daniel', '1234'),
('Antonio', 'Antooooonio', 'antonio', '1234'),
('Enrique', ':|', 'enrique', '1234'),
('Ángel', 'Que fue jefa', 'angel', '1234'),
('Flavio', 'En verdad me llamo Fabio', 'flavio', '1234'),
('Edu', 'Delegado el que tengo aquí colgado', 'edu', '1234'),
('Luis', 'Van a suspender todos!!', 'luis', '1234');

INSERT INTO mensajes (mensaje, idDestinatario, idRemitente, fecha) VALUES
('Hola, ¿cómo estás?', 2, 1, '2025-01-14 10:00:00'),
('¡Hola! Estoy bien, ¿y tú?', 1, 2, '2025-01-14 10:05:00'),
('Todo bien, gracias. ¿Listo para la reunión?', 2, 1, '2025-01-14 10:10:00'),
('Sí, estoy revisando los últimos detalles.', 1, 2, '2025-01-14 10:15:00'),
('Perfecto, nos vemos a las 11.', 2, 1, '2025-01-14 10:20:00');