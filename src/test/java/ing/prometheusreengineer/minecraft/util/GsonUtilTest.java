package ing.prometheusreengineer.minecraft.util;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GsonUtilTest {
    private static final Type TYPE = new TypeToken<Map<String, Integer>>() {}.getType();
    private static final String PRETTY = "{\n    \"a\": 1,\n    \"b\": 2\n}";

    @TempDir
    Path folder;

    private static Map<String, Integer> map() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        return map;
    }

    @Test
    void prettyStringUsesFourSpaces() throws IOException {
        assertEquals(PRETTY, GsonUtil.toPrettyString(map(), TYPE));
    }

    @Test
    void writesTheSameToAFile() throws IOException {
        Path file = folder.resolve("map.json");
        GsonUtil.writePretty(file, map(), TYPE);
        assertEquals(PRETTY, new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }
}
