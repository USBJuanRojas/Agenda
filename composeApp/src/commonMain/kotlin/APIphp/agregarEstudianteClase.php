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

// Evitar duplicados
$check = $con->prepare("SELECT id_gestor FROM gestor_clases WHERE id_usuario = ? AND id_clase = ?");
$check->bind_param("ii", $id_usuario, $id_clase);
$check->execute();
$result = $check->get_result();

if ($result->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "El estudiante ya está en esta clase"]);
    exit();
}

$stmt = $con->prepare("INSERT INTO gestor_clases (id_usuario, id_clase) VALUES (?, ?)");
$stmt->bind_param("ii", $id_usuario, $id_clase);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Estudiante agregado correctamente"]);
} else {
    echo json_encode(["status" => "error", "message" => "No se pudo agregar al estudiante"]);
}

$stmt->close();
$con->close();
?>
