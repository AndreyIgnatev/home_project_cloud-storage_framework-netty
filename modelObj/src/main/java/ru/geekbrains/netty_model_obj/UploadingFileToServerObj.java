package ru.geekbrains.netty_model_obj;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class UploadingFileToServerObj extends AbstractObj {

    private String filename;
    private byte[] data;

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setData(Path path) {
        try {
            this.data = Files.readAllBytes(path);
        } catch (IOException e) {
           log.error("Не возможно записать файл в массив!");
        }
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

}
