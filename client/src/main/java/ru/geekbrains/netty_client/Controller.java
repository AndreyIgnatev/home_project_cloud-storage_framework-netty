package ru.geekbrains.netty_client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import ru.geekbrains.netty_model_obj.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class Controller implements Initializable {

    @FXML
    public VBox fileManagerPanel;
    @FXML
    public TextArea textArea;
    @FXML
    public TextField TextFieldRegistrationCenter;
    @FXML
    public TextField TextFieldRegistrationLeft;
    @FXML
    public TextField TextFieldRegistrationRight;
    @FXML
    public Button buttonLoginAuthentication;
    @FXML
    public Button buttonLoginRegistration;
    @FXML
    public ListView<String> clientListViewFileManager;
    @FXML
    public ListView<String> serverListViewFileManager;
    @FXML
    public TextField infoTextFieldFileManager;
    @FXML
    public TextField infoTextFieldClient;
    @FXML
    public TextField infoTextFieldServer;
    @FXML
    public VBox endPanel;
    @FXML
    public VBox authenticationPanel;
    @FXML
    public VBox registrationPanel;
    @FXML
    public TextArea TextAreaInfo;
    @FXML
    public TextField newNameFile;
    @FXML
    public Button buttonOK;
    @FXML
    public Button buttonNO;
    @FXML
    private TextField RightTextField;
    @FXML
    private TextField leftTextField;
    private Network network;
    private Path clientDir;
    private Integer id;
    private boolean flag = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticationWindows();
        network = new Network();
        network.start();
        renameFileVisibleNo();
        Thread thread = new Thread(() -> {
            try {
                while (flag) {
                    AbstractObj abstractObj = network.readObjectMsg();

                    if (abstractObj instanceof AuthenticationObj) {
                        AuthenticationObj authenticationObj = (AuthenticationObj) abstractObj;
                        id = authenticationObj.getId();
                        if (id != null && id != 0) {
                            clientDir = Paths.get("client", "PackageFileClient", (Integer.toString(id)));
                            boolean flagViewPanel = authenticationObj.getMessage().trim().equals("Логин и пароль подтверждены!!!");
                            if (flagViewPanel) {
                                setFileManagerWindows();
                                refreshListFileClient(clientDir);
                                refreshListFileServer(authenticationObj.getId());
                            }
                        } else {
                            textArea.setText(authenticationObj.getMessage());
                        }
                    }

                    if (abstractObj instanceof RegistrationObj) {
                        RegistrationObj registrationObj = (RegistrationObj) abstractObj;
                        if (registrationObj.getId() != null) {
                            if (!Files.exists(Paths.get("client", "PackageFileClient", (Integer.toString(registrationObj.getId()))))) {
                                Files.createDirectory(Paths.get("client", "PackageFileClient", (Integer.toString(registrationObj.getId()))));
                            }
                            setAuthenticationWindows();
                            textArea.appendText(registrationObj.getMessage());
                            textArea.setText(registrationObj.getMessage());
                        } else {
                            TextAreaInfo.appendText(registrationObj.getMessage());
                        }
                    }

                    if (abstractObj instanceof RefreshServerFileListObj) {
                        RefreshServerFileListObj refreshServerFileListObj = (RefreshServerFileListObj) abstractObj;
                        Platform.runLater(() -> {
                            serverListViewFileManager.getItems().clear();
                            serverListViewFileManager.getItems().addAll(refreshServerFileListObj.getServerFileList());
                        });
                    }

                    if (abstractObj instanceof UploadingFileToServerObj) {
                        UploadingFileToServerObj fileToServerObj = (UploadingFileToServerObj) abstractObj;
                        infoTextFieldFileManager.clear();
                        infoTextFieldFileManager.setText(fileToServerObj.getMessage());
                    }

                    if (abstractObj instanceof UploadingFileToClientObj) {
                        UploadingFileToClientObj fileToClientObj = (UploadingFileToClientObj) abstractObj;
                        if (Files.exists(clientDir.resolve(fileToClientObj.getFilename()))) {
                            infoTextFieldFileManager.setText("Файл был получен от сервера, но не был записан на стороне клиента," +
                                    " так как уже существует с таким именем!");
                        } else {
                            Files.write(clientDir.resolve(fileToClientObj.getFilename()), fileToClientObj.getData(), StandardOpenOption.CREATE);
                        }
                        refreshListFileClient(clientDir);
                        infoTextFieldFileManager.setText("Файл: " + fileToClientObj.getFilename() + " загружен c сервера!");
                    }

                    if (abstractObj instanceof DeleteFileToServerObj) {
                        DeleteFileToServerObj deleteFileToServerObj = (DeleteFileToServerObj) abstractObj;
                        infoTextFieldFileManager.setText(deleteFileToServerObj.getMessage());
                    }

                    if (abstractObj instanceof RenameFileToServerObj) {
                        RenameFileToServerObj fileToServerObj = (RenameFileToServerObj) abstractObj;
                        infoTextFieldFileManager.clear();
                        infoTextFieldFileManager.setText(fileToServerObj.getMessage());
                    }

                }
            } catch (Exception e) {
                log.error("ERROR while() - основной цикл на клиенте!");
            }
        });
        thread.start();
        mouseClickClientListViewFileManager(clientListViewFileManager, infoTextFieldClient);
        mouseClickServerListViewFileManager(serverListViewFileManager, infoTextFieldServer);
    }

    private void mouseClickClientListViewFileManager(ListView<String> clientListViewFileManager, TextField infoTextFieldClient) {
        clientListViewFileManager.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String pathClientListViewFileManager = clientListViewFileManager.getSelectionModel().getSelectedItem();
                infoTextFieldClient.setText(pathClientListViewFileManager);
                infoTextFieldServer.clear();
            }
        });
    }

    private void mouseClickServerListViewFileManager(ListView<String> serverListViewFileManager, TextField infoTextFieldServer) {
        serverListViewFileManager.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String pathServerListViewFileManager = serverListViewFileManager.getSelectionModel().getSelectedItem();
                infoTextFieldServer.setText(pathServerListViewFileManager);
                infoTextFieldClient.clear();
            }
        });
    }

    @FXML
    private void sendAuthObj() {
        AuthenticationObj messageAutObj = new AuthenticationObj();
        messageAutObj.setLogin(leftTextField.getText());
        messageAutObj.setPassword(RightTextField.getText());
        network.writeObjectMsg(messageAutObj);
        leftTextField.clear();
        RightTextField.clear();
    }

    @FXML
    private void sendRegistryObj() {
        RegistrationObj messageRegistersObj = new RegistrationObj();
        messageRegistersObj.setLogin(TextFieldRegistrationCenter.getText());
        if (TextFieldRegistrationLeft.getText().equals(TextFieldRegistrationRight.getText())) {
            messageRegistersObj.setPassword(TextFieldRegistrationLeft.getText());
            network.writeObjectMsg(messageRegistersObj);
            TextFieldRegistrationCenter.clear();
            TextFieldRegistrationRight.clear();
            TextFieldRegistrationLeft.clear();
        } else {
            textArea.setText("Введенные Вами пароли не совпадают!!!");
        }
    }

    private void setAuthenticationWindows() {
        authenticationPanel.setVisible(true);
        authenticationPanel.setManaged(true);
        registrationPanel.setVisible(false);
        registrationPanel.setVisible(false);
        fileManagerPanel.setVisible(false);
        fileManagerPanel.setManaged(false);
        endPanel.setVisible(false);
        endPanel.setManaged(false);
    }

    private void setRegistrationWindows() {
        authenticationPanel.setVisible(false);
        authenticationPanel.setManaged(false);
        registrationPanel.setVisible(true);
        registrationPanel.setVisible(true);
        fileManagerPanel.setVisible(false);
        fileManagerPanel.setManaged(false);
        endPanel.setVisible(false);
        endPanel.setManaged(false);
    }

    private void setFileManagerWindows() {
        fileManagerPanel.setVisible(true);
        fileManagerPanel.setManaged(true);
        authenticationPanel.setVisible(false);
        authenticationPanel.setManaged(false);
        registrationPanel.setVisible(false);
        registrationPanel.setVisible(false);
        endPanel.setVisible(false);
        endPanel.setManaged(false);
    }

    private void setQuitWindows() {
        authenticationPanel.setVisible(false);
        authenticationPanel.setManaged(false);
        registrationPanel.setVisible(false);
        registrationPanel.setVisible(false);
        fileManagerPanel.setVisible(false);
        fileManagerPanel.setManaged(false);
        endPanel.setVisible(true);
        endPanel.setManaged(true);
    }

    private List<String> getFiles(Path path) throws IOException {
        return Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    private void refreshListFileClient(Path path) throws IOException {
        List<String> files = getFiles(path);
        Platform.runLater(() -> {
            clientListViewFileManager.getItems().clear();
            clientListViewFileManager.getItems().addAll(files);
        });
    }

    private void refreshListFileServer(Integer id) {
        RefreshServerFileListObj messageRefresh = new RefreshServerFileListObj();
        messageRefresh.setId(id);
        network.writeObjectMsg(messageRefresh);
    }

    @FXML
    private void FileServerButton() {
        UploadingFileToServerObj upload = new UploadingFileToServerObj();
        upload.setFilename(infoTextFieldClient.getText());
        upload.setId(id);
        if (Files.exists(clientDir.resolve(infoTextFieldClient.getText()))) {
            upload.setData(clientDir.resolve(infoTextFieldClient.getText()));
            network.writeObjectMsg(upload);
        } else {
            log.error("Файл не был отправлен на сервер, так как его не существует!!!");
        }
    }

    @FXML
    private void FileClientButton() {
        UploadingFileToClientObj upload = new UploadingFileToClientObj();
        upload.setFilename(infoTextFieldServer.getText());
        upload.setId(id);
        network.writeObjectMsg(upload);
        infoTextFieldFileManager.setText("Запрос на скачивание файла отправлен на сервер!");
    }

    @FXML
    private void deleteFile() throws IOException {
        if (infoTextFieldClient.getText().length() != 0) {

            Files.delete(clientDir.resolve(infoTextFieldClient.getText()));
            refreshListFileClient(clientDir);
            infoTextFieldFileManager.setText("Файл " + infoTextFieldClient.getText() + " с клиента успешно удален!");
        }

        if (infoTextFieldServer.getText().length() != 0) {
            DeleteFileToServerObj deleteFile = new DeleteFileToServerObj();
            deleteFile.setFilename(infoTextFieldServer.getText());
            deleteFile.setId(id);
            network.writeObjectMsg(deleteFile);
        }
    }

    @FXML
    private void logOutClient() {
        textArea.clear();
        textArea.setText("Вы вышли из вашей учетной записи!");
        setAuthenticationWindows();
    }

    @FXML
    private void quitClient() {
        if (network != null) {
            flag = false;
            network.stopClient();
            setQuitWindows();
        }
    }

    @FXML
    private void registrationForm() {
        setRegistrationWindows();
    }


    @FXML
    private void renameFileVisibleYes() {
        newNameFile.setVisible(true);
        buttonOK.setVisible(true);
        buttonNO.setVisible(true);
        infoTextFieldFileManager.clear();
        infoTextFieldFileManager.setText("Выберите файл на стороне клиента или сервера, который требуется переименовать! ");
    }

    @FXML
    private void renameFileVisibleNo() {
        newNameFile.setVisible(false);
        buttonOK.setVisible(false);
        buttonNO.setVisible(false);
        newNameFile.clear();
    }


    @FXML
    private void buttonRenameFileOK(){
        if (infoTextFieldClient.getText().length()!= 0 && newNameFile.getText().length()!=0) {
            try {
                Files.move(clientDir.resolve(infoTextFieldClient.getText().trim()), clientDir.resolve(newNameFile.getText().trim()), StandardCopyOption.REPLACE_EXISTING);
                refreshListFileClient(clientDir);
            } catch (IOException e) {
                infoTextFieldFileManager.clear();
                infoTextFieldFileManager.setText("Файл не переименован, так как не был найден!");
                log.error("Файл не переименован, так как не был найден!");
            }
            infoTextFieldFileManager.clear();
            infoTextFieldFileManager.setText("Файл " + infoTextFieldClient.getText() + " на стороне клиента успешно создан с новым именем " + newNameFile.getText());
            infoTextFieldClient.clear();
            renameFileVisibleNo();
        }
        if (infoTextFieldServer.getText().length()!= 0) {
            RenameFileToServerObj renameFile = new RenameFileToServerObj();
            renameFile.setId(id);
            renameFile.setOldFilename(infoTextFieldServer.getText());
            renameFile.setNewFilename(newNameFile.getText());
            network.writeObjectMsg(renameFile);
        }
    }

}

