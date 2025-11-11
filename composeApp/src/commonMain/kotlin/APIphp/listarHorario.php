<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión a la base de datos"]);
    exit;
}

$sql = "
    SELECT 
        h.id_horario, 
        h.id_clase, 
        c.nombre_clase, 
        h.dias_semana
    FROM horario_clase h
    INNER JOIN clases c ON h.id_clase = c.id_clase
";

$result = $con->query($sql);

if ($result && $result->num_rows > 0) {
    $horarios = [];
    while ($row = $result->fetch_assoc()) {
        $horarios[] = $row;
    }
    echo json_encode([
        "success" => true,
        "data" => $horarios
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "No hay horarios registrados"
    ]);
}

$con->close();
?>
