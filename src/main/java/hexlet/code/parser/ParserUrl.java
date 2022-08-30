package hexlet.code.parser;

import java.net.MalformedURLException;
import java.net.URL;

public class ParserUrl {
    public static String parse(String stringUrl) throws MalformedURLException {
//        StringBuilder builder = new StringBuilder();
        URL parsedUrl = new URL(stringUrl);
        return String.format("%s://%s%s", parsedUrl.getProtocol(),
                parsedUrl.getHost(),
                parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort()).toLowerCase();
//        String protocol = parsedUrl.getProtocol();
//        String host = parsedUrl.getHost();
//        String port = String.valueOf(parsedUrl.getPort());
//        return builder.append(protocol).append("://").append(host).append(port.equals("-1") ? "" : ":" + port)
//                .toString();
    }
}
