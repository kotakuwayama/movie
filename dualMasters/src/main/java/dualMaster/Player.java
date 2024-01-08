package dualMaster;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {

	private List<File> deck = new ArrayList<File>();
	private List<File> cemetery = new ArrayList<File>();
	private List<String> deckId = new ArrayList<String>();

	public List<File> getDeck() {
		return deck;
	}

	public void setDeck(List<File> deck) {
		this.deck = deck;
	}

	public List<File> getCemetery() {
		return cemetery;
	}

	public void setCemetery(List<File> cemetery) {
		this.cemetery = cemetery;
	}

	public void addCemetery(String cemetery) {
		File file = new File(cemetery);
		this.cemetery.add(file);
	}
	
	public List<String> getDeckId() {
		return deckId;
	}

	public void addDeckId(String deckId) {
		this.deckId.add(deckId);
	}
	
	public void deleteDeckId(String deckId) {
		this.deckId.remove(deckId);
	}
	

	/**
	 * ドロップされたカードをデッキ下に追加する
	 * @param String cardPath
	 */
	public void addDeckBottom(String cardPath) {
		File file = new File(cardPath);
		this.deck.add(file);
	}

	/**
	 * ドロップされたカードをデッキ上に追加する
	 * @param String cardPath
	 */
	public void addDeckTop(String cardPath) {
		File file = new File(cardPath);
		this.deck.add(0,file);
	}

	/**
	 * ドロップされたカードをシャッフルしてデッキ下に追加する
	 * @param String cardPath
	 */
	public void addDeckBottomShuffle(List<String> cardListPath) {
		List<File> fileList = new ArrayList<File>();
		for(String item : cardListPath) {
			File file = new File(item);
			fileList.add(file);
		}
		Collections.shuffle(fileList); 
		for(File item : fileList) {
			this.deck.add(item);
		}
	}

	/**
	 * ドロップされたカードをデッキ上からX番目に追加する
	 * @param String cardPath
	 */
	public void addDeckTopSelect(String cardPath, int selectNum) {
		File file = new File(cardPath);
		this.deck.add(selectNum - 1,file);
	}

	/**
	 * 指定されたフォルダから全てのカードを取得する(初期デッキ)
	 * @param String directoryPath
	 */
	public void getCardFiles(String directoryPath) {
		List<File> cardFiles = new ArrayList<>();
		File directory = new File(directoryPath);
		if (directory.exists() && directory.isDirectory()) {
			File[] imageFiles = directory.listFiles();
			if (imageFiles != null) {
				Collections.addAll(cardFiles, imageFiles);
			}
		}
		Collections.shuffle(cardFiles); 
		this.deck = cardFiles;
	}

	/**
	 * デッキからカードをN枚引く
	 * @param int drawNum 引く枚数
	 */
	public List<File> draw(int drawNum) {
		List<File> fileList = new ArrayList<File>();
		if (drawNum <= deck.size()) {
			fileList.addAll(deck.subList(0, drawNum));
			deck.subList(0, drawNum).clear();
		} else {
			return fileList;
		}
		return fileList;
	}

	/**
	 * デッキをシャッフルする
	 */
	public void deckShuffle() {
		Collections.shuffle(this.deck); 
	}

	/**
	 * デッキの残り枚数を取得
	 */
	public int getDeckNum() {
		return this.deck.size();
	}

	/**
	 * デッキの全てのカードファイル名を出力する
	 */
	public void getAllDeckName() {
		for(File item : this.deck) {
			System.out.println(item.getName());
		}
	}

	/**
	 * デッキ上のカードを取得する
	 */
	public File getTopDeckCard() {
		if (!deck.isEmpty()) {
			return deck.get(0);
		}
		return null;
	}

}
