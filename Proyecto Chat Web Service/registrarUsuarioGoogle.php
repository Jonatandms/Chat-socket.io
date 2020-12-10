<?php
    
	$link = mysqli_connect("localhost", "ws", "ws", "chat");
	
	if($link === false){
        die("ERROR 1");
    }
    // RECIBE LOS DATOS DE LA APP
	$correo = $_POST['correo'];
	$usuario = $_POST['usuario'];
	$url = $_POST['url'];

    // VERIFICAMOS QUE NO ESTEN VACIAS LAS VARIABLES
    if( empty($correo) && empty($url) && empty($usuario) && empty($contraseña)) {

        // SI ALGUNA VARIABLE ESTA VACIA MUESTRA ERROR
      
        echo "ERROR 2";

    } else {

        // CREAMOS LA CONSULTA
        $sql = "INSERT into perfil (correo,url,usuario) VALUES ('$correo','$url','$usuario')";
       

        // CREAMOS UN ARRAY PARA GUARDAR LOS VALORES DEL REGISTRO
       // $data = array();
		
		 if (mysqli_query($link,$sql) === TRUE) {
			echo "OK";      
			}else {
			echo "ERROR";
			}
		
	}
     
    

?>