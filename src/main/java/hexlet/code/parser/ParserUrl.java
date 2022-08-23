package hexlet.code.parser;

import java.net.MalformedURLException;
import java.net.URL;

public class ParserUrl {
    public static String parse(String stringUrl) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        URL url = new URL(stringUrl);
        String protocol = url.getProtocol();
        String host = url.getHost();
        String port = String.valueOf(url.getPort());
        return builder.append(protocol).append("://").append(host).append(port.equals("-1") ? "" : ":" + port)
                .toString();
    }
}
