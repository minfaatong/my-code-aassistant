package com.github.minfaatong.tool.codeworkbench;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import com.github.minfaatong.tool.codeworkbench.handler.CloneButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.CmdButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.IDEButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.listener.GitUrlParserListener;
import com.github.minfaatong.tool.codeworkbench.listener.ProjectPathChangedListener;
import com.github.minfaatong.tool.codeworkbench.listener.URLChangeListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;

@Slf4j
public class GitCloneAndOpenProjectFX2 extends Application implements GitUrlParserListener {
    MainController controller = null;

    CloneButtonClickedEventEventHandler cloneButtonClickedEventEventHandler;

    @Override
    public void start(Stage primaryStage) {
        log.info("UI Thread - {}", Platform.isFxApplicationThread() ? "UI Thread" : "Background Thread");

        try {
//            URL fxmlUrl = Thread.currentThread().getContextClassLoader().getResource("fxml/git-clone-and-open-project.fxml");
            URL fxmlUrl = getClass().getResource("fxml/git-clone-and-open-project.fxml");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(fxmlUrl);
            Parent root = loader.load();

            controller = loader.getController();
            Config config = readConfig("src/main/resources/config.yml");

            controller.getTfUrl().textProperty().addListener(new URLChangeListener(this));

            controller.getBtnOpenInIDE().setDisable(true);
            controller.getBtnOpenInTerm().setDisable(true);

            cloneButtonClickedEventEventHandler = new CloneButtonClickedEventEventHandler(
                    controller.getTfUrl(), controller.getTfShortName(),
                    controller.getTfCurrentProjectPath(),
                    controller.getTaLogConsole());
            controller.getBtnClone().setOnAction(cloneButtonClickedEventEventHandler);

            controller.getBtnOpenInIDE().setOnAction(new IDEButtonClickedEventEventHandler(
                    cloneButtonClickedEventEventHandler,
                    controller.getTfCurrentProjectPath(),
                    config));
            controller.getBtnOpenInTerm().setOnAction(new CmdButtonClickedEventEventHandler(
                    cloneButtonClickedEventEventHandler,
                    controller.getTfCurrentProjectPath(),
                    config));

            controller.getTfCurrentProjectPath().textProperty().addListener(
                    new ProjectPathChangedListener(
                            controller.getBtnOpenInIDE(), controller.getBtnOpenInTerm()));

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Git Clone and Open Project");
            primaryStage.show();
        } catch (IOException e) {
            log.error("Error while creating form", e);
            showErrorMessage("Error loading FXML file", e);
        }
    }

    protected Config readConfig(String configPath) {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(configPath);
        } catch (FileNotFoundException e) {
            log.error("Error while reading app config - {}", configPath, e);
        }
        return yaml.loadAs(fileInputStream, Config.class);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void gitUrlParsed(String host, String organization, String repository, String branch, String shortName) {
        log.info("branch='{}', shortName='{}'", branch, shortName);
        if (StringUtils.isEmpty(controller.getTfShortName().getText())) {
            controller.getTfShortName().setText(shortName);
        }
    }
}
