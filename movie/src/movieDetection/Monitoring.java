package movieDetection;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Monitoring extends Application {
	boolean startFlg = true;
	private TextArea textArea;
	private long tmpFileSize = 0;

	@Override
	public void start(Stage primaryStage) {
		textArea = new TextArea();
		textArea.setEditable(false);
		StackPane root = new StackPane();
		root.getChildren().add(textArea);
		Scene scene = new Scene(root, 400, 300);
		primaryStage.setTitle("ファイル監視");

		// 画面サイズを取得
		double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
		double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

		// Stageの位置を設定（画面右上）
		primaryStage.setX(screenWidth - 410);
		primaryStage.setY(0);

		primaryStage.setScene(scene);
		primaryStage.show();

		// フォルダを監視するスレッドの開始
		startChecking("監視用フォルダ");
	}

	private void startChecking(String folderPath) {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

		executorService.scheduleAtFixedRate(() -> {
			Platform.runLater(() -> {

				if (startFlg) {
					startFlg = false;
					textArea.appendText(getToDay() + " - 監視開始\n");
				}

				// フォルダ内のファイルを取得
				List<File> fileList = Arrays.asList(new File(folderPath).listFiles());

				// フォルダ内にファイルが存在するかチェック
				boolean filesExist = fileList != null && !fileList.isEmpty();

				//拡張子を取得
				int index = 0;
				String extension = "";
				if (fileList.size() > 0) {
					String fileName = fileList.get(0).getName();
					index = fileName.lastIndexOf('.');
					if (index != -1) {
						extension = fileName.substring(index + 1);
					}
				}
				//サイズが前と比べて同じ かつ 拡張子がダウンロード中を示すcrdownloadでなければ処理開始
				if (filesExist && tmpFileSize == fileList.get(0).length() && !extension.equals("crdownload")) {
					executorService.shutdown();
					textArea.appendText(getToDay() + " - ファイルを検知しました\n");
					Process process = null;

					try {
						String command3 = "手順3_コマンド生成.bat";
						ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "/wait", "\"\"", command3);
						process = pb.start();
						int result = process.waitFor();

						if (result == 0) {
							String command4 = "手順4_トリミング.bat";
							pb = new ProcessBuilder("cmd", "/c", "start", "/wait", "\"\"", command4);
							pb.start();
							result = process.waitFor();
							textArea.appendText(getToDay() + " - 切り出し完了\n");
							process.destroy();
						}

					} catch (Exception e) {
						textArea.appendText(getToDay() + " - エラー発生\n");
						textArea.appendText(getToDay() + " - " + e.getStackTrace() + "\n");
					} finally {
						textArea.appendText(getToDay() + " - 監視終了\n");
						if (process != null) {
							process.destroy();
						}
					}

				} else {
					textArea.appendText(getToDay() + " - ファイル監視中 動画ファイルなし \n");
				}
				//現在のサイズを保存
				if (fileList.size() > 0) {
					tmpFileSize = fileList.get(0).length();
				}

			});
		}, 0, 5, TimeUnit.SECONDS); // チェック間隔を5秒に設定
	}

	public static String getToDay() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return now.format(formatter);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
