<?php
    
	$link = mysqli_connect("localhost", "ws", "ws", "chat");
	
	if($link === false){
        die("ERROR: No pudo conectarse con la DB. " . mysqli_connect_error());
    }
    // RECIBE LOS DATOS DE LA APP
	$correo = $_POST['correo'];
	$usuario = $_POST['usuario'];
	$url = $_POST['url'];
	$contraseña = $_POST['contraseña'];

    // VERIFICAMOS QUE NO ESTEN VACIAS LAS VARIABLES
    if( empty($correo) && empty($url) && empty($usuario) && empty($contraseña)) {

        // SI ALGUNA VARIABLE ESTA VACIA MUESTRA ERROR
      
        echo "ERROR 1";

    } else {

        // CREAMOS LA CONSULTA
        $sql = "INSERT into perfil (correo,url,usuario,contraseña) VALUES ('$correo','$url','$usuario','$contraseña')";
       

        // CREAMOS UN ARRAY PARA GUARDAR LOS VALORES DEL REGISTRO
       // $data = array();
		
		 if (mysqli_query($link,$sql) === TRUE) {
			echo "OK";      
			}else {
			echo "ERROR";
			}
		
	}
     
    

?>