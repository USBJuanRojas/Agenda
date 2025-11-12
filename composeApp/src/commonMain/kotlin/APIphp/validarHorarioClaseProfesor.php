<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Leer cuerpo JSON
$input = file_get_contents("php://input");
$data = json_decode($input, true);

if (!$data) {
    echo json_encode(["success" => false, "message" => "No se recibieron datos JSON válidos", "input" => $input]);
    exit;
}

// Parámetros requeridos
$id_profesor = $data['id_profesor'] ?? null;
$dias_semana = $data['dias_semana'] ?? null;   // ej: "L", "LX", "LMV", etc.
$hora_inicio = $data['hora_inicio'] ?? null; // formato: "08:00" o "08:00:00"
$hora_fin = $data['hora_fin'] ?? null;       // formato: "10:00" o "10:00:00"
$id_clase_excluir = $data['id_clase'] ?? null; // opcional: clase actual para excluir

if (!$id_profesor || !$dias_semana || !$hora_inicio || !$hora_fin) {
    echo json_encode(["success" => false, "message" => "Faltan parámetros requeridos", "received" => $data]);
    exit;
}

$con = new mysqli($host, $usuario, $clave, $bd);
if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión: " . $con->connect_error]);
    exit;
}
$con->set_charset("utf8mb4");

$newStart = $hora_inicio;
$newEnd   = $hora_fin;

// Base del query
$sql = "
SELECT c.id_clase, c.nombre_clase, h.dias_semana, c.hora_inicio, c.hora_fin
FROM clases c
INNER JOIN horario_clase h ON c.id_clase = h.id_clase
WHERE c.id_profesor = ?
  AND h.dias_semana REGEXP CONCAT('[', ?, ']')
  AND (c.hora_inicio < ? AND c.hora_fin > ?)
";

// Si se debe excluir una clase (por ejemplo, al editar)
if ($id_clase_excluir) {
    $sql .= " AND c.id_clase != ?";
}

$stmt = $con->prepare($sql);
if (!$stmt) {
    echo json_encode(["success" => false, "message" => "Error preparando consulta", "error" => $con->error]);
    exit;
}

// Bind dinámico
if ($id_clase_excluir) {
    // 1=id_profesor (i), 2=dias_semana (s), 3=newEnd (s), 4=newStart (s), 5=id_clase_excluir (i)
    $stmt->bind_param("isssi", $id_profesor, $dias_semana, $newEnd, $newStart, $id_clase_excluir);
} else {
    // Sin exclusión
    $stmt->bind_param("isss", $id_profesor, $dias_semana, $newEnd, $newStart);
}

$stmt->execute();
$res = $stmt->get_result();

$conflictos = [];
while ($row = $res->fetch_assoc()) {
    $dias_existentes = str_split($row['dias_semana']);
    $hayDiaComun = false;
    foreach ($dias_existentes as $d) {
        if (strpos($dias_semana, $d) !== false) {
            $hayDiaComun = true;
            break;
        }
    }
    if ($hayDiaComun) {
        $conflictos[] = [
            "id_clase" => $row['id_clase'],
            "nombre_clase" => $row['nombre_clase'],
            "dias_semana" => $row['dias_semana'],
            "hora_inicio" => $row['hora_inicio'],
            "hora_fin" => $row['hora_fin']
        ];
    }
}

if (count($conflictos) > 0) {
    echo json_encode([
        "success" => false,
        "message" => "El profesor ya tiene clase asignada en esos días/horas",
        "conflictos" => $conflictos
    ]);
} else {
    echo json_encode(["success" => true, "message" => "Sin conflictos"]);
}

$stmt->close();
$con->close();
?>
