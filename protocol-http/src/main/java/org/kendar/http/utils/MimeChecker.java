package org.kendar.http.utils;

import org.kendar.http.utils.constants.ConstantsHeader;
import org.kendar.http.utils.constants.ConstantsMime;

import java.util.Locale;

public class MimeChecker {
    private static final String[] STATIC_FILES = {
            ".jpg", ".jpeg", ".ico", ".png", ".gif", ".woff2", ".woff", ".otf", ".ttf", ".eot", ".zip",
            ".pdf", ".tif", ".svg", ".tar", ".gz", ".tgz", ".rar", ".html", ".htm", ".js", ".map", ".jpg",
            ".jpeg", ".css", ".json", ".ts"
    };

    public static boolean isBinary(Request request) {
        var cnt = request.getHeader(ConstantsHeader.CONTENT_TYPE);
        if (cnt == null || cnt.isEmpty()) return false;
        return isBinary(cnt.get(0), "");
    }

    public static boolean isBinary(Response request) {
        var cnt = request.getHeader(ConstantsHeader.CONTENT_TYPE);
        if (cnt == null || cnt.isEmpty()) return false;
        return isBinary(cnt.get(0), "");
    }

    public static boolean isJson(String mime) {
        if (mime == null || mime.isEmpty()) {
            return false;
        }
        var mimeLow = mime.toLowerCase(Locale.ROOT);
        ;
        if (mimeLow.contains("json")) return true;
        return false;
    }

    public static boolean isJsonSmile(String mime) {
        if (mime == null || mime.isEmpty()) {
            return false;
        }
        var mimeLow = mime.toLowerCase(Locale.ROOT);
        ;
        if (mimeLow.contains("application/x-jackson-smile")) return true;
        return false;
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isBinary(String mime, String contentEncoding) {

        if (mime == null || mime.isEmpty()) {
            return false;
        }
        var mimeLow = mime.toLowerCase(Locale.ROOT);
        if (mimeLow.contains("text")) return false;
        if (mimeLow.contains("xml")) return false;
        if (mimeLow.contains("soap")) return false;
        if (mimeLow.contains("javascript")) return false;
        if (mimeLow.contains("json")) return false;
        if (mimeLow.contains(ConstantsMime.JSON_SMILE)) return false;
        if (mimeLow.contains("application/x-www-form-urlencoded")) return false;
        return true;
    }

    public static boolean isStatic(String mime, String path) {
        if (mime != null) {
            var mimeLow = mime.toLowerCase(Locale.ROOT);
            if (mimeLow.startsWith("text") || mimeLow.startsWith("image")) {
                return true;
            }
        }
        var pathlow = path.toLowerCase(Locale.ROOT);
        if (pathlow.equalsIgnoreCase("/") || pathlow.equalsIgnoreCase("")) {
            return true;
        }
        for (int i = 0; i < STATIC_FILES.length; i++) {
            if (pathlow.endsWith(STATIC_FILES[i])) return true;
        }
        return false;
    }
}
