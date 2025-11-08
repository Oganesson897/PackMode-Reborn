package io.sommers.packmode.core;

import io.sommers.packmode.PMConfig;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

public class SelectUI extends JDialog {

    public SelectUI(File mcLocation) {
        super((JFrame) null, "PackMode Selector ", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        setSize(350, 200);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Thread.currentThread().interrupt();
            }
        });

        JPanel panel = new JPanel();
        add(panel);
        JComboBox<String> box = new JComboBox<>(PMConfig.getAcceptedModes());
        box.setBounds(130, 22, 100, 21);
        panel.add(box);
        JButton button = new JButton("Apply");
        button.addActionListener((e) -> {
            PMConfig.setPackMode((String) box.getSelectedItem());
            overrideConfig(mcLocation);
            PMConfig.setDisableTipScreen(true);
            dispose();
        });
        panel.add(button);

        setVisible(true);
    }

    public static void overrideConfig(File mcLocation) {
        File cfgCurrent = new File(mcLocation, "config");
        File cfgOverride = new File(mcLocation, "config-override/" + PMConfig.getPackMode().toLowerCase());
        if (cfgOverride.isDirectory()) {
            Path sourceDir = Paths.get(cfgOverride.toURI());
            Path targetDir = Paths.get(cfgCurrent.toURI());

            try {
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                Files.walkFileTree(sourceDir, new Impl(targetDir, sourceDir));

                Files.walk(sourceDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);

            } catch (IOException e) {
                LogManager.getLogger("PackMode").error(e);
            }
        }
    }

    private static class Impl extends SimpleFileVisitor<Path> {

        private final Path targetDir;
        private final Path sourceDir;

        private Impl(Path targetDir, Path sourceDir) {

            this.targetDir = targetDir;
            this.sourceDir = sourceDir;
        }

        @Override
        public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
            Path targetFile = targetDir.resolve(sourceDir.relativize(file));
            Files.createDirectories(targetFile.getParent());
            Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public @NotNull FileVisitResult visitFileFailed(@NotNull Path file, @NotNull IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

}
