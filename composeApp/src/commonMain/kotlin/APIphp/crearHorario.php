<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

$con = new mysqli($host, $usuario, $clave, $bd);
$con->set_charset("utf8mb4");

if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión a la base de datos"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);

$id_clase = $data['id_clase'] ?? $_POST['id_clase'] ?? $_GET['id_clase'] ?? null;
$dias_semana = $data['dias_semana'] ?? $_POST['dias_semana'] ?? $_GET['dias_semana'] ?? null;

if (!$id_clase || !$dias_semana) {
    echo json_encode(["success" => false, "message" => "Faltan parámetros requeridos"]);
    exit;
}

if (!preg_match('/^[LMXJVSD]+$/', $dias_semana)) {
    echo json_encode(["success" => false, "message" => "Formato de días inválido"]);
    exit;
}

// Validar existencia de la clase
$checkClase = $con->prepare("SELECT id_clase FROM clases WHERE id_clase = ?");
$checkClase->bind_param("i", $id_clase);
$checkClase->execute();
$checkClase->store_result();
if ($checkClase->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "La clase especificada no existe"]);
    exit;
}
$checkClase->close();

// Evitar duplicado
$check = $con->prepare("SELECT id_clase FROM horario_clase WHERE id_clase = ? AND dias_semana = ?");
$check->bind_param("is", $id_clase, $dias_semana);
$check->execute();
$check->store_result();
if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Este horario ya existe para la clase"]);
    exit;
}
$check->close();

$stmt = $con->prepare("INSERT INTO horario_clase (id_clase, dias_semana) VALUES (?, ?)");
$stmt->bind_param("is", $id_clase, $dias_semana);

if ($stmt->execute()) {
    echo json_encode([
        "success" => true,
        "message" => "Horario creado correctamente",
        "id_horario" => $stmt->insert_id
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Error interno al crear el horario"]);
}

$stmt->close();
$con->close();
?>
