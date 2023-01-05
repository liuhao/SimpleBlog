package simpleblog.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/** OpenWeatherAPI Created by lyoo on 3/27/2016. */
public class OpenWeatherMapData implements WeatherData {
  private static Logger logger = LogManager.getLogger(OpenWeatherMapData.class.getName());

  private static final String OPENWEATHERMAP_GET_URL;
  private static final double KELVIN = 273.15;

  static {
    OPENWEATHERMAP_GET_URL = "http://api.openweathermap.org/data/2.5/weather";
  }

  private String cityId;
  private String appId;
  private Document payloadDocument;

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setPayloadDocument(Document dataDocument) {
    this.payloadDocument = dataDocument;
  }

  private void getXmlDocument() {
    String url =
        OPENWEATHERMAP_GET_URL
            + "?q="
            + cityId
            + "&unit=kelvin&mode=xml&appid="
            + Optional.ofNullable(System.getenv(appId)).orElse("undefined");
    try {
      URL getUrl = new URL(url);
      try {
        SAXReader saxReader = new SAXReader();
        this.payloadDocument = saxReader.read(getUrl); // 读取XML文件,获得document对象
      } catch (Exception e) {
        logger.catching(e);
        logger.error("SAXRead error");
      }
    } catch (MalformedURLException e) {
      logger.catching(e);
      logger.error("no protocol is specified, or an unknown protocol is found, or url is null.");
    }
  }

  public String getWeather() {
    String weatherText = "unknown ?℃~?℃";
    if (null == payloadDocument) {
      getXmlDocument();
    }
    Node nodeWeather = payloadDocument.selectSingleNode("//weather");
    Node nodeTemperature = payloadDocument.selectSingleNode("//temperature");
    if (null == nodeWeather || null == nodeTemperature) {
      logger.error("the forecast element is not exist!");
    } else {
      String weather = nodeWeather.valueOf("@value");
      String minTemp = "?";
      if (!nodeTemperature.valueOf("@min").isEmpty()) {
        minTemp =
            String.valueOf(
                new BigDecimal(Double.valueOf(nodeTemperature.valueOf("@min")) - KELVIN)
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .doubleValue());
      }
      String maxTemp = "?";
      if (!nodeTemperature.valueOf("@max").isEmpty()) {
        maxTemp =
            String.valueOf(
                new BigDecimal(Double.valueOf(nodeTemperature.valueOf("@max")) - KELVIN)
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .doubleValue());
      }
      weatherText = weather + " " + minTemp + "℃~" + maxTemp + "℃";
      return weatherText;
    }
    return weatherText;
  }

  public String getLocation() {
    logger.traceEntry();
    String tagText = "unknown";
    if (null == payloadDocument) {
      getXmlDocument();
    }
    Node nodeCity = payloadDocument.selectSingleNode("//city");
    if (!nodeCity.valueOf("@name").isEmpty()) {
      tagText = " " + nodeCity.valueOf("@name");
    }
    return tagText;
  }
}
