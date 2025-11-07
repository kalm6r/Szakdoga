package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UrlImageLoader {
    private static final Map<String, Image> MEM = new ConcurrentHashMap<>();
    private static final Path CACHE_DIR = Paths.get(System.getProperty("user.home"), ".katalogus-cache", "images");

    private UrlImageLoader() {}

    public static Image get(String url, int maxW, int maxH) {
    	// classpath: erőforrás betöltés (pl. classpath:/images/mxmaster3s.jpg)
    	if (url.startsWith("classpath:")) {
    	    String res = url.substring("classpath:".length());
    	    if (!res.startsWith("/")) res = "/" + res;
    	    try (java.io.InputStream in = UrlImageLoader.class.getResourceAsStream(res)) {
    	        if (in == null) return null;
    	        javax.imageio.ImageIO.setUseCache(false);
    	        java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(in);
    	        if (src == null) return null;
    	        java.awt.Image scaled = scale(src, maxW, maxH);
    	        MEM.put(url + "|" + maxW + "x" + maxH, scaled);
    	        return scaled;
    	    } catch (Exception ex) {
    	        return null;
    	    }
    	}

        if (url == null || url.isBlank()) return null;
        String key = url + "|" + maxW + "x" + maxH;
        Image cached = MEM.get(key);
        if (cached != null) return cached;

        try {
            Files.createDirectories(CACHE_DIR);
            Path file = CACHE_DIR.resolve(hash(url) + ext(url));
            BufferedImage src;
            if (Files.exists(file)) {
                try (InputStream in = Files.newInputStream(file)) {
                    src = ImageIO.read(in);
                }
            } else {
                URLConnection con = new URL(url).openConnection();
                con.setConnectTimeout(4000);
                con.setReadTimeout(6000);
                con.setRequestProperty("User-Agent", "CatalogApp/1.0");
                try (InputStream in = con.getInputStream()) {
                    // mentsük cache-be
                    Path tmp = Files.createTempFile("img", ".tmp");
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                    Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                }
                try (InputStream in = Files.newInputStream(file)) {
                    src = ImageIO.read(in);
                }
            }
            if (src == null) return null;
            Image scaled = scale(src, maxW, maxH);
            MEM.put(key, scaled);
            return scaled;
        } catch (Exception e) {
            return null; // halkan elnyeljük: kép nélkül is működjön a kártya
        }
    }

    private static String ext(String url) {
        int i = url.lastIndexOf('.');
        if (i > 0 && i > url.lastIndexOf('/')) return url.substring(i).toLowerCase();
        return ".img";
    }

    private static String hash(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] d = md.digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static Image scale(BufferedImage src, int maxW, int maxH) {
        double r = Math.min(maxW / (double) src.getWidth(), maxH / (double) src.getHeight());
        int w = Math.max(1, (int) Math.round(src.getWidth() * r));
        int h = Math.max(1, (int) Math.round(src.getHeight() * r));
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}
