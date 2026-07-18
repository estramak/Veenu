package services;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Reverse geocoding via Nominatim (OpenStreetMap) — converts a
 * lat/lon pin drop into a street address, per Architecture.md's
 * planned geocoding integration.
 *
 * Nominatim's usage policy requires a descriptive User-Agent and caps
 * requests at 1/second for the free public endpoint. Fine for
 * development traffic; if listing creation volume grows, this should
 * move to a self-hosted Nominatim instance or a paid provider —
 * revisit before that becomes a bottleneck.
 */
@Service
public class GeocodingService {

    private static final String NOMINATIM_REVERSE_URL =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%s&lon=%s";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReverseGeocodeResult reverseGeocode(double latitude, double longitude) {
        String url = String.format(NOMINATIM_REVERSE_URL, latitude, longitude);

        HttpHeaders headers = new HttpHeaders();
        // Required by Nominatim's usage policy, identifies the app,
        // not a browser User-Agent string
        headers.set("User-Agent", "Veenu/0.1 (community map app, dev contact TBD)");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode address = root.path("address");

            return ReverseGeocodeResult.builder()
                    .address(buildStreetAddress(address))
                    .city(firstNonBlank(address, "city", "town", "village", "hamlet"))
                    .state(abbreviateState(textOrNull(address, "state")))
                    .zip(textOrNull(address, "postcode"))
                    .country(textOrNull(address, "country"))
                    .build();

        } catch (Exception e) {
            // Reverse geocoding is a convenience, not a hard requirement
            // — if Nominatim is down/rate-limited/unparseable, fall back
            // to requiring the user to type the address manually rather
            // than failing listing creation entirely.
            return ReverseGeocodeResult.builder().build();
        }
    }

    private String buildStreetAddress(JsonNode address) {
        String houseNumber = textOrNull(address, "house_number");
        String road = textOrNull(address, "road");
        if (road == null) return null;
        return houseNumber != null ? houseNumber + " " + road : road;
    }

    private String firstNonBlank(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = textOrNull(node, field);
            if (value != null) return value;
        }
        return null;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText() : null;
    }

    // Nominatim returns full state names ("Washington"); your Listing
    // schema expects a 2-letter abbreviation per Database_Schema.md.
    // TODO: this only covers WA/OR/ID for now since that's your launch
    // region — expand this map before supporting other states.
    private String abbreviateState(String fullName) {
        if (fullName == null) return null;
        return switch (fullName) {
            case "Washington" -> "WA";
            case "Oregon" -> "OR";
            case "Idaho" -> "ID";
            default -> fullName;
        };
    }

    @Getter
    @Builder
    @NoArgsConstructor(force = true)
    @AllArgsConstructor
    public static class ReverseGeocodeResult {
        private String address;
        private String city;
        private String state;
        private String zip;
        private String country;
    }
}
