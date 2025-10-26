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
// Consulta de usuarios
$sql = "SELECT nombre, apellido, correo, user, id_rol FROM usuarios";
$result = $con->query($sql);
$usuarios = [];
if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $usuarios[] = $row;
    }
    echo json_encode($usuarios, JSON_UNESCAPED_UNICODE |
        JSON_PRETTY_PRINT);
} else {
    echo json_encode([]);
}
$con->close();
?>