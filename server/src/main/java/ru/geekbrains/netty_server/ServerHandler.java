package ru.geekbrains.netty_server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import ru.geekbrains.netty_model_obj.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("ERROR!!!", cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Client disconnected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof AuthenticationObj) {
            AuthenticationObj authObj = (AuthenticationObj) obj;
            if (DBConnection.getConnection() == null) {
                DBConnection.connect();
            }
            Integer idClient = DBConnection.getIdByLoginAndPass(authObj.getLogin().trim(), authObj.getPassword().trim());
            if (idClient != null) {
                authObj.setId(idClient);
                authObj.setMessage("Логин и пароль подтверждены!!!");
                ctx.writeAndFlush(authObj);
            } else {
                AuthenticationObj messageAut = new AuthenticationObj();
                messageAut.setMessage("Логин: " + authObj.getLogin() + " c паролем " + authObj.getPassword() + " не подтверждены!");
                ctx.writeAndFlush(messageAut);
            }
        }
        if (obj instanceof RegistrationObj) {
            RegistrationObj regObj = (RegistrationObj) obj;
            if (DBConnection.getConnection() == null) {
                DBConnection.connect();
            }
            if (DBConnection.findLogin(regObj.getLogin()) != null || regObj.getLogin().length() == 0 || regObj.getPassword().length() == 0) {
                regObj.setMessage(" Вы не зарегистрированны, так как такой пользователь уже существует или " + "\n" + " Вы ввели пустое значение (логин|пароль)");
                regObj.setId(null);
                ctx.writeAndFlush(regObj);
            } else {
                DBConnection.registrationByLoginPassAndNick(regObj.getLogin().trim(), regObj.getPassword().trim());
                Integer idClient = DBConnection.getIdByLoginAndPass(regObj.getLogin().trim(), regObj.getPassword().trim());
                Path serverDir = Paths.get("server", "PackageFileSever", (Integer.toString(idClient)));
                if (!Files.exists(serverDir)) {
                    Files.createDirectory(serverDir);
                }
                regObj.setId(idClient);
                regObj.setMessage("Вы зарегистрированны и теперь можете войдите под своей учетной записью!");
                ctx.writeAndFlush(regObj);
            }
        }

        if (obj instanceof RefreshServerFileListObj) {
            RefreshServerFileListObj refreshObj = (RefreshServerFileListObj) obj;
            Path serverDir = Paths.get("server", "PackageFileSever", (Integer.toString(refreshObj.getId())));
            List<String> listServerFiles = getFiles(serverDir);
            refreshObj.setServerFileList(listServerFiles);
            ctx.writeAndFlush(refreshObj);
        }

        if (obj instanceof UploadingFileToServerObj) {
            UploadingFileToServerObj uploadingToServerObj = (UploadingFileToServerObj) obj;
            Path serverDir = Paths.get("server", "PackageFileSever", (Integer.toString(uploadingToServerObj.getId())));
            if (Files.exists(serverDir.resolve(uploadingToServerObj.getFilename()))) {
                uploadingToServerObj.setMessage("Файл не был записан на сервер, так как уже существует с таким именем!");
                ctx.writeAndFlush(uploadingToServerObj);
            } else {
                Files.write(serverDir.resolve(uploadingToServerObj.getFilename()), uploadingToServerObj.getData(), StandardOpenOption.CREATE);
                uploadingToServerObj.setMessage("Файл " + uploadingToServerObj.getFilename() + " успешно записан на сервер!");
                ctx.writeAndFlush(uploadingToServerObj);
                refreshServerFileListSendObj(serverDir, ctx);
            }

        }
        if (obj instanceof UploadingFileToClientObj) {
            UploadingFileToClientObj uploadingFileToClientObj = (UploadingFileToClientObj) obj;
            Path serverDir = Paths.get("server", "PackageFileSever", (Integer.toString(uploadingFileToClientObj.getId())));
            if (Files.exists(serverDir.resolve(uploadingFileToClientObj.getFilename()))) {
                uploadingFileToClientObj.setData(serverDir.resolve(uploadingFileToClientObj.getFilename()));
                ctx.writeAndFlush(uploadingFileToClientObj);
            } else {
                uploadingFileToClientObj.setMessage("Запрошенный файл не найден!");
                ctx.writeAndFlush(uploadingFileToClientObj);
            }
        }

        if (obj instanceof DeleteFileToServerObj) {
            DeleteFileToServerObj deleteFileToServerObj = (DeleteFileToServerObj) obj;
            Path serverDir = Paths.get("server", "PackageFileSever", (Integer.toString(deleteFileToServerObj.getId())));

            if (Files.exists(serverDir.resolve(deleteFileToServerObj.getFilename()))) {
                Files.delete(serverDir.resolve(deleteFileToServerObj.getFilename()));
                deleteFileToServerObj.setMessage("Файл " + deleteFileToServerObj.getFilename() + " с сервера успешно удален!");
                ctx.writeAndFlush(deleteFileToServerObj);
                refreshServerFileListSendObj(serverDir, ctx);

            } else {
                deleteFileToServerObj.setMessage("Файл " + deleteFileToServerObj.getFilename() + " не был удален на сервере, так как не найден!");
                ctx.writeAndFlush(deleteFileToServerObj);
            }
        }

        if (obj instanceof RenameFileToServerObj) {
            RenameFileToServerObj renameFileToServerObj = (RenameFileToServerObj) obj;
            Path serverDir = Paths.get("server", "PackageFileSever", (Integer.toString(renameFileToServerObj.getId())));

            if (Files.exists(serverDir.resolve(renameFileToServerObj.getOldFilename()))) {
                Files.move(serverDir.resolve(renameFileToServerObj.getOldFilename().trim()), serverDir.resolve(renameFileToServerObj.getNewFilename().trim()), StandardCopyOption.REPLACE_EXISTING);
                renameFileToServerObj.setMessage("Файл " + renameFileToServerObj.getOldFilename() + " на сервера успешно переименован,его новое имя: " + renameFileToServerObj.getNewFilename());
                ctx.writeAndFlush(renameFileToServerObj);
                refreshServerFileListSendObj(serverDir, ctx);

            } else {
                renameFileToServerObj.setMessage("Файл " + renameFileToServerObj.getOldFilename() + " не был перезаписан на сервере, так как не найден!");
                ctx.writeAndFlush(renameFileToServerObj);
            }
        }

    }

    private void refreshServerFileListSendObj(Path serverDir, ChannelHandlerContext ctx) {
        RefreshServerFileListObj refreshServerFileListObj = new RefreshServerFileListObj();
        try {
            refreshServerFileListObj.setServerFileList(getFiles(serverDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.writeAndFlush(refreshServerFileListObj);
    }

    private List<String> getFiles(Path path) throws IOException {
        return Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }
}