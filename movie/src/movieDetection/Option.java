package movieDetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Option extends Application {

	//動画番号と開始終了時間を保持
	static Map<String, String> map = new HashMap<String, String>();
	static Map<String, Character> maxAlphabetMap = new HashMap<>();
	//動画ファイル名一覧
	static List<String> fileNameList = new ArrayList<String>();
	//動画番号プルダウhン
	static ComboBox<String> numBox = new ComboBox<>();

	static String currentPath = "";

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		currentPath = Paths.get("").toAbsolutePath().toString();

		String CSS_FILE = "\\src\\main\\resources\\style.css";
		CSS_FILE = ("file:///" + currentPath + CSS_FILE).replace("\\", "/");

		//動画番号と時間を取得してmapに追加。ファイル名一覧も取得
		getNumberList();

		//元動画情報取得
		File mainFile = new File(currentPath + "\\" + "input_main");
		File[] mainFiles = mainFile.listFiles();
		String mainVideoPath = mainFiles[0].toString();

		//元動画の再生時間を取得
		String movieLength = null;
		try {
			// ffmpegコマンドを作成
			ProcessBuilder processBuilder = new ProcessBuilder(
					"ffmpeg", "-i", mainVideoPath);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			// プロセスの出力を読み取る
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			Pattern pattern = Pattern.compile("Duration: (\\d{2}:\\d{2}:\\d{2}\\.\\d{2})");
			while ((line = reader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					movieLength = matcher.group(1);
					break;
				}
			}
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		int index = movieLength.indexOf(".");
		movieLength = movieLength.substring(0, index);
		int inputLength = convertToSeconds(movieLength);

		//********************番号指定切り取り********************
		//何秒前プルダウン
		ComboBox<String> beforeTimeComb = new ComboBox<>();
		for (int i = 1; i <= 20; i++) {
			beforeTimeComb.getItems().addAll(String.valueOf(i));
		}
		beforeTimeComb.setValue("1");

		//何秒後プルダウン
		ComboBox<String> afterTimeComb = new ComboBox<>();
		for (int i = 1; i <= 20; i++) {
			afterTimeComb.getItems().addAll(String.valueOf(i));
		}
		afterTimeComb.setValue("1");

		//切り取りボタン
		VBox cutVBox = new VBox();
		Button cutBotton = new Button("切り取り");
		cutBotton.setStyle("-fx-font-size: 20px;");
		cutBotton.getStyleClass().add("stylish-button");
		cutBotton.setMinHeight(50);
		CheckBox deleteCheck = new CheckBox("元動画を削除する");
		deleteCheck.setPadding(new Insets(10, 10, 10, 10));
		cutVBox.getChildren().addAll(cutBotton, deleteCheck);

		//動画番号のプルダウンを変更するとその番号に紐づく時間が表示される
		Label timeText = new Label("");
		timeText.setText("開始終了時間⇒  " + map.get(numBox.getValue()).replace("-", ":").replace("_", "～"));

		HBox numberTimeHBox = new HBox(10,
				new Label("動画番号: "), numBox,
				timeText,
				new Label("何秒前まで取得するか"), beforeTimeComb, new Label("秒"),
				new Label("何秒後まで取得するか"), afterTimeComb, new Label("秒"),
				cutVBox);
		//余白設定
		HBox.setMargin(timeText, new Insets(0, 20, 0, 20));
		HBox.setMargin(cutVBox, new Insets(0, 20, 0, 20));

		Label numberTimeLabel = new Label("■動画番号を指定して切り抜く前後の時間を入力");
		numberTimeLabel.setStyle("-fx-font-size: 20px;");
		VBox numberTimeVBox = new VBox(10, numberTimeLabel, numberTimeHBox);
		numberTimeVBox.setPadding(new Insets(100, 0, 0, 100));

		//切り取りボタンを押した時のイベント
		cutBotton.setOnAction(event -> {

			String number = numBox.getValue();

			String targetTime1 = map.get(number).split("_")[0];
			String targetTime2 = map.get(number).split("_")[1];
			int beforeTime = Integer.valueOf(beforeTimeComb.getValue());
			int afterTime = Integer.valueOf(afterTimeComb.getValue());

			//元動画から指定した秒数分だけ出力時間を計算
			//開始位置の計算
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss");
			LocalTime time1 = LocalTime.parse(targetTime1, formatter);
			long ssConvert = time1.toSecondOfDay();
			long startss = ssConvert - beforeTime;
			String startTime = convertToHHMMSS(startss);

			//終了位置の計算
			LocalTime time2 = LocalTime.parse(targetTime2, formatter);
			ssConvert = time2.toSecondOfDay();
			long emdss = ssConvert + afterTime;
			String endTime = convertToHHMMSS(emdss);

			// startTime と endTime を秒に変換し、抽出範囲の秒数取得
			int startTimeInSeconds = convertToSeconds(startTime);
			int endTimeInSeconds = convertToSeconds(endTime);
			int durationInSeconds = endTimeInSeconds - startTimeInSeconds;

			if (0 > startTimeInSeconds) {
				showAlert(AlertType.ERROR, "Information", " 開始時刻がマイナスになっています。");
			} else if (inputLength < startTimeInSeconds) {
				showAlert(AlertType.ERROR, "Information", " 開始時刻が元動画の再生時間を超えています。");
			} else if (inputLength < endTimeInSeconds) {
				showAlert(AlertType.ERROR, "Information", " 終了時刻が元動画の再生時間を超えています。");
			} else {
				// 長さを HH:mm:ss 形式に変換
				String duration = convertToHHMMSS(durationInSeconds);

				//()の中のアルファベットを決定
				String fileName = "";
				for (String str : fileNameList) {
					if (str.contains(map.get(number))) {
						fileName = str;
					}
				}
				File inputFile = new File(mainVideoPath);

				//切り取った時間でファイル名を作る
				fileName = fileName.replaceAll("\\d{2}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}", "");
				int index1 = fileName.indexOf(".");
				StringBuilder sb = new StringBuilder(fileName);
				sb.insert(index1, startTime.replace(":", "-") + "_" + endTime.replace(":", "-"));

				//アルファベット部分はa⇒b⇒c... の順で作るため、()の中の文字列を取得し、アルファベットの命名を探す。
				String numberPart = "";
				String alphabetPart = "";
				String ans = "";
				Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
				Matcher matcher = pattern.matcher(sb.toString());
				if (matcher.find()) {
					// 数字部分とアルファベット部分を抽出
					numberPart = matcher.group(1).replaceAll("[^\\d]", "");
					alphabetPart = matcher.group(1).replaceAll("\\d", "");
					Character alphabet = maxAlphabetMap.get(numberPart);

					//初回はアルファベットがないので"a"固定。それ以降はアルファベット順の文字列を取得
					if (alphabet == null) {
						ans = "a";
					} else {
						ans = getNextAlphabet(alphabet);
					}
				} else {
					ans = "a";
					System.out.println("No match found");
				}

				getMaxAlphabetForNumbers(numberPart + ans);

				int index2 = sb.indexOf(")");
				sb.insert(index2, ans);
				String lastName = sb.toString().replace(alphabetPart, "");

				File outputFile = new File(currentPath + "\\" + "trim_main" + "\\" + lastName);

				// FFmpegコマンド
				String command = String.format(
						"ffmpeg -noaccurate_seek -ss %s -i \"%s\" -t %s -c copy \"%s\"",
						startTime, inputFile, duration, outputFile);

				try {
					Process process = Runtime.getRuntime().exec(command);

					int exitCode = process.waitFor();
					if (exitCode == 0) {

						//削除チェックボックスがONなら元動画を削除
						String deleteName = "";
						if (deleteCheck.isSelected()) {
							String fileTmp = map.get(number);
							for (String str : fileNameList) {
								if (str.contains(fileTmp)) {
									File deleteFile = new File(currentPath + "\\" + "trim_main" + "\\" + str);
									if (deleteFile.delete()) {
										String selectedValue = numBox.getValue();
										numBox.getItems().remove(selectedValue);
										map.remove(number);
										deleteName = str;
										break;
									}
								}
							}
						}

						//生成した動画分をプルダウンにセット
						String addKey = "";
						String addValue = startTime.replace(":", "-") + "_" + endTime.replace(":", "-");
						matcher = pattern.matcher(lastName.toString());
						if (matcher.find()) {
							addKey = matcher.group(1);
						}
						map.put(addKey, addValue);
						numBox.getItems().add(addKey);
						numBox.setValue(addKey);
						fileNameList.add(lastName);
						fileNameList.remove(deleteName);
						//降順に並び替え
						List<String> items = numBox.getItems();
						List<String> sortedItems = items.stream()
								.sorted((a, b) -> Integer.compare(extractNumber(a), extractNumber(b)))
								.collect(Collectors.toList());
						numBox.setItems(FXCollections.observableArrayList(sortedItems));

						if (deleteCheck.isSelected()) {
							showAlert(AlertType.INFORMATION, "Information", deleteName + "を削除しました" + "\n" + lastName + "を出力しました");
						} else {
							showAlert(AlertType.INFORMATION, "Information", lastName + "を出力しました");
						}

					} else {
						// エラーストリームを読み取る
						BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
						String line;
						StringBuilder errorMessage = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							errorMessage.append(line).append("\n");
						}
						showAlert(AlertType.ERROR, "Error", "FFmpeg error: " + errorMessage.toString());
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					showAlert(AlertType.ERROR, "Exception", e.getMessage());
				}
			}

		});

		//動画番号のプルダウン
		numBox.setOnAction(event -> {
			if (map.containsKey(numBox.getValue())) {
				timeText.setText("開始終了時間⇒  " + map.get(numBox.getValue()).replace("-", ":").replace("_", "～"));
				beforeTimeComb.setValue(beforeTimeComb.getValue());
				afterTimeComb.setValue(afterTimeComb.getValue());
			}

			//プルダウンで選んだ対象の動画ファイルを特定
			//			String videoPath = "";
			//			for (String fileName : fileNameList) {
			//				if (fileName.contains(map.get(numBox.getValue()))) {
			//					videoPath = currentPath + "\\" + "trim_main" + "\\" + fileName;
			//				}
			//			}
			//
			//			String startTime = map.get(numBox.getValue()).split("_")[0].replace("-", ":");
			//			int number = Integer.valueOf(beforeTimeComb.getValue());
			//			int invertedNumber = -number; //～秒前は過去なのでマイナスに変換
			//			startTime = addSecondsToTimestamp(startTime, invertedNumber);
			//			System.out.println(videoPath);
			//			System.out.println(startTime);
			//			System.out.println(end);

		});

		//********************任意時間指定切り取り********************
		// 時間入力用コンボボックス
		ComboBox<Integer> hoursComboBox1 = new ComboBox<>();
		for (int i = 0; i < 24; i++) {
			hoursComboBox1.getItems().add(i);
			hoursComboBox1.setValue(0);
		}
		// 分入力用コンボボックス
		ComboBox<Integer> minutesComboBox1 = new ComboBox<>();
		for (int i = 0; i < 60; i++) {
			minutesComboBox1.getItems().add(i);
			minutesComboBox1.setValue(0);
		}
		// 秒入力用コンボボックス
		ComboBox<Integer> secondsComboBox1 = new ComboBox<>();
		for (int i = 0; i < 60; i++) {
			secondsComboBox1.getItems().add(i);
			secondsComboBox1.setValue(0);
		}

		// 時間入力用コンボボックス
		ComboBox<Integer> hoursComboBox2 = new ComboBox<>();
		for (int i = 0; i < 24; i++) {
			hoursComboBox2.getItems().add(i);
			hoursComboBox2.setValue(0);
		}
		// 分入力用コンボボックス
		ComboBox<Integer> minutesComboBox2 = new ComboBox<>();
		for (int i = 0; i < 60; i++) {
			minutesComboBox2.getItems().add(i);
			minutesComboBox2.setValue(0);
		}
		// 秒入力用コンボボックス
		ComboBox<Integer> secondsComboBox2 = new ComboBox<>();
		for (int i = 0; i < 60; i++) {
			secondsComboBox2.getItems().add(i);
			secondsComboBox2.setValue(0);
		}

		//切り取りボタン
		Button cutAnyBotton = new Button("切り取り");
		cutAnyBotton.setStyle("-fx-font-size: 20px;");
		cutAnyBotton.getStyleClass().add("stylish-button");
		cutAnyBotton.setMinHeight(50);
		//余白設定
		HBox.setMargin(cutAnyBotton, new Insets(0, 20, 0, 20));

		Label anyTimeLabel = new Label("■任意の時間を入力");
		anyTimeLabel.setStyle("-fx-font-size: 20px;");

		HBox anyTimeHBox = new HBox(5,
				hoursComboBox1, new Label("時間: "),
				minutesComboBox1, new Label("分: "),
				secondsComboBox1, new Label("秒: ～"),
				hoursComboBox2, new Label("時間: "),
				minutesComboBox2, new Label("分: "),
				secondsComboBox2, new Label("秒: "),
				cutAnyBotton);
		VBox anyTimeVBox = new VBox(10, anyTimeLabel, anyTimeHBox);
		anyTimeVBox.setPadding(new Insets(100, 0, 0, 100));

		//切り取りボタンを押した時のイベント
		cutAnyBotton.setOnAction(event -> {

			int beforeHH = Integer.valueOf(hoursComboBox1.getValue());
			int beforeMM = Integer.valueOf(minutesComboBox1.getValue());
			int beforeSS = Integer.valueOf(secondsComboBox1.getValue());
			String startTime = convertToHHMMSSbyHHMMSS(beforeHH, beforeMM, beforeSS);

			int afterHH = Integer.valueOf(hoursComboBox2.getValue());
			int afterMM = Integer.valueOf(minutesComboBox2.getValue());
			int afterSS = Integer.valueOf(secondsComboBox2.getValue());
			String endTime = convertToHHMMSSbyHHMMSS(afterHH, afterMM, afterSS);

			// startTime と endTime を秒に変換し、抽出範囲の秒数取得
			int startTimeInSeconds = convertToSeconds(startTime);
			int endTimeInSeconds = convertToSeconds(endTime);
			int durationInSeconds = endTimeInSeconds - startTimeInSeconds;
			if (durationInSeconds < 0) {
				showAlert(AlertType.ERROR, "Information", "開始時刻 < 終了時刻となるように入力してください。");
			} else if (inputLength < startTimeInSeconds) {
				showAlert(AlertType.ERROR, "Information", " 開始時刻が元動画の再生時間を超えています。");
			} else if (inputLength < endTimeInSeconds) {
				showAlert(AlertType.ERROR, "Information", " 終了時刻が元動画の再生時間を超えています。");
			} else {
				// 長さを HH:mm:ss 形式に変換
				String duration = convertToHHMMSS(durationInSeconds);

				//接頭文字のファイル名を確定
				Map<String, Integer> diffMap = new HashMap<String, Integer>();
				for (String dirFileName : fileNameList) {
					int startIndex = dirFileName.indexOf(")");
					int endIndex = dirFileName.indexOf("_");
					String dirStart = dirFileName.substring(startIndex + 1, endIndex).replace("-", ":");
					//mapのkey⇒接頭文字
					startIndex = dirFileName.indexOf("(");
					endIndex = dirFileName.indexOf(")");
					String number = dirFileName.substring(startIndex + 1, endIndex);
					//mapのvalue⇒入力した時刻とフォルダにある時刻の差分
					int compareTime = convertToSeconds(dirStart);
					int diff = startTimeInSeconds - compareTime;
					diffMap.put(number, diff);
					System.out.println(diff);
				}

				// 日付が一番近く かつ 若い番号のデータを見つける
				Optional<Map.Entry<String, Integer>> minEntry = diffMap.entrySet().stream()
						.filter(entry -> entry.getValue() >= 0) // 0以上の値をフィルタリング
						.min(Map.Entry.comparingByValue()); // 値を基準に最小のエントリを取得

				String prefix = "";
				if (minEntry.isPresent()) {
					prefix = minEntry.get().getKey();
				} else {
					prefix = "1";
				}

				//時間のファイル名
				StringBuilder sb = new StringBuilder();
				sb.append("m(" + prefix + ")");
				sb.append(startTime.replace(":", "-") + "_" + endTime.replace(":", "-") + "●" + ".mp4");

				//動画ファイル生成
				File inputFile = new File(mainVideoPath);
				File outputFile = new File(currentPath + "\\" + "trim_main" + "\\" + sb.toString());
				if (outputFile.exists()) {
					showAlert(AlertType.ERROR, "Information", "同名のファイルが存在します。\n" + sb.toString());
				} else {
					// FFmpegコマンド
					String command = String.format(
							"ffmpeg -noaccurate_seek -ss %s -i \"%s\" -t %s -c copy \"%s\"",
							startTime, inputFile, duration, outputFile);

					System.out.println(command);

					try {
						Process process = Runtime.getRuntime().exec(command);

						int exitCode = process.waitFor();
						if (exitCode == 0) {
							showAlert(AlertType.INFORMATION, "Information", sb.toString() + "を出力しました");
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
						showAlert(AlertType.ERROR, "Exception", e.getMessage());
					}
				}

			}

		});

		//レイアウト全体
		VBox root = new VBox(numberTimeVBox, anyTimeVBox);
		Scene scene = new Scene(root);
		scene.getStylesheets().add(CSS_FILE);
		primaryStage.setTitle("動画切り取りツール");
		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	private static void getNumberList() {

		File movieDir = new File(currentPath + "\\" + "trim_main");
		File[] files = movieDir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.toString().indexOf(".") != -1) {
				String[] split = file.toString().split("\\\\");
				String fileName = split[split.length - 1];
				if (!fileName.contains("●")) {
					int startNum = fileName.indexOf("(");
					int endNum = fileName.indexOf(")");
					int dotNum = fileName.indexOf(".");
					//動画番号と時間を取得してmapに追加
					String number = fileName.substring(startNum + 1, endNum);
					String time = fileName.substring(endNum + 1, dotNum);
					map.put(number, time);
					//ファイル名一覧も取得
					fileNameList.add(fileName);
					getMaxAlphabetForNumbers(number);
				}
			}
		}

		//プルダウンにセット
		for (String key : map.keySet()) {
			numBox.getItems().addAll(key);
		}

		//降順に並び替え
		List<String> items = numBox.getItems();
		List<String> sortedItems = items.stream()
				.sorted((a, b) -> Integer.compare(extractNumber(a), extractNumber(b)))
				.collect(Collectors.toList());
		numBox.setItems(FXCollections.observableArrayList(sortedItems));
		numBox.setValue(numBox.getItems().get(0));
	}

	//各数字のうち一番新しいアルファベットを保持
	public static void getMaxAlphabetForNumbers(String input) {

		// 数字部分とアルファベット部分を抽出
		String numberPart = input.replaceAll("[^\\d]", "");
		String alphabetPart = input.replaceAll("\\d", "");

		Character alphabet = alphabetPart.isEmpty() ? null : alphabetPart.charAt(0);

		// Mapに既に数字が存在する場合、アルファベットを比較して最大のものを保持
		if (maxAlphabetMap.containsKey(numberPart)) {
			Character currentMaxAlphabet = maxAlphabetMap.get(numberPart);
			if (alphabet != null && (currentMaxAlphabet == null || alphabet > currentMaxAlphabet)) {
				maxAlphabetMap.put(numberPart, alphabet);
			}
		} else {
			// 数字が存在しない場合、新たにエントリを作成
			maxAlphabetMap.put(numberPart, alphabet);
		}
	}

	private static int extractNumber(String str) {
		// Extract the numeric part from the beginning of the string
		String number = str.replaceAll("[^0-9].*", "");
		return Integer.parseInt(number);
	}

	// 秒数をHH-mm-ss形式にフォーマットするメソッド
	private static String convertToHHMMSS(long totalSeconds) {
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public static String convertToHHMMSSbyHHMMSS(int hours, int minutes, int seconds) {
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	// 時間を秒に変換するメソッド
	int convertToSeconds(String time) {
		String[] parts = time.split(":");
		int hours = Integer.parseInt(parts[0]);
		int minutes = Integer.parseInt(parts[1]);
		int seconds = Integer.parseInt(parts[2]);
		return hours * 3600 + minutes * 60 + seconds;
	}

	public static String addSecondsToTimestamp(String timestamp, int secondsToAdd) {
		// DateTimeFormatterを使用してhh:mm:ss形式の時間を解析
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalTime time = LocalTime.parse(timestamp, formatter);

		// 秒を加算
		LocalTime newTime = time.plusSeconds(secondsToAdd);

		// 新しい時間を文字列として返す
		return newTime.format(formatter);
	}

	public static String getNextAlphabet(char currentChar) {

		// 入力文字がアルファベットであることを確認
		if (currentChar < 'a' || currentChar > 'z') {
			throw new IllegalArgumentException("Input must be a lowercase alphabetic character.");
		}

		// 次のアルファベットを計算
		if (currentChar == 'z') {
			return "a"; // 'z' の次は 'a'
		} else {
			char nextChar = (char) (currentChar + 1);
			return Character.toString(nextChar);
		}
	}

	// メッセージボックスを表示するメソッド
	private void showAlert(AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null); // ヘッダーを非表示にする場合
		alert.setContentText(message);
		alert.showAndWait();
	}
}
