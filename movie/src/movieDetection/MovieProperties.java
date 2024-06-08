package movieDetection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MovieProperties extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		Path currentDir = Paths.get("").toAbsolutePath();
		File file = new File(currentDir + "\\" + "system.properties");

		String noChangeTime = "";
		String beforeHoldTime = "";
		String afterHoldTime = "";
		String removeBlockTime = "";
		String removeTimeStr = "";
		int removeTime = 0;
		String threshold = "";
		String screenshotTimeStr = "";
		int screenshotTime = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("noChangeTime")) {
					noChangeTime = line.split("=")[1];
				} else if (line.contains("beforeHoldTime")) {
					beforeHoldTime = line.split("=")[1];
				} else if (line.contains("afterHoldTime")) {
					afterHoldTime = line.split("=")[1];
				} else if (line.contains("removeBlockTime")) {
					removeBlockTime = line.split("=")[1];
				} else if (line.contains("removeTime")) {
					removeTimeStr = line.split("=")[1];
					String[] timeParts = removeTimeStr.split(":");
					int hoursInSeconds = Integer.parseInt(timeParts[0]) * 3600;
					int minutesInSeconds = Integer.parseInt(timeParts[1]) * 60;
					int seconds = Integer.parseInt(timeParts[2]);
					removeTime = hoursInSeconds + minutesInSeconds + seconds;
				} else if (line.contains("threshold")) {
					threshold = line.split("=")[1];
				} else if (line.contains("screenshotTime")) {
					screenshotTimeStr = line.split("=")[1];
					String[] timeParts = screenshotTimeStr.split(":");
					int hoursInSeconds = Integer.parseInt(timeParts[0]) * 3600;
					int minutesInSeconds = Integer.parseInt(timeParts[1]) * 60;
					int seconds = Integer.parseInt(timeParts[2]);
					screenshotTime = hoursInSeconds + minutesInSeconds + seconds;
				}
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//noChangeTime
		ImageView imageView = new ImageView(new Image("C:\\pleiades\\2023-06\\workspace\\movie\\noChangeTime.png"));
		Label noChangeTimeLabel = new Label("■何秒以上変化がなかったら切り取り処理を行うか ");
		HBox noChangeTimeHBox = new HBox(getSecondCombBox(), getSecondLabel(), imageView);
		ComboBox<Integer> noChangeTimeComb = (ComboBox<Integer>) noChangeTimeHBox.getChildren().get(0);
		noChangeTimeComb.setValue(Integer.valueOf(noChangeTime));
		VBox noChangeTimeVBox = new VBox(noChangeTimeLabel, noChangeTimeHBox);
		noChangeTimeVBox.setPadding(new Insets(30, 0, 30, 30));

		//beforeHoldTime
		Label beforeTimeLabel = new Label("■切り取り処理をするときに開始の何秒前まで取得するか ");
		HBox beforeTimeHBox = new HBox(getSecondCombBox(), getSecondLabel());
		ComboBox<Integer> beforeTimeComb = (ComboBox<Integer>) beforeTimeHBox.getChildren().get(0);
		beforeTimeComb.setValue(Integer.valueOf(beforeHoldTime));
		VBox beforeTimeVBox = new VBox(beforeTimeLabel, beforeTimeHBox);
		beforeTimeVBox.setPadding(new Insets(30, 0, 30, 30));

		//afterHoldTime
		Label afterTimeLabel = new Label("■切り取り処理をするときに終わりの何秒後まで取得するか ");
		HBox afterTimeHBox = new HBox(getSecondCombBox(), getSecondLabel());
		ComboBox<Integer> afterTimeComb = (ComboBox<Integer>) afterTimeHBox.getChildren().get(0);
		afterTimeComb.setValue(Integer.valueOf(afterHoldTime));
		VBox afterTimeVBox = new VBox(afterTimeLabel, afterTimeHBox);
		afterTimeVBox.setPadding(new Insets(30, 0, 30, 30));

		//removeBlockTime
		Label removeBlockTimeLabel = new Label("■何秒以上のブロックが1個でもあれば利用するか ");
		HBox removeBlockTimeHBox = new HBox(getSecondCombBox(), getSecondLabel());
		ComboBox<Integer> removeBlockTimeComb = (ComboBox<Integer>) removeBlockTimeHBox.getChildren().get(0);
		removeBlockTimeComb.setValue(Integer.valueOf(removeBlockTime));
		VBox removeBlockTimeVBox = new VBox(removeBlockTimeLabel, removeBlockTimeHBox);
		removeBlockTimeVBox.setPadding(new Insets(30, 0, 30, 30));

		//removeTime
		Label removeTimeLabel = new Label("■何秒以下の動画を削除するか (↑で設定した開始終了の時間も含む)");
		HBox removeTimeHBox = new HBox(getSecondCombBox(), getSecondLabel());
		ComboBox<Integer> removeTimeComb = (ComboBox<Integer>) removeTimeHBox.getChildren().get(0);
		removeTimeComb.setValue(Integer.valueOf(removeTime));
		VBox removeTimeVBox = new VBox(removeTimeLabel, removeTimeHBox);
		removeTimeVBox.setPadding(new Insets(30, 0, 30, 30));

		//threshold
		Label thresholdLabel = new Label("■検知する閾値");
		HBox thresholdHBox = new HBox(getSecondCombBox(), getNoLabel());
		ComboBox<Integer> thresholdComb = (ComboBox<Integer>) thresholdHBox.getChildren().get(0);
		thresholdComb.setValue(Integer.valueOf(threshold));
		VBox thresholdVBox = new VBox(thresholdLabel, thresholdHBox);
		thresholdVBox.setPadding(new Insets(30, 0, 30, 30));

		//screenshotTime
		Label screenshotTimeLabel = new Label("■スクリーンショットを取る動画の時間");
		HBox screenshotTimeHBox = new HBox(getSecondCombBox(), getSecondLabel());
		ComboBox<Integer> screenshotTimeComb = (ComboBox<Integer>) screenshotTimeHBox.getChildren().get(0);
		screenshotTimeComb.setValue(Integer.valueOf(screenshotTime));
		VBox screenshotTimeVBox = new VBox(screenshotTimeLabel, screenshotTimeHBox);
		screenshotTimeVBox.setPadding(new Insets(30, 0, 30, 30));

		Button updateBotton = new Button("保存して終了");
		updateBotton.setStyle("-fx-font-size: 20px;");
		updateBotton.setMinWidth(100);
		updateBotton.setMinHeight(50);
		HBox updateHbox = new HBox(updateBotton);
		updateHbox.setAlignment(Pos.CENTER);

		//保存してファイルに書き込む
		updateBotton.setOnAction(event -> {

			ComboBox<Integer> noChangeTimeKeep = (ComboBox<Integer>) noChangeTimeHBox.getChildren().get(0);
			ComboBox<Integer> beforeTimeKeep = (ComboBox<Integer>) beforeTimeHBox.getChildren().get(0);
			ComboBox<Integer> afterTimeKeep = (ComboBox<Integer>) afterTimeHBox.getChildren().get(0);
			ComboBox<Integer> removeBlockTimeKeep = (ComboBox<Integer>) removeBlockTimeHBox.getChildren().get(0);
			ComboBox<Integer> removeTimeKeep = (ComboBox<Integer>) removeTimeHBox.getChildren().get(0);
			ComboBox<Integer> thresholdKeep = (ComboBox<Integer>) thresholdHBox.getChildren().get(0);
			ComboBox<Integer> screenshotTimeKeep = (ComboBox<Integer>) screenshotTimeHBox.getChildren().get(0);

			try {
				// ファイルを一時的に読み込むためのBufferedReaderを作成
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				StringBuilder content = new StringBuilder();

				// ファイルの内容を一行ずつ読み込んでcontentに追加
				while ((line = reader.readLine()) != null) {
					if (line.contains("noChangeTime")) {
						content.append("noChangeTime=" + noChangeTimeKeep.getValue()).append("\n");

					} else if (line.contains("beforeHoldTime")) {
						content.append("beforeHoldTime=" + beforeTimeKeep.getValue()).append("\n");

					} else if (line.contains("afterHoldTime")) {
						content.append("afterHoldTime=" + afterTimeKeep.getValue()).append("\n");

					} else if (line.contains("removeBlockTime")) {
						content.append("removeBlockTime=" + removeBlockTimeKeep.getValue()).append("\n");

					} else if (line.contains("removeTime")) {
						// 秒数を時間、分、秒に分割
						int hours = removeTimeKeep.getValue() / 3600;
						int minutes = (removeTimeKeep.getValue() % 3600) / 60;
						int seconds = removeTimeKeep.getValue() % 60;
						// 各要素を2桁の数字に整形
						String hoursStr = String.format("%02d", hours);
						String minutesStr = String.format("%02d", minutes);
						String secondsStr = String.format("%02d", seconds);
						// "00:00:00" 形式に結合
						String formattedTime = hoursStr + ":" + minutesStr + ":" + secondsStr;
						content.append("removeTime=" + formattedTime).append("\n");

					} else if (line.contains("threshold")) {
						content.append("threshold=" + thresholdKeep.getValue()).append("\n");

					} else if (line.contains("screenshotTime")) {
						// 秒数を時間、分、秒に分割
						int hours = screenshotTimeKeep.getValue() / 3600;
						int minutes = (screenshotTimeKeep.getValue() % 3600) / 60;
						int seconds = screenshotTimeKeep.getValue() % 60;
						// 各要素を2桁の数字に整形
						String hoursStr = String.format("%02d", hours);
						String minutesStr = String.format("%02d", minutes);
						String secondsStr = String.format("%02d", seconds);
						// "00:00:00" 形式に結合
						String formattedTime = hoursStr + ":" + minutesStr + ":" + secondsStr;

						content.append("screenshotTime=" + formattedTime).append("\n");

					} else {
						content.append(line).append("\n");;
					}
				}
				reader.close();

				// ファイルを書き込むためのBufferedWriterを作成
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(content.toString());
				writer.close();

				System.out.println("ファイルの更新が完了しました。");

				primaryStage.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		VBox mainVbox = new VBox(noChangeTimeVBox, beforeTimeVBox, afterTimeVBox, removeBlockTimeVBox, removeTimeVBox, thresholdVBox, screenshotTimeVBox, updateHbox);

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(mainVbox);
		scrollPane.setFitToWidth(true);

		Scene scene = new Scene(scrollPane);
		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public Label getSecondLabel() {
		Label secondLabel = new Label(" 秒 ");
		secondLabel.setPadding(new Insets(5, 0, 0, 0));
		return secondLabel;
	}

	public Label getNoLabel() {
		Label secondLabel = new Label("");
		secondLabel.setPadding(new Insets(5, 0, 0, 0));
		return secondLabel;
	}

	public ComboBox<Integer> getSecondCombBox() {
		ObservableList<Integer> options = FXCollections.observableArrayList();
		for (int i = 1; i <= 100; i++) {
			options.add(i);
		}
		ComboBox<Integer> secondBox = new ComboBox<>();
		secondBox.getItems().addAll(options);
		secondBox.setValue(1);
		return secondBox;
	}

}
