package SimpleBlog.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.io.SAXReader;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Get Weather Information from Yahoo Site.
 * Created by lyoo on 9/25/2015.
 */
public class YahooWeatherData implements WeatherData {
    private static final Logger logger = LogManager.getLogger(YahooWeatherData.class.getName());

    private static final String YAHOO_WEATHER_GET_URL;

    static {
        YAHOO_WEATHER_GET_URL = "http://weather.yahooapis.com/forecastrss";
    }

    private String woeid;

    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    private Document getXmlDocument() {
        String url = YAHOO_WEATHER_GET_URL + "?w=" + woeid + "&u=c";
        try {
            URL getUrl = new URL(url);
            try {
                SAXReader saxReader = new SAXReader();
                return saxReader.read(getUrl); // 读取XML文件,获得document对象
            } catch (Exception e) {
                logger.catching(e);
                logger.error("SAXRead error");
            }
        } catch (MalformedURLException e) {
            logger.catching(e);
            logger.error("no protocol is specified, or an unknown protocol is found, or url is null.");
        }
        return null;
    }

    public String getWeather() {
        String weatherText = "unknown ?℃~?℃";

        Document document = getXmlDocument();
        if (document == null) {
            logger.error("fail to get XML Document");
        } else {
            Element weatherElm;
            weatherElm = document.getRootElement().element("channel").element("item").element("forecast");
            if (weatherElm == null) {
                logger.error("the forecast element is not exist!");
            } else {
                Attribute low = weatherElm.attribute("low");
                Attribute high = weatherElm.attribute("high");
                Attribute text = weatherElm.attribute("text");
                weatherText = text.getText() + " " + low.getText() + "℃~" + high.getText() + "℃";
                return weatherText;
            }
        }
        return weatherText;
    }

    public String getLocation() {
        logger.entry();
        String tagText = "unknown";
        Document document = getXmlDocument();

        if (document == null) {
            logger.error("fail to get XML Document");
        } else {
            Element cityElm;
            cityElm = document.getRootElement().element("channel").element("location");
            if (cityElm == null) {
                logger.error("the location element is not exist!");
            } else {
                Attribute city = cityElm.attribute("city");
                tagText = " " + city.getText();
            }
        }
        return tagText;
    }
}
