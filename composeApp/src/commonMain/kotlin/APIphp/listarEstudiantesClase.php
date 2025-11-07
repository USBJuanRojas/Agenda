<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    http_response_code(500);
    echo json_encode(["error" => "Conexión fallida: " . $con->connect_error]);
    exit();
}

if (!isset($_GET["id_clase"])) {
    http_response_code(400);
    echo json_encode(["error" => "Falta el parámetro id_clase"]);
    exit();
}

$id_clase = intval($_GET["id_clase"]);

$sql = "SELECT u.id_usuario, u.nombre, u.apellido, u.correo, u.user, u.id_rol
        FROM gestor_clases g
        INNER JOIN usuarios u ON g.id_usuario = u.id_usuario
        WHERE g.id_clase = ?";

$stmt = $con->prepare($sql);
$stmt->bind_param("i", $id_clase);
$stmt->execute();
$result = $stmt->get_result();

$estudiantes = [];
while ($row = $result->fetch_assoc()) {
    $estudiantes[] = $row;
}

echo json_encode($estudiantes, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

$stmt->close();
$con->close();
?>
