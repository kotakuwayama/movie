package movieDetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class rewrite extends Application {

	private static double startX;
	private static double startY;
	private static double endX;
	private static double endY;
	private static double picWitdh;
	private static double picHeight;
	private static String propertiesPath;

	private boolean drawing = false;
	private boolean rectangleDrawn = false;
	private Canvas canvas;
	private GraphicsContext gc;
	private Image tempImage; // 一時的なイメージを保存するための変数

	public static void main(String[] args) throws IOException, InterruptedException {

		//プロパティファイル読み込み
		Path p = Paths.get("").toAbsolutePath();
		String currentPath = p.toString();
		propertiesPath = currentPath + "\\system.properties";
		String email = "";
		try {
			InputStream istream = new FileInputStream(propertiesPath);
			Properties properties = new Properties();
			properties.load(istream);
			email = properties.getProperty("mailAddress");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		//サブスクの支払い判定
		Boolean result = false;
		try {
			result = SubscriptionCheck.subscriptionCheck(email);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		//支払いをしてなければ支払いリンクを生成して表示
		Boolean payJudge = false;
		if (!result) {
			System.out.println("支払いチェック：NG");
			try {
				payJudge = SubscriptionCheck.payment(email);
				if (payJudge) {
					System.out.println("支払い完了しました。");
					//					showAlert(AlertType.INFORMATION, "Information", "支払い完了しました。");
					mainFunction(args);
				} else {
					System.out.println("支払い失敗しました。");
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		//支払い完了していればそのまま利用できる
		else {
			System.out.println("支払いチェック：OK");
			mainFunction(args);
		}
	}

	private static void mainFunction(String[] args) {
		launch(args);

		int rectangleX = (int) startX;
		int rectangleY = (int) startY;
		int rectangleWidth = (int) endX - (int) startX;
		int rectangleHeight = (int) endY - (int) startY;

		//プロパティファイル読み込み
		try {
			// ファイルを読み込んで行ごとに処理
			Path path = Path.of(propertiesPath);
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.contains("rectangleX")) {
					lines.set(i, line.replace(line, "rectangleX" + "=" + rectangleX));
				}
				if (line.contains("rectangleY")) {
					lines.set(i, line.replace(line, "rectangleY" + "=" + rectangleY));
				}
				if (line.contains("rectangleWidth")) {
					lines.set(i, line.replace(line, "rectangleWidth" + "=" + rectangleWidth));
				}
				if (line.contains("rectangleHeight")) {
					lines.set(i, line.replace(line, "rectangleHeight" + "=" + rectangleHeight));
				}
			}

			// ファイルに書き込み（上書き）
			Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
			System.out.println("テキストの置換が完了しました。");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Rectangle Coordinates");

		//プロパティファイル読み込み
		Path p1 = Paths.get("");
		Path p2 = p1.toAbsolutePath();
		String currentPath = p2.toString();

		String videoPath = currentPath + "\\screenshot";
		File folder = new File(currentPath + "\\screenshot");
		File[] files = folder.listFiles();
		String vPath = "";
		for (File file : files) {
			if (file.isFile()) {
				vPath = file.getName();
			}
		}

		// 画像を読み込む
		Image image = new Image(videoPath + "\\" + vPath);
		picWitdh = image.getWidth();
		picHeight = image.getHeight();

		canvas = new Canvas(picWitdh, picHeight);
		gc = canvas.getGraphicsContext2D();
		gc.drawImage(image, 0, 0);

		canvas.setOnMousePressed(this::handleMousePressed);
		canvas.setOnMouseDragged(this::handleMouseDragged);
		canvas.setOnMouseReleased(this::handleMouseReleased);

		StackPane root = new StackPane();
		root.getChildren().add(canvas);

		double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
		double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setWidth(screenWidth);
		primaryStage.setHeight(screenHeight);
		primaryStage.show();
	}

	private void handleMousePressed(MouseEvent event) {
		startX = event.getX();
		startY = event.getY();
		endX = startX;
		endY = startY;
		drawing = true;
		rectangleDrawn = false;

		// クリック時にキャンバスを一時的に保存
		tempImage = canvas.snapshot(null, null);
	}

	private void handleMouseDragged(MouseEvent event) {
		if (drawing && !rectangleDrawn) {
			endX = event.getX();
			endY = event.getY();
			gc.drawImage(tempImage, 0, 0); // 一時的なイメージを復元
			drawRectangle(); // 新しい長方形を描画
		}
	}

	private void handleMouseReleased(MouseEvent event) {
		if (drawing && !rectangleDrawn) {
			endX = event.getX();
			endY = event.getY();

			if (endX > picWitdh - 3) {
				endX = picWitdh - 3;
			}
			if (endY > picHeight - 3) {
				endY = picHeight - 3;
			}

			drawing = false;
			rectangleDrawn = true;
			System.out.println("Start: (" + startX + "," + startY + ")");
			System.out.println("End: (" + endX + "," + endY + ")");
		}
	}

	private void drawRectangle() {
		gc.setStroke(Color.RED);
		double width = Math.abs(endX - startX);
		double height = Math.abs(endY - startY);
		gc.strokeRect(startX, startY, width, height);
	}

	// メッセージボックスを表示するメソッド
	private static void showAlert(AlertType alertType, String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(alertType);
			alert.setTitle(title);
			alert.setHeaderText(null); // ヘッダーを非表示にする場合
			alert.setContentText(message);
			alert.showAndWait();
		});
	}

}
