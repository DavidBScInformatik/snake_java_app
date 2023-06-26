package application;

import java.net.URL;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SnakeGame extends Application {

	public enum Direction {
		UP, DOWN, LEFT, RIGHT;
	}

	// Fenster
	private static Stage window;

	public static final int BLOCK_SIZE = 20;
	public static final int GAME_WIDTH = 30 * BLOCK_SIZE;
	public static final int GAME_HEIGHT = 20 * BLOCK_SIZE;

	private static double speed = 0.2;
	private static boolean isEndless = false;

	private Direction direction = Direction.RIGHT;
	private boolean moved = false;
	private boolean running = false;

	private Timeline timeline = new Timeline();

	private ObservableList<Node> snake;

	private MediaPlayer mediaplayer;
	private Slider volumeSlider = new Slider();
	private Label volumeLabel = new Label("1.0");

	private int score = 0;
	private Label scoreLabel = new Label("Score: " + score);
	private Label infoLabel = new Label("Drücke ESC für Exit und SPACE für Pause");

	// -------------
	// --Gametszene--
	// --------------

	private Pane creatGameContent() {
		Pane root = new Pane();
		root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

		root.setStyle(""
				// + "-fx-background-image: url(/images/gras.png);"
				+ "-fx-background-size: 20 20;" + "-fx-background-repeat: repeat;" + "-fx-border-color: black;"
				+ "-fx-border-style: solid;" + "-fx-border-width: 2;");

		// Schlange
		Group snakeBody = new Group();
		snake = snakeBody.getChildren();

		// Essen
		javafx.scene.shape.Rectangle food = new javafx.scene.shape.Rectangle(BLOCK_SIZE, BLOCK_SIZE);
		Image foodImage = new Image(getClass().getResourceAsStream("/images/food.png"));
		ImagePattern imagePattern = new ImagePattern(foodImage);
		food.setFill(imagePattern);

		creatRandomFood(food);

		// Animation
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(speed), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (!running) {
					return;
				}

				boolean toRemove = snake.size() > 1;
				Node tail; // Kopf bzw Ende der Schlange

				if (toRemove) {
					tail = snake.remove(snake.size() - 1);
				} else {
					tail = snake.get(0);
				}

				double tailX = tail.getTranslateX();
				double tailY = tail.getTranslateY();

				switch (direction) {
					case UP:
						tail.setTranslateX(snake.get(0).getTranslateX());
						tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
						break;
					case DOWN:
						tail.setTranslateX(snake.get(0).getTranslateX());
						tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
						break;
					case LEFT:
						tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
						tail.setTranslateY(snake.get(0).getTranslateY());
						break;
					case RIGHT:
						tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
						tail.setTranslateY(snake.get(0).getTranslateY());
						break;
					default:
						break;
				}

				moved = true;

				if (toRemove) {
					snake.add(0, tail);
				}

				for (Node rect : snake) {
					if (rect != tail && tail.getTranslateX() == rect.getTranslateX()
							&& tail.getTranslateY() == rect.getTranslateY()) {
						score = 0;
						scoreLabel.setText("Score: " + score);
						restartGame();
						break;
					}
				}

				// Wand oder nicht
				if (isEndless) {
					gameIsEndless(tail, root);
				} else {
					gameIsNotEndless(tail, food);
				}

				// Food einsammeln
				if (tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY()) {
					creatRandomFood(food);
					score += 20;
					scoreLabel.setText("Score: " + score);

					Rectangle rectangle = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
					rectangle.setTranslateX(tailX);
					rectangle.setTranslateY(tailY);
					snake.add(rectangle);
				}

			}
		});

		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Timeline.INDEFINITE);

		// ScoreLabel
		scoreLabel.setFont(Font.font("Arial", 30));
		scoreLabel.setTranslateX(GAME_WIDTH / 2);

		// InfoLabel;
		infoLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 10));

		root.getChildren().addAll(food, snakeBody, scoreLabel, infoLabel);

		return root;
	}

	// random foot spawn
	private void creatRandomFood(Node food) {
		food.setTranslateX((int) (Math.random() * (GAME_WIDTH - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
		food.setTranslateY((int) (Math.random() * (GAME_HEIGHT - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
	}

	private void gameIsEndless(Node tail, Parent root) {
		root.setStyle(""
				// + "-fx-background-image: url(/images/gras.png);"
				+ "-fx-background-size: 20 20;" + "-fx-background-repeat: repeat;");

		if (tail.getTranslateX() < 0) {
			tail.setTranslateX(GAME_WIDTH - BLOCK_SIZE);
		}

		if (tail.getTranslateX() >= GAME_WIDTH) {
			tail.setTranslateX(0);
		}

		if (tail.getTranslateY() < 0) {
			tail.setTranslateY(GAME_HEIGHT - BLOCK_SIZE);
		}

		if (tail.getTranslateY() >= GAME_HEIGHT) {
			tail.setTranslateY(0);
		}

	}

	private void gameIsNotEndless(Node tail, Node food) {
		if (tail.getTranslateX() < 0 || tail.getTranslateX() >= GAME_WIDTH || tail.getTranslateY() < 0
				|| tail.getTranslateY() >= GAME_HEIGHT) {
			score = 0;
			scoreLabel.setText("Score: " + score);
			restartGame();
			creatRandomFood(food);
		}
	}

	// START GAME

	private void startGame() {
		Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
		snake.add(head);
		timeline.play();
		running = true;
	}

	// RESTART GAME

	private void restartGame() {
		stopGame();
		startGame();
	}

	// STOP GAME
	private void stopGame() {
		running = false;
		timeline.stop();
		snake.clear();
	}

	// -------------
	// --Startszene--
	// --------------

	private BorderPane creatStartScreen() {
		BorderPane root = new BorderPane();

		// Start

		Label startLabel = new Label("");
		Image image = new Image(getClass().getResourceAsStream("/images/snake.png"));
		ImageView imageView = new ImageView(image);
		startLabel.setGraphic(imageView);

		Button startButton = new Button("Start");
		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				Scene scene = new Scene(creatGameContent());
				keypressed(scene);
				window.setScene(scene);
				window.setResizable(false);
				window.setTitle("Snake Game");
				window.show();
				startGame();
			}
		});

		VBox vBox = new VBox(30);
		vBox.setAlignment(Pos.CENTER);
		vBox.getChildren().addAll(startLabel, startButton);
		root.setTop(vBox);

		// Exit
		Button exitButton = new Button("Exit");
		BorderPane.setAlignment(exitButton, Pos.CENTER);
		BorderPane.setMargin(exitButton, new Insets(20));
		root.setBottom(exitButton);

		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Platform.exit();
			}
		});

		// Einstellungen
		Button gameSpeed = new Button("Schwierigkeitsstufe");
		Button endlessOrNot = new Button("Rand /");
		Label speedLabel = new Label("Leicht");

		gameSpeed.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (speed == 0.2) {
					SnakeGame.speed = 0.15;
					speedLabel.setText("Mittel");
				} else if (speed == 0.15) {
					SnakeGame.speed = 0.09;
					speedLabel.setText("Schwer");
				} else if (speed == 0.09) {
					SnakeGame.speed = 0.2;
					speedLabel.setText("Leicht");
				}
			}
		});

		endlessOrNot.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (SnakeGame.isEndless) {
					endlessOrNot.setText("️Rand /");
					SnakeGame.isEndless = false;
				} else {
					endlessOrNot.setText("Rand X");
					SnakeGame.isEndless = true;
				}
			}
		});

		HBox hBox = new HBox(10);
		hBox.setAlignment(Pos.CENTER);
		hBox.getChildren().addAll(gameSpeed, endlessOrNot, speedLabel);
		root.setLeft(hBox);
		BorderPane.setMargin(hBox, new Insets(20));

		// Musik
		Button muteButton = new Button("",
				new ImageView(new Image(getClass().getResourceAsStream("/images/mute.png"))));
		Button unmuteButton = new Button("",
				new ImageView(new Image(getClass().getResourceAsStream("/images/unmute.png"))));

		muteButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				mediaplayer.pause();
			}
		});

		unmuteButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				mediaplayer.play();
			}
		});

		VBox vBox3 = new VBox(5);
		vBox3.getChildren().addAll(volumeSlider, volumeLabel);
		volumeSlider.setOrientation(Orientation.VERTICAL);

		VBox vBox2 = new VBox(5);
		vBox2.setAlignment(Pos.CENTER_RIGHT);
		vBox2.getChildren().addAll(muteButton, unmuteButton, new Separator(), vBox3);

		root.setRight(vBox2);
		BorderPane.setMargin(vBox2, new Insets(20));

		return root;
	}

	private void playMusik(String title) {
		String musicFile = title;
		URL fileUrl = getClass().getResource(musicFile);

		Media media = new Media(fileUrl.toString());
		mediaplayer = new MediaPlayer(media);
		mediaplayer.stop();
		mediaplayer.setCycleCount(MediaPlayer.INDEFINITE);
	}

	// Tastatur Interaktion

	private void keypressed(Scene scene) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				// System.out.println("Taste: " + arg0.getCode());
				if (!moved) {
					return;
				}

				switch (arg0.getCode()) {
					case W:
					case UP:
						if (direction != Direction.DOWN) {
							direction = Direction.UP;
							break;
						}
					case S:
					case DOWN:
						if (direction != Direction.UP) {
							direction = Direction.DOWN;
							break;
						}
					case A:
					case LEFT:
						if (direction != Direction.RIGHT) {
							direction = Direction.LEFT;
							break;
						}
					case D:
					case RIGHT:
						if (direction != Direction.LEFT) {
							direction = Direction.RIGHT;
							break;
						}
					case SPACE:
						timeline.pause();
						scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
							@Override
							public void handle(KeyEvent arg0) {
								if (arg0.getCode() == KeyCode.SPACE) {
									timeline.playFromStart();
									keypressed(scene);
								} else if (arg0.getCode() == KeyCode.ESCAPE) {
									Platform.exit();
								}
							}
						});
						break;
					case ESCAPE:
						Platform.exit();
						defaul: break;
				}
				moved = false;

			};
		});
	}

	@Override
	public void init() throws Exception {
		String musikFile = "/music/snakeMusic.mp3";
		playMusik(musikFile);

		volumeSlider.setValue(mediaplayer.getVolume() * 100);
		volumeSlider.setPrefWidth(80);
		volumeSlider.setShowTickLabels(true);

		volumeSlider.valueProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable arg0) {
				mediaplayer.setVolume(volumeSlider.getValue() / 100);
				volumeLabel.setText(String.format("%.2f", volumeSlider.getValue() / 100));
			}
		});
	}

	@Override
	public void start(Stage primaryStage) {
		Parent root = creatStartScreen();

		primaryStage.setResizable(false);
		primaryStage.setTitle("SNAKE");
		window = primaryStage;

		window.setScene(new Scene(root, GAME_WIDTH, GAME_HEIGHT));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
