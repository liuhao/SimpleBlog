package simpleblog.plugin;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenWeatherMapDataTest {
  private OpenWeatherMapData openWeatherMapData;

  @BeforeEach
  void setUp() {
    openWeatherMapData = new OpenWeatherMapData();
    try {
      Document xml = new SAXReader().read(new File("src/test/resources/OpenWeatherMap.xml"));
      openWeatherMapData.setPayloadDocument(xml);
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void getWeather() {
    assertEquals("overcast clouds -1.47℃~0.8℃", openWeatherMapData.getWeather());
  }

  @Test
  void getLocation() {
    assertEquals(" Toronto", openWeatherMapData.getLocation());
  }
}
