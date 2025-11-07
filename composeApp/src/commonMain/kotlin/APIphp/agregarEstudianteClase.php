<?php
header("Content-Type: application/json; charset=UTF-8");
include("conexion.php");

$response = array();

try {
    $con = new mysqli($host, $usuario, $clave, $bd);

    if ($con->connect_error) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => "Error de conexión"]);
        exit;
    }

    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $id_usuario = $_POST['id_usuario'] ?? null;
        $id_clase = $_POST['id_clase'] ?? null;

        if ($id_usuario && $id_clase) {
            $check = $conexion->prepare("SELECT * FROM gestor_clases WHERE id_usuario = ? AND id_clase = ?");
            $check->bind_param("ii", $id_usuario, $id_clase);
            $check->execute();
            $result = $check->get_result();

            if ($result->num_rows > 0) {
                $response["status"] = "error";
                $response["message"] = "El estudiante ya está agregado a esta clase";
            } else {
                $stmt = $conexion->prepare("INSERT INTO gestor_clases (id_usuario, id_clase) VALUES (?, ?)");
                $stmt->bind_param("ii", $id_usuario, $id_clase);

                if ($stmt->execute()) {
                    $response["status"] = "success";
                    $response["message"] = "Estudiante agregado correctamente";
                } else {
                    $response["status"] = "error";
                    $response["message"] = "Error al agregar estudiante";
                }

                $stmt->close();
            }
            $check->close();
        } else {
            $response["status"] = "error";
            $response["message"] = "Faltan parámetros";
        }
    } else {
        $response["status"] = "error";
        $response["message"] = "Método no permitido";
    }

    echo json_encode($response);
    $conexion->close();
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
