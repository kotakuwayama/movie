package movieDetection;

import java.io.IOException;

public class Test {

	public static void main(String[] args) throws IOException, InterruptedException {

		String command3 = "手順3_コマンド生成.bat";
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", command3);
		Process process = pb.start();
		process.waitFor();

		System.out.println("プロセスが終了しました");

	}

}
