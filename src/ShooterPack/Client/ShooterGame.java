package ShooterPack.Client;

import ShooterPack.Game.Collisions;
import ShooterPack.Game.GameObject;
import ShooterPack.Game.Packets.*;
import ShooterPack.Game.Vec2f;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ShooterGame implements Runnable{
    //ui
    Pane root;
    int screenWidth;
    int screenHeight;
    public int canvasScreenWidth;
    public int canvasScreenHeight;
    GraphicsContext gc;
    ProgressBar cooldowntimer;
    public Scene scene;
    public Stage stage;

    public ShooterGame game;

    //game logic objects
    GameObject player;
    GameObject enemy;
    ArrayList<GameObject> gameobjects;
    Collisions collisions;
    float playerSpeed;

    private int fps;
    private long diff;
    private boolean debugMode;

    //textures
    Image bullettex;
    Image playertex;
    Image skeletontex;
    Image walltex;

    //keyboard keys
    final int key_w = 0;
    final int key_s = 1;
    final int key_a = 2;
    final int key_d = 3;
    boolean keyPressed[];
    boolean keysNeedToSend;
    float mouseX;
    float mouseY;

    //Camera
    public float camX;
    public float camY;

    //Network
    Socket clientSocket;
    InputStream in;
    OutputStream out;

    //Chating
    TextArea textArea;
    TextField textField;
    Button submitText;

    ShooterGame(Stage primaryStage)
    {
        //set game logic variables
        screenWidth = 1000;
        screenHeight = 1000;
        canvasScreenWidth = screenWidth;
        canvasScreenHeight = screenHeight-100;
        gameobjects = new ArrayList<>();
        fps = 60;
        debugMode = false;
        keyPressed = new boolean[5];
        keysNeedToSend = false;
        playerSpeed=3.0f;
        collisions = new Collisions();
        game=this;

        //Load sprites
        playertex = new Image("assets/xeonsheet.bmp");

        playertex = ImageTransparent.makeTransparent(playertex,0,128,0);
        skeletontex = new Image("assets/skeleton.png");
        walltex = new Image("assets/wall.png");
        bullettex = new Image("assets/bullet.png");
        bullettex = ImageTransparent.makeTransparent(bullettex,255,255,255);

        //UI
        root = new Pane();
        scene = new Scene(root,screenWidth,screenHeight) ;
        primaryStage.setTitle("Super shooter 12/10");
        var canvas = new Canvas(canvasScreenWidth, canvasScreenHeight);
        canvas.setLayoutY(100);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        //shot counter
        Text shots = new Text("shots:0");
        shots.setX(50);
        shots.setY(20);
        shots.setFont(new Font(16));
        root.getChildren().add(shots);

        //debug box
        CheckBox debugbox = new CheckBox();
        debugbox.setLayoutX(100);
        debugbox.setLayoutY(70);
        debugbox.setSelected(false);
        debugbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                debugMode = debugbox.isSelected();
            }
        });
        root.getChildren().add(debugbox);

        Text debugtext =new Text("debug");
        debugtext.setFont(new Font(16));
        debugtext.setLayoutX(30);
        debugtext.setLayoutY(80);
        root.getChildren().add(debugtext);

        //Chat
        textArea = new TextArea();
        textArea.setLayoutX(150);
        textArea.setLayoutY(10);
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(200);
        textArea.setPrefRowCount(5);
        textArea.setDisable(true);
        root.getChildren().add(textArea);

        textField = new TextField();
        textField.setLayoutX(750);
        textField.setLayoutY(10);
        textField.setPrefWidth(200);
        textField.setPrefHeight(50);
        root.getChildren().add(textField);

        submitText = new Button();
        submitText.setLayoutX(750);
        submitText.setLayoutY(100);
        submitText.setPrefWidth(100);
        submitText.setPrefHeight(50);
        submitText.setText("Submit");
        root.getChildren().add(submitText);
        submitText.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                String msg = textField.getText();
                byte[] packet = ShooterNetwork.CreateCTextPacket(game);
                try {
                    out.write(packet);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        //Left mouse click on canvas. Do a shot
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                float mx = (float) mouseEvent.getX();
                float my = (float) mouseEvent.getY();
                keyPressed[4]=true;
                keysNeedToSend = true;
                mouseX=mx;
                mouseY=my;
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                keyPressed[4]=false;
                keysNeedToSend = true;
            }
        });
        //Keyboard events
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                handleKeyboardPress(keyEvent);
            }
        });
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                handleKeyboardRelease(keyEvent);
            }
        });

        stage = primaryStage;
        stage.getIcons().add(skeletontex);

    }

    public int connect(String ip,String name)
    {
        //Do network
        try {
            clientSocket = new Socket(ip, 55555);
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            String playername = name;
            int bufsize = 2+4 + playername.length()*2+2;
            ByteBuffer buf = ByteBuffer.allocate(bufsize);
            buf.put((byte)'C');
            buf.put((byte)'C');
            buf.putInt(playername.length());
            buf.put(playername.getBytes("UTF-16"));
            out.write(buf.array());
            byte[] packet = new byte[1024];
            int packetsize = in.read(packet);
            ByteBuffer wrap = ByteBuffer.wrap(packet);
            char packetfrom = (char)wrap.get();
            char packettype = (char)wrap.get();
            if (packetfrom=='S')
                if(packettype=='N') {
                    System.out.println("reading world");
                    byte[] data = new byte[packetsize - 2];
                    wrap.get(data,0,packetsize-2);
                    PacketSConnect pack = new PacketSConnect();
                    pack.read(data);
                    System.out.println("reading world2");
                    ShooterNetwork.ReadServerNewConnection(this, pack);
                    System.out.println("reading world3");
                }

            stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::disconnect);
            camX = player.pos.x;
            camY = player.pos.y;
        }catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }


    public void disconnect(WindowEvent event)
    {
        System.out.println("Stage is closing");
        // Save file
        byte[] msg = new byte[2];
        msg[0]=(byte)ServerPacket.clienttype.charAt(0);
        msg[1]=(byte)ServerPacket.clientdisconnect.charAt(0);
        try
        {
            out.write(msg);
            Thread.sleep(1000);
        }catch (Exception e)
        {e.printStackTrace();}
    }

    public void handleKeyboardPress(KeyEvent keyEvent)
    {
        KeyCode key = keyEvent.getCode();
        if (key==KeyCode.W)
        {
            if(!keyPressed[key_w])
                keysNeedToSend = true;
            keyPressed[key_w]=true;
        }
        if (key==KeyCode.S)
        {
            if(!keyPressed[key_s])
                keysNeedToSend = true;
            keyPressed[key_s]=true;
        }
        if (key==KeyCode.A)
        {
            if(!keyPressed[key_a])
                keysNeedToSend = true;
            keyPressed[key_a]=true;
        }
        if (key==KeyCode.D)
        {
            if(!keyPressed[key_d])
                keysNeedToSend = true;
            keyPressed[key_d]=true;
        }
    }
    public void handleKeyboardRelease(KeyEvent keyEvent)
    {
        KeyCode key = keyEvent.getCode();
        if (key==KeyCode.W)
        {
            keyPressed[key_w]=false;
            keysNeedToSend = true;
        }
        if (key==KeyCode.S)
        {
            keyPressed[key_s]=false;
            keysNeedToSend = true;
        }
        if (key==KeyCode.A)
        {
            keyPressed[key_a]=false;
            keysNeedToSend = true;
        }
        if (key==KeyCode.D)
        {
            keyPressed[key_d]=false;
            keysNeedToSend = true;
        }
    }
    @Override
    public void run() {
        //Main loop

        //loop actions, updater will be call every frame with runLater method
        Runnable network = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        byte[] packet = new byte[1024];
                        int packetsize = game.in.read(packet);
                        // System.out.println(packetsize);

                        ByteBuffer wrap = ByteBuffer.wrap(packet);
                        char packetfrom = (char)wrap.get();
                        char packettype = (char)wrap.get();
                        if (packetfrom == 'S')
                            if (packettype == 'U') {
                                byte[] data = new byte[packetsize - 2];
                                wrap.get(data, 0, packetsize - 2);
                                PacketSUpdate pack = new PacketSUpdate();
                                pack.read(data);
                                ShooterNetwork.ReadServerUpdate(game, pack);
                            }
                            else if(packettype == 'T')
                            {
                                byte[] data = new byte[packetsize - 2];
                                wrap.get(data, 0, packetsize - 2);
                                for (var t : data) {
                                    System.out.print(t + " ");
                                }
                                PacketSText pack = new PacketSText();
                                pack.read(data);
                                textArea.appendText(pack.name+":" + pack.msg + "\n");
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //Update camera
                    camX = player.pos.x;
                    camY = player.pos.y;
                }
        }};

        Runnable updater = new Runnable() {
            @Override
            public void run() {
                draw();
                if(keysNeedToSend)
                {
                    System.out.println("Keys sent");
                    byte[] packet = ShooterNetwork.CreateInputPacket(game);
                    try {
                        out.write(packet);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                keysNeedToSend = false;
            }
        };

        int frametime = 1000/fps;
        long currenttime = System.currentTimeMillis();
        Thread thr = new Thread(network);
        thr.start();
        while (true) {
            //Sleep
            try {
                long newtime = System.currentTimeMillis();
                diff = newtime - currenttime;
                long sleeptime = frametime-diff;
                //debug this!
                if(sleeptime>0)
                   Thread.sleep(5);
                currenttime = newtime;

            } catch (Exception ex) {
            }

            // UI update is run on the Application thread
            Platform.runLater(updater);
            //draw();
        }
    }

    public void draw()
    {
        gc.clearRect(0,0,screenWidth,screenHeight);
        float offsetX = camX - canvasScreenWidth/2;
        float offsetY = camY - canvasScreenHeight/2;
        for(GameObject obj : gameobjects)
        {
            //offset camera
            obj.pos.x -= offsetX;
            obj.pos.y -= offsetY;

            //if(obj.type == GameObject.gameobjecttype_player)
            if(obj == player)
                gc.drawImage(obj.sprite,20,15,40,62,obj.pos.x,obj.pos.y,obj.size.x,obj.size.y);
            else {
                gc.drawImage(obj.sprite, obj.pos.x, obj.pos.y, obj.size.x, obj.size.y);
            }
            //draw collision area
            if(debugMode) {
                gc.setStroke(Color.GREEN);
                if(!obj.bullet)
                    gc.strokeRect(obj.pos.x, obj.pos.y, obj.size.x, obj.size.y);
                else {
                    gc.strokeOval(obj.pos.x, obj.pos.y, obj.size.x, obj.size.y);
                    gc.strokeRect(obj.pos.x, obj.pos.y, obj.size.x, obj.size.y);
                }
            }
            //return camera
            obj.pos.x += offsetX;
            obj.pos.y += offsetY;
        }
    }
}
