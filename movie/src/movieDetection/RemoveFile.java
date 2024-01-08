package movieDetection;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveFile {

	public static void main(String[] args) {

		//プロパティファイル読み込み
		Path p1 = Paths.get("");
		Path p2 = p1.toAbsolutePath();
		String currentPath = p2.toString();

		File folder = new File(currentPath + "\\trim_main");
		File[] files = folder.listFiles();
		Map<String , String> mainMap = new HashMap<String, String>();

		//trim_mainフォルダにあるファイル名から()の中の数字を取り出す
		for(File item : files) {
			// 正規表現パターンを定義
			String pattern = "\\((.*?)\\)";
			Pattern regex = Pattern.compile(pattern);
			Matcher matcher = regex.matcher(item.getName());

			// ()内の文字列を抽出
			while (matcher.find()) {
				String extractedText = matcher.group(1);
				String fileName = item.getName().substring(1);
				mainMap.put(extractedText,fileName);
			}
		}


		//trim_counterフォルダにある番号が、trim_mainから取得した番号に一致しなかったらtrim_counterから削除する。
		Map<String , String> trimMap = new HashMap<String, String>();
		List <String> removeTrimList = new ArrayList<String>();
		folder = new File(currentPath + "\\trim_counter");
		files = folder.listFiles();
		for(File item : files) {
			// 正規表現パターンを定義
			String pattern = "\\((.*?)\\)";
			Pattern regex = Pattern.compile(pattern);
			Matcher matcher = regex.matcher(item.getName());

			// ()内の文字列を抽出
			while (matcher.find()) {
				String extractedText = matcher.group(1);
				String fileName = item.getName().substring(1);
				trimMap.put(extractedText,fileName);
			}
		}

		for(String trimKey : trimMap.keySet()) {
			if(!mainMap.containsKey(trimKey)) {
				removeTrimList.add("c" + trimMap.get(trimKey));
			}
		}

		//trimにある対象のファイルを削除
		for(String item : removeTrimList) {
			File file = new File(currentPath + "\\trim_counter" + "\\" + item);
			if(file.exists()) {
				file.delete();
				System.out.println(file.getName() + "を削除しました。");
			}
		}

	}

}
