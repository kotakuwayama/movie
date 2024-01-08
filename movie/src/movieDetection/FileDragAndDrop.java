package movieDetection;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.io.File;

public class FileDragAndDrop {

	public static void main(String[] args) {
		// ドラッグアンドドロップ先の位置の座標を指定
		int dropX = 500;
		int dropY = 500;

		// ドラッグアンドドロップするフォルダのパスを指定
		String sourceFolderPath = "C:\\movieCut\\trim_main";

		// ファイル一覧を取得
		File sourceFolder = new File(sourceFolderPath);
		File[] files = sourceFolder.listFiles();

		// ドラッグアンドドロップ処理
		try {
			Robot robot = new Robot();
			robot.setAutoDelay(100);

			for (File file : files) {
				if (file.isFile()) {
					String filePath = file.getAbsolutePath();

					// ファイルをクリップボードにコピー
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection fileSelection = new StringSelection(filePath);
					clipboard.setContents(fileSelection, null);

					// ドラッグアンドドロップ操作をシミュレート
					robot.mouseMove(dropX, dropY);
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.delay(100);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

					// 一時的にクリップボードをクリア
					clipboard.setContents(new StringSelection(""), null);

					// ドラッグアンドドロップが完了するまで待機
					Thread.sleep(5000);
				}
			}
		} catch (AWTException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
