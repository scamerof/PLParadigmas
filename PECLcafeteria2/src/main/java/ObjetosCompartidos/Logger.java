package ObjetosCompartidos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logger compartido para registrar todos los eventos del sistema.
 * Thread-safe usando ReentrantLock.
 * Escribe en fichero "logs/evolution.cafeteria.txt" con timestamp.
 */
public class Logger {
    
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + java.io.File.separator + "evolution.cafeteria.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final ReentrantLock lock = new ReentrantLock(true);
    private static BufferedWriter bufferedWriter = null;
    
    static {
        try {
            // Crear la carpeta logs si no existe
            Files.createDirectories(Paths.get(LOG_DIR));

            // Abrir BufferedWriter con UTF-8 y truncar si existe
            Path p = Paths.get(LOG_FILE);
            bufferedWriter = Files.newBufferedWriter(p, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Registra un evento en el fichero de log con timestamp.
     * Formato: [HH:mm:ss.SSS] evento
     */
    public static void log(String evento) {
        lock.lock();
        try {
            if (bufferedWriter != null) {
                String timestamp = LocalDateTime.now().format(formatter);
                String linea = "[" + timestamp + "] " + evento;
                bufferedWriter.write(linea);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Cierra el fichero de log (llamar al terminar el programa).
     */
    public static void close() {
        lock.lock();
        try {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
