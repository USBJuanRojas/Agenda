<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

$data = json_decode(file_get_contents("php://input"), true);

$id_clase = $data["id_clase"] ?? null;
$dias_semana = $data["dias_semana"] ?? "";

if (!$id_clase) {
    echo json_encode(["success" => false, "message" => "ID de clase no proporcionado"]);
    exit;
}

$con = new mysqli($host, $usuario, $clave, $bd);
if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

// Verificar si ya existe
$stmt = $con->prepare("SELECT id_horario FROM horario_clase WHERE id_clase = ?");
$stmt->bind_param("i", $id_clase);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    // Actualizar
    $stmt = $con->prepare("UPDATE horario_clase SET dias_semana = ? WHERE id_clase = ?");
    $stmt->bind_param("si", $dias_semana, $id_clase);
    $success = $stmt->execute();
    $message = $success ? "Horario actualizado" : "Error al actualizar";
} else {
    // Insertar nuevo
    $stmt = $con->prepare("INSERT INTO horario_clase (id_clase, dias_semana) VALUES (?, ?)");
    $stmt->bind_param("is", $id_clase, $dias_semana);
    $success = $stmt->execute();
    $message = $success ? "Horario creado" : "Error al crear";
}

echo json_encode(["success" => $success, "message" => $message]);
$stmt->close();
$con->close();
?>
