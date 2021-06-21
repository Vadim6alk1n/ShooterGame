package ShooterPack.Client;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class ShooterStart {
    public Stage stage;
    public ShooterStart(Stage primaryStage)
    {
        //UI
        Pane root = new Pane();
        Scene scene = new Scene(root,250,150);
        stage = primaryStage;
        stage.setTitle("Super shooter 12/10");
        stage.setScene( scene);

        TextField ipfield = new TextField();
        ipfield.setLayoutX(10);
        ipfield.setLayoutY(50);
        ipfield.setPrefHeight(30);
        ipfield.setPrefWidth(200);
        root.getChildren().add(ipfield);

        TextField namefield = new TextField();
        namefield.setLayoutX(10);
        namefield.setLayoutY(10);
        namefield.setPrefHeight(30);
        namefield.setPrefWidth(200);
        root.getChildren().add(namefield);

        Button connectButton = new Button("Connect");
        connectButton.setLayoutX(100);
        connectButton.setLayoutY(90);
        root.getChildren().add(connectButton);
        connectButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                String msg = ipfield.getText();
                String name = namefield.getText();
                ShooterGame game = new ShooterGame(stage);
                if (game.connect(msg,name)==1)
                {
                    stage.setScene(game.scene);
                    Thread gamethread = new Thread(game);
                    gamethread.start();
                }
            }
        });

    }
}
