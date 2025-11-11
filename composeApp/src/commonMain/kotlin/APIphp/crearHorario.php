<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

$con = new mysqli($host, $usuario, $clave, $bd);
if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión: " . $con->connect_error]);
    exit;
}

// Leer JSON o parámetros por POST/GET
$data = json_decode(file_get_contents("php://input"), true);

$id_clase = $data['id_clase'] ?? $_POST['id_clase'] ?? $_GET['id_clase'] ?? null;
$dias_semana = $data['dias_semana'] ?? $_POST['dias_semana'] ?? $_GET['dias_semana'] ?? null;

// Validaciones
if (!$id_clase || !$dias_semana) {
    echo json_encode(["success" => false, "message" => "Faltan parámetros requeridos"]);
    exit;
}

// Validar formato (solo letras válidas L, M, X, J, V, S, D)
if (!preg_match('/^[LMXJVSD]+$/', $dias_semana)) {
    echo json_encode(["success" => false, "message" => "Formato de días inválido"]);
    exit;
}

// Insertar en la tabla correcta
$stmt = $con->prepare("INSERT INTO horario_clase (id_clase, dias_semana) VALUES (?, ?)");
$stmt->bind_param("is", $id_clase, $dias_semana);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Horario creado correctamente"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al crear horario: " . $stmt->error]);
}

$stmt->close();
$con->close();
?>
