<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$con = new mysqli($host, $usuario, $clave, $bd);
if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

$id_clase = $_GET["id_clase"] ?? null;

if (!$id_clase) {
    echo json_encode(["success" => false, "message" => "ID de clase no proporcionado"]);
    exit;
}

$sql = $con->prepare("
    SELECT id_horario, id_clase, dias_semana 
    FROM horario_clase 
    WHERE id_clase = ?
");
$sql->bind_param("i", $id_clase);
$sql->execute();

$result = $sql->get_result();
$horarios = $result->fetch_all(MYSQLI_ASSOC);

echo json_encode($horarios);

$sql->close();
$con->close();
?>
