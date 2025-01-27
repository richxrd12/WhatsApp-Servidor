@echo off
if "%1"=="-f" (

    echo Levantando la base de datos por primera vez...

    docker build -t mysql-whatsapp .

    echo Creada la imagen, ahora el contenedor...

    docker run -d --name whatsapp -p 3306:3306 mysql-whatsapp

    echo Creado el contenedor, listo para usar :D

) else if "%1"=="-d" (

    echo Borrando el contenedor y la imagen...

    docker stop whatsapp

    docker rm whatsapp

    docker rmi mysql-whatsapp

    echo Se ha borrado el contenedor y la imagen :D

) else (
    echo Levantando el contenedor...

    docker start whatsapp

    echo Listo para usar :D
)

