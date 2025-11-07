<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

$sql = "
SELECT 
    t.id_tarea,
    t.asunto,
    t.descripcion,
    t.fecha_inicio,
    t.fecha_fin,
    t.observaciones,
    c.id_clase,
    c.nombre_clase
FROM tareas t
LEFT JOIN clases c ON t.id_clase = c.id_clase
ORDER BY t.fecha_inicio ASC
";

$result = $con->query($sql);
$tareas = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $tareas[] = $row;
    }
    echo json_encode(["success" => true, "tareas" => $tareas]);
} else {
    echo json_encode(["success" => false, "message" => "No se encontraron tareas"]);
}

$con->close();
?>
