package dualMaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class cardScraping {

	public static void main(String[] args) {
		String hostUrl = "https://dm.takaratomy.co.jp";
		String cardOutput = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\cardList\\";

		String filePath = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\insert文.txt";
		String insertStr = "";

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int roopCnt = 0;
		int twoCnt = 0;

		WebDriver driver = null;
		WebDriver driver2 = null;

		System.setProperty("webdriver.chrome.driver", "C:\\pleiades\\2023-06\\workspace\\dualMasters\\chromedriver-win64\\chromedriver.exe");

		//タカラトミーページからカード情報を取得する。1ページ～349ページまで
		for(int i = 7; i<=349; i++) {
			try {
				driver = new ChromeDriver();
				String basePath = "https://dm.takaratomy.co.jp/card/?v=%7B%22suggest%22:%22on%22,%22keyword_type%22:%5B%22card_name%22,%22card_ruby%22,%22card_text%22%5D,%22culture_cond%22:%5B%22%E5%8D%98%E8%89%B2%22,%22%E5%A4%9A%E8%89%B2%22%5D,%22pagenum%22:%22"+ i + "%22,%22samename%22:%22show%22,%22sort%22:%22release_new%22%7D";
				System.out.println(basePath);
				driver.get(basePath);
				driver2 = new ChromeDriver();

				try {
					// 一覧ページの中から1つのカードに対するリンクを取得し、さらにページを開く
					List<WebElement> elements = driver.findElements(By.cssSelector("ul.cardList01.clearfix > li > a"));
					for (WebElement element : elements) {
						String hrefValue = element.getAttribute("href");
						driver2.get(hrefValue);
						twoCnt = 0;
						//1つのカード情報(ツインパクトの場合複数取得される)
						List<WebElement> infos = driver2.findElements(By.cssSelector(".cardPopupDetail"));
						for (WebElement info : infos) {
							String twin = "0";
							String fileName = "";
							String id = "";
							String name = "";
							String cardPath = "";
							String type = "";
							String color = "";
							String rare = "";
							String power = "";
							String cost = "";
							String mana = "";
							String kind = "";
							String illust = "";
							String ability = "";
							String flavor = "";
							twoCnt++;

							//1つのカードの1項目
							List<WebElement> items = info.findElements(By.cssSelector("tbody > tr > td, tbody > tr > th"));
							for (WebElement item : items) {
								String className = item.getAttribute("class");

								//名前
								if(className.equals("cardname")) {
									String pattern = "([^\\(]+)\\(([^)]+)\\)";
									Pattern regex = Pattern.compile(pattern);
									Matcher matcher = regex.matcher(item.getText());
									if (matcher.find()) {
										name = matcher.group(1);
										fileName = matcher.group(2).replace(" ", "_").replace("/","_");
										id = matcher.group(2).replace(" ", "_").replace("/","_");
										System.out.println(name);
									}
								}
								//カード画像
								else if(className.equals("cardarea")) {
									if(twoCnt >= 2) {
										continue;
									}
									WebElement cardElement = item.findElement(By.cssSelector(".cardimg > img"));
									cardPath = cardElement.getAttribute("src");
									try {
										URL url = new URL(cardPath);
										URLConnection connection = url.openConnection();
										InputStream inputStream = connection.getInputStream();
										fileName = cardOutput + fileName + ".jpg";

										File file = new File(fileName);
										if(!file.exists()) {
											FileOutputStream fileOutputStream = new FileOutputStream(fileName);

											byte[] buffer = new byte[1024];
											int bytesRead;
											while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
												fileOutputStream.write(buffer, 0, bytesRead);
											}
											fileOutputStream.close();
											inputStream.close();
										}								
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								//種類(クリーチャーとか呪文とか)
								else if(className.equals("typetxt")) {
									type = item.getText();
								}
								//文明
								else if(className.equals("civtxt")) {
									color = item.getText();
								}
								//レア度
								else if(className.equals("raretxt")) {
									rare = item.getText();
								}
								//パワー
								else if(className.equals("powertxt")) {
									power = item.getText();
								}
								//コスト
								else if(className.equals("costtxt")) {
									cost = item.getText();
								}
								//マナ(基本1か0)
								else if(className.equals("manatxt")) {
									mana = item.getText();
								}
								//種族
								else if(className.equals("racetxt")) {
									kind = item.getText();
								}
								//イラストレーター
								else if(className.equals("illusttxt")) {
									illust = item.getText();
								}
								//能力
								else if(className.equals("abilitytxt")) {
									ability = item.getText();
								}
								//フレーバーテキスト
								else if(className.equals("flavortxt")) {
									flavor = item.getText();
								}
							}

							if(roopCnt == 0) {
								insertStr += "INSERT INTO dualmasterscardlist (id, name, cost, mana, color, type, kind, power, rarity, ability, illustrator, flavor) "
										+ "VALUES('" + id + "','" +  name + "','" + cost + "','" +  mana + "','" +  color  + "','" +  type + "','" +  kind + "','" + power + "','" + rare + "','" + ability + "','" + illust  + "','" + flavor + "'),\n";
							}else {
								insertStr +="('" + id + "','" +  name + "','" + cost + "','" +  mana + "','" +  color  + "','" +  type + "','" +  kind + "','" + power + "','" + rare + "','" + ability + "','" + illust  + "','" + flavor + "'),\n";
							}

							roopCnt++;



						}
					}

					try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
						writer.write(insertStr);
					} catch (IOException e) {
						e.printStackTrace();
					}finally {
						driver2.close();
					}

				}catch (Exception e) {
					System.out.println(e.getMessage());
				}

			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				driver.close();
			}
		}

		System.out.println("end");
	}
}
