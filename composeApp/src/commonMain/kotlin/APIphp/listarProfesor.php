<?php
// Encabezados
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

// Conexión a la base de datos
$con = new mysqli($host, $usuario, $clave, $bd);

// Verificar conexión
if ($con->connect_error) {
    http_response_code(500);
    echo json_encode(["error" => "Conexión fallida: " . $con->connect_error]);
    exit();
}

// 🔹 Cambia este número si el rol de profesor tiene otro id distinto
$idRolProfesor = 2;

// Consulta para listar solo profesores
$sql = "SELECT id_usuario, CONCAT(nombre, ' ', apellido) AS nombre_completo, correo 
        FROM usuarios 
        WHERE id_rol = $idRolProfesor";

$result = $con->query($sql);

$profesores = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $profesores[] = $row;
    }

    echo json_encode($profesores, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
} else {
    // Si no hay resultados, devolvemos un arreglo vacío
    echo json_encode([]);
}

$con->close();
?>
