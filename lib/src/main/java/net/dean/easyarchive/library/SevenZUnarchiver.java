package net.dean.easyarchive.library;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static net.dean.easyarchive.library.InflationUtils.*;

public class SevenZUnarchiver extends AbstractUnarchiver {

    public SevenZUnarchiver() {
        super("7z");
    }

    @NotNull
    @Override
    public List<File> doInflate(@NotNull File f, @NotNull File dest, int total) throws IOException {
        SevenZFile seven = new SevenZFile(f);
        OutputStream out;

        List<File> files = new ArrayList<>();

        SevenZArchiveEntry entry;
        while ((entry = seven.getNextEntry()) != null) {
            File outFile = new File(dest, entry.getName());
            mkdirs(entry.isDirectory() ? outFile : outFile.getParentFile());
            if (!outFile.isDirectory()) {
                out = newOutputStream(outFile);
                byte[] content = new byte[(int) entry.getSize()];
                seven.read(content, 0, content.length);
                out.write(content);
                out.close();

                files.add(outFile);

                log(ArchiveEvent.inflate(outFile, files.size(), total));
            }
        }
        return files;

    }

    @Override
    public int doCount(@NotNull File f) throws IOException {
        SevenZFile seven = null;
        try {
            seven = new SevenZFile(f);
            int total = 0;
            SevenZArchiveEntry entry;
            while ((entry = seven.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    total++;
                }
            }
            return total;
        } finally {
            if (seven != null)
                seven.close();
        }
    }
}
