<?php
   $conn = new mysqli("localhost", "ws", "ws", "chat");
// Check connection
if ($conn->connect_error) {
  echo "ERROR 1" ;
}

    // RECIBE LOS DATOS DE LA APP
	$usuario = $_POST['usuario'];

    // VERIFICAMOS QUE NO ESTEN VACIAS LAS VARIABLES
    if(empty($usuario)) {

        // SI ALGUNA VARIABLE ESTA VACIA MUESTRA ERROR
        //echo "Se deben llenar los dos campos";
        echo "ERROR 2";

    } else {

        // CREAMOS LA CONSULTA
        $sql = "SELECT idusuario FROM perfil WHERE usuario = '$usuario'" ;;
        $result = $conn->query($sql);

		if ($result->num_rows > 0) {
			// output data of each row
		while($row = $result->fetch_assoc()) {
			echo $row["idusuario"];
		}
	} else {
	echo "0 results";
}

	}
   
?>