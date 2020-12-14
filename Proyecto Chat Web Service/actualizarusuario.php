<?php 
	$link = mysqli_connect("localhost", "ws", "ws", "chat");
	
	if($link === false){
        die("ERROR");
    }
    // RECIBE LOS DATOS DE LA APP
	$correo = $_POST['correo'];
	$usuario = $_POST['usuario'];
	$url = $_POST['url'];
	$contrasena = $_POST['contrasena'];

    // VERIFICAMOS QUE NO ESTEN VACIAS LAS VARIABLES
    if( empty($correo) && empty($usuario)) {

        // SI ALGUNA VARIABLE ESTA VACIA MUESTRA ERROR
      
        echo "ERROR 1";

    } else {

        // CREAMOS LA CONSULTA
        $sql = "UPDATE perfil SET usuario='$usuario', url='$url', contraseña='$contrasena' WHERE correo='$correo'";
       

        // CREAMOS UN ARRAY PARA GUARDAR LOS VALORES DEL REGISTRO
       // $data = array();
		
		 if (mysqli_query($link,$sql) === TRUE) {
			echo "OK";      
			}else {
			echo "ERROR 2";
			}
		
	}
     
        
?>