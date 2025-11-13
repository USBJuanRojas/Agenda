<?php
header("Content-Type: application/json; charset=UTF-8");
include_once("conexion.php");

$con = new mysqli($host, $usuario, $clave, $bd);
if ($con->connect_error) {
    echo json_encode(["status" => "error", "message" => "Error de conexión"]);
    exit;
}

// Parámetros
$id_usuario = $_POST["id_usuario"] ?? null;
$id_clase_nueva = $_POST["id_clase_nueva"] ?? null;
$dias_semana = $_POST["dias_semana"] ?? null;
$hora_inicio = $_POST["hora_inicio"] ?? null;
$hora_fin = $_POST["hora_fin"] ?? null;

if (!$id_usuario || !$id_clase_nueva || !$dias_semana || !$hora_inicio || !$hora_fin) {
    echo json_encode(["status" => "error", "message" => "Faltan parámetros"]);
    exit;
}

// Obtener todas las clases actuales del estudiante (excepto la nueva)
$query = "
    SELECT c.id_clase, c.nombre_clase, c.hora_inicio, c.hora_fin, h.dias_semana
    FROM gestor_clases g
    INNER JOIN clases c ON g.id_clase = c.id_clase
    INNER JOIN horario_clase h ON c.id_clase = h.id_clase
    WHERE g.id_usuario = ? AND c.id_clase != ?
";
$stmt = $con->prepare($query);
$stmt->bind_param("ii", $id_usuario, $id_clase_nueva);
$stmt->execute();
$result = $stmt->get_result();

// Verificar conflicto
$conflicto = false;
$clase_conflictiva = "";
$horario_ini_conflictivo = "";
$horario_fin_conflictivo = "";

while ($row = $result->fetch_assoc()) {
    // Revisar si comparten algún día
    $dias_actual = str_split($row["dias_semana"]);
    $dias_nueva = str_split($dias_semana);
    $dias_comunes = array_intersect($dias_actual, $dias_nueva);

    if (!empty($dias_comunes)) {
        // Comparar horas
        if (
            ($hora_inicio < $row["hora_fin"]) &&
            ($hora_fin > $row["hora_inicio"])
        ) {
            $conflicto = true;
            $clase_conflictiva = $row["nombre_clase"];
            $horario_ini_conflictivo = $row["hora_inicio"];
            $horario_fin_conflictivo = $row["hora_fin"];
            break;
        }
    }
}

if ($conflicto) {
    echo json_encode([
        "status" => "conflicto",
        "message" => "El/La estudiante ya tiene una clase en ese horario ($clase_conflictiva). Entre $horario_ini_conflictivo - $horario_fin_conflictivo."
    ]);
} else {
    echo json_encode(["status" => "ok", "message" => "Sin conflictos."]);
}

$stmt->close();
$con->close();
?>
