package movieDetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class movieCut_only {
	private static Mat prevGrayFrame;

	public static void main(String[] args) throws IOException {
		//プロパティファイル読み込み
		Path p1 = Paths.get("");
		Path p2 = p1.toAbsolutePath();
		String currentPath = p2.toString();

		//dll読み込み
		String dllPath = currentPath + "\\opencv_java480.dll";
		System.load(dllPath);

		long point1 = System.currentTimeMillis();
		Properties properties = new Properties();
		String propertiesPass = currentPath + "\\system.properties";
		double noChangeTime = 0;
		double noChangeTimeM = 0;
		int beforeHoldTime = 0;
		int afterHoldTime = 0;
		int rectangleX = 0;
		int rectangleY = 0;
		int rectangleWidth = 0;
		int rectangleHeight = 0;
		int counterFlg = 0;
		int thresholdProperties = 0;
		Double removeBlockTime = 0.0;
		InputStream istream = new FileInputStream(propertiesPass);
		properties.load(istream);
		noChangeTime = Integer.valueOf(properties.getProperty("noChangeTime"));
		noChangeTimeM = Integer.valueOf(properties.getProperty("noChangeTime"));
		beforeHoldTime = Integer.valueOf(properties.getProperty("beforeHoldTime"));
		afterHoldTime = Integer.valueOf(properties.getProperty("afterHoldTime"));
		rectangleX = Integer.valueOf(properties.getProperty("rectangleX"));
		rectangleY = Integer.valueOf(properties.getProperty("rectangleY"));
		rectangleWidth = Integer.valueOf(properties.getProperty("rectangleWidth"));
		rectangleHeight = Integer.valueOf(properties.getProperty("rectangleHeight"));
		counterFlg = Integer.valueOf(properties.getProperty("counterOutputFlg"));
		thresholdProperties = Integer.valueOf(properties.getProperty("threshold"));
		removeBlockTime = Double.valueOf(properties.getProperty("removeBlockTime"));

		// パチンコ本体のパスを指定
		String mainPath = currentPath + "\\input_main";
		File mainFolder = new File(mainPath);
		File[] files = mainFolder.listFiles();
		String vMainPath = "";
		for (File file : files) {
			if (file.isFile()) {
				vMainPath = file.getName();
			}
		}

		VideoCapture vc = new VideoCapture(mainPath + "\\" + vMainPath);
		int frameWidth = (int) vc.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int frameHeight = (int) vc.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		double frameRate = vc.get(Videoio.CAP_PROP_FPS);
		double totalFrames = vc.get(Videoio.CAP_PROP_FRAME_COUNT);
		double durationInSeconds = totalFrames / frameRate;
		System.out.println("動画の秒数: " + durationInSeconds + "秒");
		System.out.println(noChangeTime + "秒以上変化がなかったら取得");
		System.out.println("取得する場合は開始の" + beforeHoldTime + "秒前から取得");
		System.out.println("取得する場合は終わり" + afterHoldTime + "秒後まで取得\n");

		//指定された秒数をフレーム数に変換
		noChangeTime = frameRate * noChangeTime;
		removeBlockTime = frameRate * removeBlockTime;

		List<Integer> frameStaretEnd = new ArrayList<Integer>();
		List<List<Integer>> frameList = new ArrayList<List<Integer>>();
		List<List<Integer>> frameResultList = new ArrayList<List<Integer>>();

		int frameSkipCnt = (int) frameRate / 4;
		Mat frame = new Mat();
		int frameCnt = 0;
		int tmpFrame = 0;
		double threshold = 50;// 閾値

		//-----------------切り取る開始終了フレームの処理 開始-------------------
		while (frameCnt < totalFrames - 1) {
			frameCnt++;

			//フレームレートの3で割った数ごとに変化をみる
			if (frameCnt % frameSkipCnt == 3) {
				vc.grab();
				vc.retrieve(frame);
				if (prevGrayFrame != null) {

					// フレーム間の差分を計算
					Rect regionOfInterest = new Rect(rectangleX, rectangleY, rectangleWidth, rectangleHeight);
					Mat roiFrame = new Mat(prevGrayFrame, regionOfInterest);
					if (frame.empty()) {
						break;
					}
					Mat roiCurrentFrame = new Mat(frame, regionOfInterest);

					Mat diffFrame = new Mat();
					Core.absdiff(roiFrame, roiCurrentFrame, diffFrame);
					prevGrayFrame.release();

					// 差分を二値化する
					Mat binaryDiff = new Mat();
					Imgproc.threshold(diffFrame, binaryDiff, threshold, 1, Imgproc.THRESH_BINARY);

					// 二値化した差分の合計値を計算
					double totalDiff = Core.sumElems(binaryDiff).val[0];
					if (totalDiff > thresholdProperties) {
						//指定したフレーム以上変化がなかった場合は切り取り箇所とする。
						if (frameCnt - tmpFrame > noChangeTime) {
							System.out.println(noChangeTimeM + "秒以上変化しない時間帯:  start=" + (tmpFrame) / frameRate + "秒" + "  end=" + (frameCnt) / frameRate + "秒");
							frameStaretEnd.add(tmpFrame);
							frameStaretEnd.add(frameCnt);
							frameList.add(frameStaretEnd);
							frameStaretEnd = new ArrayList<Integer>();
						}
						tmpFrame = frameCnt;
					}
					roiFrame.release();
					roiCurrentFrame.release();
					diffFrame.release();
					binaryDiff.release();
				}
				// 現在のフレームを次のフレームの前に保存
				prevGrayFrame = frame.clone();
				frame.release();
			} else {
				vc.grab();
			}
		}
		vc.release();
		//-----------------切り取る開始終了フレームの処理 終了-------------------

		//-----------------各ブロックの開始終了日を保持する 開始-----------------
		// frameListと同じデータをコピーする新しいリストを作成
		List<List<Integer>> copiedList = new ArrayList<List<Integer>>();
		for (List<Integer> innerList : frameList) {
			List<Integer> copy = new ArrayList<>(innerList);
			copiedList.add(copy);
		}
		//-----------------各ブロックの開始終了日を保持する 終了-----------------

		//-----------------データの連結 開始-----------------
		int startTime = 0;
		int nextStartTime = 0;
		int endTime = 0;
		int beforeEndTime = 0;
		//
		//		//開始終了余白の合計 > 終了開始の差分 であればデータを合体し、不要になったデータを削除
		Double totalBlankTime = (beforeHoldTime * frameRate) + (afterHoldTime * frameRate);

		// 条件に基づいて要素を修正および削除
		Iterator<List<Integer>> iterator = frameList.iterator();
		List<Integer> previousData = null;
		while (iterator.hasNext()) {
			List<Integer> currentData = iterator.next();
			if (previousData != null) {
				double diff = currentData.get(0) - previousData.get(1);
				if (diff < totalBlankTime) {
					// 差分が開始終了余白の合計より小さい場合、要素を修正
					previousData.set(1, currentData.get(1));
					// 修正された要素を削除
					iterator.remove();
				} else {
					previousData = currentData;
				}
			} else {
				previousData = currentData;
			}
		}
		//-----------------データの連結 終了-----------------

		//-----------------指定した何秒以内の動画を削除する 開始-------------------
		//		Iterator<List<Integer>> iterator2 = frameList.iterator();
		//		while (iterator2.hasNext()) {
		//			List<Integer> listItem = iterator2.next();
		//			int startTimeStr = listItem.get(0);
		//			int endTimeStr = listItem.get(1);
		//			int diffFrame = endTimeStr - startTimeStr;
		//
		//			String[] timeTotal = removeTime.split(":");
		//			int hours = Integer.parseInt(timeTotal[0]);
		//			int minutes = Integer.parseInt(timeTotal[1]);
		//			int seconds = Integer.parseInt(timeTotal[2]);
		//
		//			// 時間、分、秒を秒数に変換しフレームレート
		//			Double inputFrame = ((hours * 3600) + (minutes * 60) + seconds) * frameRate;
		//			if (diffFrame < inputFrame) {
		//				iterator2.remove();
		//			}
		//		}
		//-----------------指定した何秒以内の動画を削除する 終了-------------------

		//-----------------ブロック単位の削除 開始-----------------
		iterator = frameList.iterator();
		int countData = 0;
		while (iterator.hasNext()) {
			Boolean diff = false;
			List<Integer> currentData = iterator.next();

			//結合前の保持していたデータ
			Boolean bool = true;
			for (int i = 0; i < copiedList.size(); i++) {
				if (bool) {
					i = countData;
					bool = false;
				}
				Integer start = copiedList.get(i).get(0);
				Integer end = copiedList.get(i).get(1);
				if (currentData.get(0) <= start && currentData.get(1) >= end) {
					if (end - start > removeBlockTime) {
						diff = true;
					}
				}
				if (currentData.get(1) == end) {
					break;
				}
			}
			if (diff != true) {
				iterator.remove();
			}
			countData++;
		}
		//-----------------ブロック単位の削除 終了-----------------

		//-----------------切り取るデータの前後の秒数を計算 開始-----------------
		//開始終了の余白を追加
		for (int i = 0; i < frameList.size(); i++) {
			startTime = frameList.get(i).get(0);
			endTime = frameList.get(i).get(1);
			frameStaretEnd = new ArrayList<Integer>();
			//開始
			//一番最初のデータは前データが存在しないので、開始-XX秒>0 であるかだけ判定
			if (i == 0) {
				if (startTime - (beforeHoldTime * frameRate) > 0) {
					frameStaretEnd.add(startTime - (beforeHoldTime * (int) frameRate));
				} else {
					frameStaretEnd.add(startTime);
				}
			} else {
				//開始が1つ前のデータの終了よりも大きい場合
				//一番最初のデータは比較対象がないので、元々のデータから取得。それ以外は秒数を計算後のデータを取得。
				if (frameResultList.size() == 0) {
					beforeEndTime = frameList.get(i - 1).get(1);
				} else {
					beforeEndTime = frameResultList.get(frameResultList.size() - 1).get(1);
				}

				if (startTime - (beforeHoldTime * frameRate) > beforeEndTime) {
					frameStaretEnd.add(startTime - (beforeHoldTime * (int) frameRate));
				} else {
					frameStaretEnd.add(startTime);
				}
			}

			//終了
			//一番最後のデータは次データが存在しないので、終了+XX秒<全体の秒数 であるかだけ判定
			if (i == frameList.size() - 1) {
				if (endTime + (afterHoldTime * frameRate) < totalFrames) {
					frameStaretEnd.add(endTime + (afterHoldTime * (int) frameRate));
				} else {
					frameStaretEnd.add(endTime);
				}
			} else {
				//終了が1つ後のデータの終了よりも大きい場合
				nextStartTime = frameList.get(i + 1).get(0);
				if (endTime + (afterHoldTime * frameRate) < nextStartTime) {
					frameStaretEnd.add(endTime + (afterHoldTime * (int) frameRate));
				} else {
					frameStaretEnd.add(endTime);
				}
			}
			frameResultList.add(frameStaretEnd);
		}
		//-----------------切り取るデータの前後の秒数を計算 終了-------------------

		//-----------------テキストデータへの出力 開始-----------------------------
		File file = new File(currentPath + "\\開始終了位置.txt");
		// ファイルが存在しない場合、新規ファイルを作成
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file);
		for (List<Integer> list : frameResultList) {
			String startName = String.format("%02d:%02d:%02d", (int) (list.get(0) / frameRate / 3600), (int) ((list.get(0) / frameRate % 3600) / 60), (int) (list.get(0) / frameRate % 60));
			String endName = String.format("%02d:%02d:%02d", (int) (list.get(1) / frameRate / 3600), (int) ((list.get(1) / frameRate % 3600) / 60), (int) (list.get(1) / frameRate % 60));
			fw.write("開始 " + startName + "秒  " + "終了 " + endName + "秒\r\n");
		}

		fw.close();

		file = new File(currentPath + "\\手順4_トリミング.bat");
		// ファイルが存在しない場合、新規ファイルを作成
		if (!file.exists()) {
			file.createNewFile();
		}
		//-----------------テキストデータへの出力 終了----------------------------

		//-----------------コマンドの出力 開始-------------------------------------
		fw = new FileWriter(file);
		int count = 0;
		for (List<Integer> itemList : frameResultList) {
			String startCommand = String.format("%02d:%02d:%02d", (int) (itemList.get(0) / frameRate / 3600), (int) ((itemList.get(0) / frameRate % 3600) / 60), (int) (itemList.get(0) / frameRate % 60));
			String endCommand = String.format("%02d:%02d:%02d", (int) (itemList.get(1) / frameRate / 3600), (int) ((itemList.get(1) / frameRate % 3600) / 60), (int) (itemList.get(1) / frameRate % 60));
			String startName = String.format("%02d-%02d-%02d", (int) (itemList.get(0) / frameRate / 3600), (int) ((itemList.get(0) / frameRate % 3600) / 60), (int) (itemList.get(0) / frameRate % 60));
			String endName = String.format("%02d-%02d-%02d", (int) (itemList.get(1) / frameRate / 3600), (int) ((itemList.get(1) / frameRate % 3600) / 60), (int) (itemList.get(1) / frameRate % 60));

			// FFmpegコマンドを構築
			String command = currentPath + "\\ffmpeg.exe -ss " + startCommand + " -to " + endCommand + " -i " + mainFolder + "\\" + vMainPath + " -c:v copy -c:a aac " + currentPath + "\\trim_main" + "\\m(" + ++count + ")" + startName + "_" + endName + ".mp4\n";
			System.out.println(command);
			fw.write(command);
		}

		fw.close();
		//-----------------コマンドの出力 終了------------------

		long point2 = System.currentTimeMillis();
		System.out.println("経過時間：" + (point2 - point1) / 1000 + "秒");
	}

}