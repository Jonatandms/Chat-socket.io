<?php
    
    // Variables de la conexion a la DB
    $mysqli = new mysqli("localhost","proyecto","proyecto","chat");
    
    // Comprobamos la conexion
    if($mysqli->connect_errno) {
        die("Fallo la conexion");
    } else {
        //echo "Conexion exitosa";
    }
    
    ?>
