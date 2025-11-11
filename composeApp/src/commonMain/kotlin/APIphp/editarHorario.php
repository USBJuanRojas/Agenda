<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

$con = new mysqli($host, $usuario, $clave, $bd);
if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

// Aceptar tanto JSON como parámetros URL o POST
$data = json_decode(file_get_contents("php://input"), true);

$id_horario = $data["id_horario"] ?? $_POST["id_horario"] ?? $_GET["id_horario"] ?? null;
$dias_semana = $data["dias_semana"] ?? $_POST["dias_semana"] ?? $_GET["dias_semana"] ?? null;

// Validar datos
if (!$id_horario || !$dias_semana) {
    echo json_encode(["success" => false, "message" => "Datos insuficientes"]);
    exit;
}

// Validar formato de los días
if (!preg_match('/^[LMXJVSD]+$/', $dias_semana)) {
    echo json_encode(["success" => false, "message" => "Formato de días inválido"]);
    exit;
}

// Ejecutar actualización
$sql = $con->prepare("UPDATE horario_clase SET dias_semana = ? WHERE id_horario = ?");
$sql->bind_param("si", $dias_semana, $id_horario);

if ($sql->execute()) {
    if ($sql->affected_rows > 0) {
        echo json_encode(["success" => true, "message" => "Horario actualizado correctamente"]);
    } else {
        echo json_encode(["success" => false, "message" => "No se encontró el horario con ese ID"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Error al actualizar horario: " . $sql->error]);
}

$sql->close();
$con->close();
?>
