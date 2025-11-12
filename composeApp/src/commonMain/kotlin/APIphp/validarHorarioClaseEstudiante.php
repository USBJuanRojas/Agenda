<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit();
}

$id_usuario = $_POST["id_usuario"] ?? null;
$id_clase = $_POST["id_clase"] ?? null;

if (!$id_usuario || !$id_clase) {
    echo json_encode(["success" => false, "message" => "Faltan parámetros"]);
    exit();
}

// Obtener datos de la clase objetivo
$sqlClase = "
    SELECT c.hora_inicio, c.hora_fin, h.dias_semana
    FROM clases c
    INNER JOIN horario_clase h ON c.id_clase = h.id_clase
    WHERE c.id_clase = ?
";
$stmt = $con->prepare($sqlClase);
$stmt->bind_param("i", $id_clase);
$stmt->execute();
$datosClase = $stmt->get_result()->fetch_assoc();
$stmt->close();

if (!$datosClase) {
    echo json_encode(["success" => false, "message" => "Clase no encontrada"]);
    exit();
}

$hora_inicio = $datosClase["hora_inicio"];
$hora_fin = $datosClase["hora_fin"];
$dias_semana = $datosClase["dias_semana"];

// Buscar clases del estudiante con horario cruzado
$sqlConf = "
    SELECT c.nombre_clase, h.dias_semana, c.hora_inicio, c.hora_fin
    FROM gestor_clases g
    INNER JOIN clases c ON g.id_clase = c.id_clase
    INNER JOIN horario_clase h ON c.id_clase = h.id_clase
    WHERE g.id_usuario = ?
    AND (
        (? < c.hora_fin AND ? > c.hora_inicio)
    )
    AND (
        h.dias_semana REGEXP CONCAT('[', ?, ']')
    )
";
$stmt = $con->prepare($sqlConf);
$stmt->bind_param("ssss", $id_usuario, $hora_inicio, $hora_fin, $dias_semana);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows > 0) {
    $conflictos = [];
    while ($row = $res->fetch_assoc()) {
        $conflictos[] = $row;
    }
    echo json_encode([
        "success" => false,
        "message" => "El estudiante ya tiene una clase en ese horario.",
        "conflictos" => $conflictos
    ]);
} else {
    echo json_encode(["success" => true, "message" => "No hay conflicto de horario"]);
}

$stmt->close();
$con->close();
?>
