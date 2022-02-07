package ru.geekbrains.netty_model_obj;

public class RenameFileToServerObj extends AbstractObj {
    private String oldFilename;
    private String newFilename;

    public String getOldFilename() {
        return oldFilename;
    }

    public void setOldFilename(String oldFilename) {
        this.oldFilename = oldFilename;
    }

    public String getNewFilename() {
        return newFilename;
    }

    public void setNewFilename(String newFilename) {
        this.newFilename = newFilename;
    }

}