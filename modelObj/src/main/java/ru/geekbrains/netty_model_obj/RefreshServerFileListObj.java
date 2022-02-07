package ru.geekbrains.netty_model_obj;

import java.util.List;

public class RefreshServerFileListObj extends AbstractObj {
    private List<String> serverFileList;

    public List<String> getServerFileList() {
        return serverFileList;
    }

    public void setServerFileList(List<String> serverFileList) {
        this.serverFileList = serverFileList;
    }
}

