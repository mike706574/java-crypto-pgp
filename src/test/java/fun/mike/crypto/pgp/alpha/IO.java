package fun.mike.crypto.pgp.alpha;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IO {
    public static void mkdir(String path) {
        new File(path).mkdir();
    }

    public static void copy(String srcPath, String destPath) {
        try (FileInputStream is = new FileInputStream(srcPath);
             FileChannel ic = is.getChannel();
             FileOutputStream os = new FileOutputStream(destPath);
             FileChannel oc = os.getChannel()) {
            oc.transferFrom(ic, 0, ic.size());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String slurp(String path) {
        try (InputStream is = new URL(path).openConnection().getInputStream()) {
            return slurp(is);
        } catch (MalformedURLException mue) {
            try {
                return new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String slurp(InputStream is) {
        try (Reader isReader = new InputStreamReader(is, "UTF-8");
             Reader reader = new BufferedReader(isReader)) {
            StringBuilder stringBuilder = new StringBuilder();
            int c = 0;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }

            return stringBuilder.toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static void nuke(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            for (String filePath : file.list()) {
                new File(file.getPath(), filePath).delete();
            }
        }
        file.delete();
    }

    public static void deleteQuietly(String path) {
        new File(path).delete();
    }

    public static void zip(String zipPath, Collection<String> paths) {
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (String path : paths) {
                zos.putNextEntry(new ZipEntry(new File(path).getName()));

                byte[] bytes = Files.readAllBytes(Paths.get(path));
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static void zip(String zipPath, Map<String, InputStream> entries) {
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (Map.Entry<String, InputStream> entry : entries.entrySet()) {
                String path = (String) entry.getKey();
                try (InputStream is = entry.getValue()) {
                    zos.putNextEntry(new ZipEntry(new File(path).getName()));
                    pipe(is, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Boolean exists(String path) {
        return new File(path).exists();

    }

    public static long pipe(InputStream input, OutputStream output) {
        try {
            byte[] buffer = new byte[1024];
            long count = 0;
            int n = 0;
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Stream<String> streamLines(String path) {
        try {
            return Files.lines(Paths.get(path));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
