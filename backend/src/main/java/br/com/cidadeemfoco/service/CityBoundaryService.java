package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CityBoundaryService {

    private static final double BOUNDARY_TOLERANCE = 1e-10;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceLoader resourceLoader;
    private final Map<String, List<Polygon>> boundaryCache = new ConcurrentHashMap<>();

    public CityBoundaryService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void validateCoordinates(City city, BigDecimal latitude, BigDecimal longitude) {
        List<Polygon> polygons = boundaryCache.computeIfAbsent(city.getIbgeCode(), this::loadBoundary);
        Point point = new Point(longitude.doubleValue(), latitude.doubleValue());
        if (polygons.stream().noneMatch(polygon -> contains(polygon, point))) {
            throw new BusinessRuleException(
                    "A localizacao da ocorrencia deve estar dentro do municipio de " + city.getName()
            );
        }
    }

    private List<Polygon> loadBoundary(String ibgeCode) {
        Resource resource = resourceLoader.getResource(
                "classpath:city-boundaries/" + ibgeCode + ".geojson"
        );
        if (!resource.exists()) {
            throw new BusinessRuleException("O limite geografico da cidade ainda nao esta configurado");
        }

        try (var inputStream = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            List<Polygon> polygons = new ArrayList<>();
            collectPolygons(root, polygons);
            if (polygons.isEmpty()) {
                throw new BusinessRuleException("O limite geografico da cidade esta invalido");
            }
            return List.copyOf(polygons);
        } catch (IOException | IllegalArgumentException exception) {
            throw new BusinessRuleException("Nao foi possivel carregar o limite geografico da cidade");
        }
    }

    private void collectPolygons(JsonNode root, List<Polygon> polygons) {
        String type = requiredText(root, "type");
        switch (type) {
            case "FeatureCollection" -> root.path("features")
                    .forEach(feature -> collectPolygons(feature, polygons));
            case "Feature" -> collectPolygons(root.path("geometry"), polygons);
            case "Polygon" -> polygons.add(parsePolygon(root.path("coordinates")));
            case "MultiPolygon" -> root.path("coordinates")
                    .forEach(coordinates -> polygons.add(parsePolygon(coordinates)));
            default -> throw new IllegalArgumentException("Unsupported GeoJSON type: " + type);
        }
    }

    private Polygon parsePolygon(JsonNode coordinates) {
        List<List<Point>> rings = new ArrayList<>();
        coordinates.forEach(ringNode -> {
            List<Point> ring = new ArrayList<>();
            ringNode.forEach(coordinate -> ring.add(new Point(
                    coordinate.path(0).asDouble(),
                    coordinate.path(1).asDouble()
            )));
            if (ring.size() < 4) {
                throw new IllegalArgumentException("A polygon ring must have at least four points");
            }
            rings.add(List.copyOf(ring));
        });
        if (rings.isEmpty()) {
            throw new IllegalArgumentException("A polygon must have at least one ring");
        }
        return new Polygon(List.copyOf(rings));
    }

    private String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        if (value == null) {
            throw new IllegalArgumentException("Missing GeoJSON field: " + field);
        }
        return value;
    }

    private boolean contains(Polygon polygon, Point point) {
        if (!isInsideRing(polygon.rings().getFirst(), point)) {
            return false;
        }
        return polygon.rings().stream()
                .skip(1)
                .noneMatch(hole -> isInsideRing(hole, point) && !isOnBoundary(hole, point));
    }

    private boolean isInsideRing(List<Point> ring, Point point) {
        if (isOnBoundary(ring, point)) {
            return true;
        }

        boolean inside = false;
        for (int current = 0, previous = ring.size() - 1; current < ring.size(); previous = current++) {
            Point a = ring.get(current);
            Point b = ring.get(previous);
            boolean crossesLatitude = (a.latitude() > point.latitude()) != (b.latitude() > point.latitude());
            if (crossesLatitude) {
                double intersectionLongitude = (b.longitude() - a.longitude())
                        * (point.latitude() - a.latitude())
                        / (b.latitude() - a.latitude())
                        + a.longitude();
                if (point.longitude() < intersectionLongitude) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }

    private boolean isOnBoundary(List<Point> ring, Point point) {
        for (int current = 0, previous = ring.size() - 1; current < ring.size(); previous = current++) {
            if (isOnSegment(ring.get(previous), ring.get(current), point)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnSegment(Point start, Point end, Point point) {
        double crossProduct = (point.latitude() - start.latitude())
                * (end.longitude() - start.longitude())
                - (point.longitude() - start.longitude())
                * (end.latitude() - start.latitude());
        if (Math.abs(crossProduct) > BOUNDARY_TOLERANCE) {
            return false;
        }
        return point.longitude() >= Math.min(start.longitude(), end.longitude()) - BOUNDARY_TOLERANCE
                && point.longitude() <= Math.max(start.longitude(), end.longitude()) + BOUNDARY_TOLERANCE
                && point.latitude() >= Math.min(start.latitude(), end.latitude()) - BOUNDARY_TOLERANCE
                && point.latitude() <= Math.max(start.latitude(), end.latitude()) + BOUNDARY_TOLERANCE;
    }

    private record Point(double longitude, double latitude) {
    }

    private record Polygon(List<List<Point>> rings) {
    }
}
