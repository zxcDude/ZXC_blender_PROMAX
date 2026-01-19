package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ObjReader {
    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";
    private static final String OBJ_COMMENT_TOKEN = "#";
    private static final String OBJ_OBJECT_TOKEN = "o";
    private static final String OBJ_GROUP_TOKEN = "g";

    public static Model read(String fileContent) {
        if (fileContent == null || fileContent.trim().isEmpty()) {
            throw new ObjReaderException("File content is empty", 0);
        }

        Model model = new Model();
        int lineNumber = 0;

        try (Scanner scanner = new Scanner(fileContent)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                lineNumber++;

                // Пропускаем пустые строки и комментарии
                if (line.isEmpty() || line.startsWith(OBJ_COMMENT_TOKEN)) {
                    continue;
                }

                List<String> tokens = splitLine(line);
                if (tokens.isEmpty()) {
                    continue;
                }

                String tokenType = tokens.get(0);
                tokens.remove(0);

                switch (tokenType) {
                    case OBJ_VERTEX_TOKEN:
                        parseVertex(tokens, lineNumber, model);
                        break;
                    case OBJ_TEXTURE_TOKEN:
                        parseTextureVertex(tokens, lineNumber, model);
                        break;
                    case OBJ_NORMAL_TOKEN:
                        parseNormal(tokens, lineNumber, model);
                        break;
                    case OBJ_FACE_TOKEN:
                        parseFace(tokens, lineNumber, model);
                        break;
                    case OBJ_OBJECT_TOKEN:
                    case OBJ_GROUP_TOKEN:
                        // Игнорируем объекты и группы для простоты
                        break;
                    default:
                        // Игнорируем неизвестные токены
                        break;
                }
            }
        }

        return model;
    }

    private static List<String> splitLine(String line) {
        // Разделяем строку, но сохраняем пустые токены для формата "f v1//vn1"
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inSlash = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (Character.isWhitespace(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                inSlash = false;
            } else if (c == '/') {
                currentToken.append(c);
                inSlash = true;
            } else {
                currentToken.append(c);
                inSlash = false;
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    private static void parseVertex(List<String> tokens, int lineNumber, Model model) {
        if (tokens.size() < 3) {
            throw new ObjReaderException("Vertex requires at least 3 coordinates", lineNumber);
        }

        try {
            float x = Float.parseFloat(tokens.get(0));
            float y = Float.parseFloat(tokens.get(1));
            float z = Float.parseFloat(tokens.get(2));

            model.addVertex(new Vector3f(x, y, z));
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid number format for vertex: " + e.getMessage(), lineNumber);
        }
    }

    private static void parseTextureVertex(List<String> tokens, int lineNumber, Model model) {
        if (tokens.size() < 2) {
            throw new ObjReaderException("Texture vertex requires at least 2 coordinates", lineNumber);
        }

        try {
            float u = Float.parseFloat(tokens.get(0));
            float v = Float.parseFloat(tokens.get(1));

            model.addTextureVertex(new Vector2f(u, v));
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid number format for texture vertex: " + e.getMessage(), lineNumber);
        }
    }

    private static void parseNormal(List<String> tokens, int lineNumber, Model model) {
        if (tokens.size() < 3) {
            throw new ObjReaderException("Normal requires 3 coordinates", lineNumber);
        }

        try {
            float x = Float.parseFloat(tokens.get(0));
            float y = Float.parseFloat(tokens.get(1));
            float z = Float.parseFloat(tokens.get(2));

            model.addNormal(new Vector3f(x, y, z));
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid number format for normal: " + e.getMessage(), lineNumber);
        }
    }

    private static void parseFace(List<String> tokens, int lineNumber, Model model) {
        if (tokens.isEmpty()) {
            // Некоторые файлы могут содержать пустые полигоны - пропускаем их
            return;
        }

        if (tokens.size() < 3) {
            System.err.println("Warning: Skipping face with less than 3 vertices at line " + lineNumber);
            return;
        }

        List<Integer> vertexIndices = new ArrayList<>();
        List<Integer> textureIndices = new ArrayList<>();
        List<Integer> normalIndices = new ArrayList<>();

        for (String token : tokens) {
            if (token.trim().isEmpty()) {
                continue; // Пропускаем пустые токены
            }

            try {
                parseFaceVertex(token, vertexIndices, textureIndices, normalIndices, lineNumber);
            } catch (ObjReaderException e) {
                // Пропускаем проблемные вершины, но продолжаем обработку
                System.err.println("Warning: " + e.getMessage());
            }
        }

        if (vertexIndices.size() < 3) {
            System.err.println("Warning: Skipping face with less than 3 valid vertices at line " + lineNumber);
            return;
        }

        // Проверяем согласованность индексов
        if (!textureIndices.isEmpty() && textureIndices.size() != vertexIndices.size()) {
            System.err.println("Warning: Mismatched vertex/texture indices at line " + lineNumber);
            textureIndices.clear();
        }

        if (!normalIndices.isEmpty() && normalIndices.size() != vertexIndices.size()) {
            System.err.println("Warning: Mismatched vertex/normal indices at line " + lineNumber);
            normalIndices.clear();
        }

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(vertexIndices);

        if (!textureIndices.isEmpty()) {
            polygon.setTextureVertexIndices(textureIndices);
        }

        if (!normalIndices.isEmpty()) {
            polygon.setNormalIndices(normalIndices);
        }

        model.addPolygon(polygon);
    }

    private static void parseFaceVertex(String token,
                                        List<Integer> vertexIndices,
                                        List<Integer> textureIndices,
                                        List<Integer> normalIndices,
                                        int lineNumber) {
        if (token == null || token.trim().isEmpty()) {
            throw new ObjReaderException("Empty face vertex token at line " + lineNumber, lineNumber);
        }

        // Убираем лишние пробелы
        token = token.trim();

        // Разделяем по "/", но сохраняем пустые элементы
        String[] parts = token.split("/", -1); // -1 сохраняет пустые элементы

        if (parts.length == 0 || parts[0].isEmpty()) {
            throw new ObjReaderException("Missing vertex index in face: " + token, lineNumber);
        }

        try {
            // Индекс вершины (обязательный)
            int vertexIndex = parseIndex(parts[0], lineNumber, "vertex");
            vertexIndices.add(vertexIndex);

            // Текстурные координаты (опциональные)
            if (parts.length > 1 && !parts[1].isEmpty()) {
                int textureIndex = parseIndex(parts[1], lineNumber, "texture");
                textureIndices.add(textureIndex);
            } else if (parts.length > 1 && textureIndices.size() > 0) {
                // Если есть предыдущие текстурные координаты, но текущая отсутствует
                // добавляем -1 как маркер отсутствия
                textureIndices.add(-1);
            }

            // Нормали (опциональные)
            if (parts.length > 2 && !parts[2].isEmpty()) {
                int normalIndex = parseIndex(parts[2], lineNumber, "normal");
                normalIndices.add(normalIndex);
            } else if (parts.length > 2 && normalIndices.size() > 0) {
                // Если есть предыдущие нормали, но текущая отсутствует
                normalIndices.add(-1);
            }

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid face index format: " + token + " - " + e.getMessage(), lineNumber);
        }
    }

    private static int parseIndex(String indexStr, int lineNumber, String type) {
        if (indexStr == null || indexStr.trim().isEmpty()) {
            throw new NumberFormatException("Empty " + type + " index");
        }

        int index = Integer.parseInt(indexStr.trim());

        if (index == 0) {
            throw new NumberFormatException("Zero " + type + " index (OBJ indices start from 1)");
        }

        // OBJ поддерживает отрицательные индексы (относительные)
        if (index < 0) {
            // Возвращаем как есть, обработка будет позже
            return index;
        }

        // Преобразуем в 0-based индекс
        return index - 1;
    }
}