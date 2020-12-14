<?php
   $conn = new mysqli("localhost", "ws", "ws", "chat");
// Check connection
if ($conn->connect_error) {
  echo "ERROR 1"; 
}

        // CREAMOS LA CONSULTA
        $sql = "SELECT url, usuario FROM perfil";
        $result = $conn->query($sql);

		if ($result->num_rows > 0) {
			// output data of each row
		while($row = $result->fetch_assoc()) {
			echo $row["url"].";".$row["usuario"]."|";
		}
	} else {
	echo "0 results";
}


	
    
?>