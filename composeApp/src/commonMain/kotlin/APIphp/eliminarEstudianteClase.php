<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Conexión fallida"]);
    exit();
}

$id_usuario = $_POST["id_usuario"] ?? null;
$id_clase = $_POST["id_clase"] ?? null;

if (!$id_usuario || !$id_clase) {
    echo json_encode(["status" => "error", "message" => "Faltan parámetros"]);
    exit();
}

$stmt = $con->prepare("DELETE FROM gestor_clases WHERE id_usuario = ? AND id_clase = ?");
$stmt->bind_param("ii", $id_usuario, $id_clase);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Estudiante eliminado de la clase"]);
} else {
    echo json_encode(["status" => "error", "message" => "No se pudo eliminar al estudiante"]);
}

$stmt->close();
$con->close();
?>
