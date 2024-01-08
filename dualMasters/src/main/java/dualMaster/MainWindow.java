package dualMaster;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainWindow extends Application {

	private static final String CARD_LIST = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\cardList\\";
	private static final String IMAGE_DIRECTORY = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\deck";
	private static final String AUDIO_DIRECTORY = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\audio\\";
	private static final String DECK_IMAGE = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\デッキ画像.jpg";
	private static final String SHILD_IMAGE = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\シールド画像.jpg";
	private static final String CEMETERY_IMAGE = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\墓地画像.jpg";
	private static final String GR_IMAGE = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\GR画像.png";
	private static final String PSYCHIC_IMAGE = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\超次元画像.png";
	private static final String CSS_FILE = "/style.css";

	private static final int START_CARDS = 5;
	private static final String[] ZONE_NAMES = {"バトルゾーン", "シールドゾーン", "手札", "マナゾーン", "どこでもないゾーン","デッキ","墓地","GRゾーン"};
	private long clickStartTime;

	private static final double CARD_WIDTH = 100.0;
	private static final double CARD_HEIGHT =140.0;

	private static final double CARD_LARGE_WIDTH = 220.0;
	private static final double CARD_LARGE_HEIGHT =308.0;

	private static final int CARD_MAX =10;

	Connection con = null;
	PreparedStatement stmt = null;
	ResultSet rs = null;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		initializeGame(primaryStage);
	}

	// 初期化メソッド
	private void initializeGame(Stage primaryStage) {
		primaryStage.setTitle("デュエマシミュレータ");

		Player p1 = new Player();
		p1.getCardFiles(IMAGE_DIRECTORY);

		// バトルゾーン
		HBox battleZone = new HBox();
		battleZone.setAlignment(Pos.CENTER);
		battleZone.setStyle("-fx-background-color: silver");
		battleZone.setMinSize(1000, CARD_HEIGHT);
		battleZone.setMaxSize(1000, CARD_HEIGHT);
		battleZone.setId(ZONE_NAMES[0]);

		// シールドゾーン
		HBox shieldZone = getShieldZoneCard("シールド", Pos.CENTER, p1, START_CARDS, ZONE_NAMES[1]);
		shieldZone.setAlignment(Pos.CENTER);
		shieldZone.setStyle("-fx-background-color: silver;");
		shieldZone.setMinSize(1000, CARD_HEIGHT);
		shieldZone.setMaxSize(1000, CARD_HEIGHT);
		shieldZone.setId(ZONE_NAMES[1]);

		// 手札
		HBox handZone = getRandomCard("手札", Pos.CENTER, p1, START_CARDS, ZONE_NAMES[2]);
		handZone.setStyle("-fx-background-color: silver;");
		handZone.setMinSize(1000, CARD_HEIGHT);
		handZone.setMaxSize(1000, CARD_HEIGHT);
		handZone.setId(ZONE_NAMES[2]);

		// マナゾーン
		HBox manaZone = new HBox();
		manaZone.setAlignment(Pos.CENTER);
		manaZone.setStyle("-fx-background-color: silver;");
		manaZone.setMinSize(1000, CARD_HEIGHT);
		manaZone.setMaxSize(1000, CARD_HEIGHT);
		manaZone.setId(ZONE_NAMES[3]);

		// 利用中ゾーン
		HBox useZone = new HBox();
		useZone.setAlignment(Pos.CENTER);
		useZone.setStyle("-fx-background-color: silver;");
		useZone.setMinSize(1000, CARD_HEIGHT);
		useZone.setMaxSize(1000, CARD_HEIGHT);
		useZone.setId(ZONE_NAMES[4]);

		// VBoxで3つの領域を縦に配置
		VBox leftZoneLayout = new VBox(20);
		leftZoneLayout.getChildren().addAll(createZoneWithLabel("バトルゾーン", battleZone),
				createZoneWithLabel("シールドゾーン", shieldZone),
				createZoneWithLabel("マナゾーン", manaZone),
				createZoneWithLabel("手札", handZone),
				createZoneWithLabel("どこでもないゾーン(表向きにするカードや、使用中の呪文など)", useZone));
		leftZoneLayout.setAlignment(Pos.CENTER);

		// デッキゾーン
		HoverZoomImageView deckImage = createCardImage(new Image(DECK_IMAGE), DECK_IMAGE, ZONE_NAMES[5], CARD_WIDTH, CARD_HEIGHT);
		deckImage.setFitWidth(150);
		deckImage.setFitHeight(220);
		Label deckLabel = new Label("デッキ");
		VBox deckZone = new VBox();
		deckZone.getChildren().addAll(deckImage, deckLabel);
		deckZone.setAlignment(Pos.BOTTOM_CENTER);

		// リセットボタン
		Button resetButton = new Button("リセット");
		resetButton.setOnAction(event -> {
			Stage newPrimaryStage = new Stage();
			initializeGame(newPrimaryStage);
			newPrimaryStage.setMaximized(true);
			newPrimaryStage.show();
			primaryStage.close();
		});

		//デッキ編成
		Button edit = new Button("デッキ編成");

		VBox deckReset = new VBox(0);
		deckReset.getChildren().addAll(resetButton, edit, deckZone);
		// 異なる余白を設定
		VBox.setMargin(resetButton, new Insets(0, 0, 0, 0));
		VBox.setMargin(edit, new Insets(400, 0, 200, 0));
		deckReset.setAlignment(Pos.BOTTOM_CENTER);

		// 墓地ゾーン
		HoverZoomImageView cemeteryImage = createCardImage(new Image(CEMETERY_IMAGE), DECK_IMAGE, ZONE_NAMES[6], CARD_WIDTH, CARD_HEIGHT);
		cemeteryImage.setFitWidth(150);
		cemeteryImage.setFitHeight(220);
		Label cemeteryLabel = new Label("墓地");
		VBox cemeteryZone = new VBox();
		cemeteryZone.getChildren().addAll(cemeteryImage, cemeteryLabel);
		cemeteryZone.setAlignment(Pos.BOTTOM_CENTER);

		//GRゾーン
		HoverZoomImageView GRImage = createCardImage(new Image(GR_IMAGE), DECK_IMAGE, ZONE_NAMES[7], CARD_WIDTH, CARD_HEIGHT);
		GRImage.setFitWidth(150);
		GRImage.setFitHeight(220);
		Label GLabel = new Label("GR");
		VBox GRZone = new VBox();
		GRZone.getChildren().addAll(GRImage, GLabel);
		GRZone.setAlignment(Pos.BOTTOM_CENTER);

		//右側部分
		HBox rightZoneLayout = new HBox(30);
		rightZoneLayout.getChildren().addAll(deckReset, cemeteryZone, GRZone);
		rightZoneLayout.setAlignment(Pos.TOP_CENTER);

		// HBoxで左右のゾーンを横に配置
		HBox mainLayout = new HBox(20);
		mainLayout.getChildren().addAll(leftZoneLayout, rightZoneLayout);
		mainLayout.setAlignment(Pos.CENTER_LEFT);
		mainLayout.setPadding(new Insets(0, 0, 0, 20)); // 上、右、下、左の余白を指定

		Scene scene = new Scene(mainLayout);
		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);

		//各ゾーンのドラッグドロップイベント処理
		setupDragAndDrop(ZONE_NAMES[0], battleZone, shieldZone, handZone, useZone, manaZone);
		setupDragAndDrop(ZONE_NAMES[1], shieldZone, handZone, useZone, manaZone, battleZone);
		setupDragAndDrop(ZONE_NAMES[3], manaZone, battleZone, shieldZone, handZone, useZone);
		setupDragAndDrop(ZONE_NAMES[2], handZone, useZone, manaZone, battleZone, shieldZone);
		setupDragAndDrop(ZONE_NAMES[4], useZone, manaZone, battleZone, shieldZone, handZone);

		//------------デッキゾーンのドラッグドロップ 特別な処理なので共通部品を使わない-------------//
		deckZone.setOnDragOver(event -> {
			if (event.getGestureSource() != deckZone && event.getDragboard().hasImage()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
		deckZone.setOnDragDropped(event -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasImage()) {
				HoverZoomImageView droppedImageView = (HoverZoomImageView) event.getGestureSource();
				List<String> choices = List.of("デッキ下", "デッキ上", "シャッフルしてデッキ下", "デッキに戻してシャッフル", "デッキ上からX番目に戻す");
				ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
				dialog.setHeaderText("デッキに戻す方法を選択してください");
				Optional<String> result = dialog.showAndWait();
				//デッキの戻す選択肢によって分岐
				result.ifPresent(action -> {
					switch (action) {
					case "デッキ下":
						p1.addDeckBottom(droppedImageView.getImageUrl());
						break;
					case "デッキ上":
						p1.addDeckTop(droppedImageView.getImageUrl());
						break;
					case "シャッフルしてデッキ下":
						//TODO 複数選択モードが出来てから
						p1.addDeckBottom(droppedImageView.getImageUrl());
						break;
					case "デッキに戻してシャッフル":
						p1.addDeckBottom(droppedImageView.getImageUrl());
						p1.deckShuffle();
						playSound("シャッフル.wav");
						break;
					case "デッキ上からX番目に戻す":
						//"1～60番目を表示
						List<String> numberChoices = IntStream.rangeClosed(1, p1.getDeckNum())
						.mapToObj(Integer::toString)
						.collect(Collectors.toList());

						// 数値を選択するダイアログを表示
						ChoiceDialog<String> numberDialog = new ChoiceDialog<>(numberChoices.get(0), numberChoices);
						numberDialog.setHeaderText("デッキ上から何番目に戻すか選択してください");
						Optional<String> numberResult = numberDialog.showAndWait();
						// 数値を取得
						numberResult.ifPresent(selectedNumber -> {
							int selectedPosition = Integer.parseInt(selectedNumber);
							p1.addDeckTopSelect(droppedImageView.getImageUrl(), selectedPosition);
						});
						break;
					default:
						break;
					}
				});
				handZone.getChildren().remove(droppedImageView);
				shieldZone.getChildren().remove(droppedImageView);
				manaZone.getChildren().remove(droppedImageView);
				useZone.getChildren().remove(droppedImageView);

				//ドラックしたカード領域幅を調整
				cardDraqNarrow(droppedImageView, handZone, shieldZone, manaZone, useZone, battleZone);
				droppedImageView.setCurrentZone(ZONE_NAMES[5]);

				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});


		//------------墓地ゾーンにドロップ 特別な処理なので共通部品を使わない-------------//
		cemeteryZone.setOnDragOver(event -> {
			if (event.getGestureSource() != cemeteryZone && event.getDragboard().hasImage()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
		cemeteryZone.setOnDragDropped(event -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasImage()) {
				HoverZoomImageView droppedImageView = (HoverZoomImageView) event.getGestureSource();				

				//シールドからドロップされた場合は、再読み込みして表向きの画像に差し替える
				if(droppedImageView.getCurrentZone().equals(ZONE_NAMES[1])) {
					File openFile = new File(droppedImageView.getImageUrl());
					Image opemImagae = new Image(openFile.toURI().toString());
					droppedImageView.setImage(opemImagae);
				}
				droppedImageView.setFitWidth(150);
				droppedImageView.setFitHeight(220);

				cemeteryZone.getChildren().clear();
				cemeteryZone.getChildren().addAll(droppedImageView, new Label("墓地"));
				handZone.getChildren().remove(droppedImageView);
				shieldZone.getChildren().remove(droppedImageView);
				manaZone.getChildren().remove(droppedImageView);
				useZone.getChildren().remove(droppedImageView);
				p1.addCemetery(droppedImageView.getImageUrl());

				//ドラックしたカード領域幅を調整
				cardDraqNarrow(droppedImageView, handZone, shieldZone, manaZone, useZone, battleZone);
				droppedImageView.setCurrentZone(ZONE_NAMES[6]);

				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});


		//デッキクリック時の操作
		deckImage.setOnMouseClicked(event -> {
			if (isLongClick()) {
				return;
			}
			//デッキを左クリック→カードドロー
			if (event.getButton() == MouseButton.PRIMARY) {
				playSound("カードをめくる.wav");
				deckDraw(handZone, p1, 1, ZONE_NAMES[2]);

				//デッキを右クリック→どこでもないゾーンに置く
			}else if(event.getButton() == MouseButton.SECONDARY) {
				playSound("カードをめくる.wav");
				deckDraw(useZone, p1, 1, ZONE_NAMES[4]);

				//デッキを中央クリック→マナゾーンに置く
			}else if(event.getButton() == MouseButton.MIDDLE) {
				playSound("カードをめくる.wav");
				deckDraw(manaZone, p1, 1, ZONE_NAMES[3]);
			}
		});	
		deckImage.setOnMousePressed(event -> {
			clickStartTime = System.currentTimeMillis();
		});
		deckImage.setOnMouseReleased(event -> {
			long clickEndTime = System.currentTimeMillis();
			long clickDuration = clickEndTime - clickStartTime;

			if (clickDuration >= 500) {
				//左クリックを1秒長押し→シールド追加
				if (event.getButton() == MouseButton.PRIMARY) {
					deckDraw(shieldZone, p1, 1, ZONE_NAMES[1]);

					//右クリックを1秒長押し→デッキサーチ
				}else if(event.getButton() == MouseButton.SECONDARY) {
				}
			}
		});



		//墓地クリック時の操作
		cemeteryZone.setOnMouseClicked(event -> {
			Stage cemeteryWindow = new Stage();
			cemeteryWindow.setTitle("墓地詳細");
			cemeteryWindow.setWidth(1000);
			cemeteryWindow.setHeight(800);

			FlowPane subLayout = new FlowPane();
			subLayout.setHgap(20);
			subLayout.setVgap(20);
			//墓地にあるカードを並べる
			List<File> cemeteryList = p1.getCemetery();
			for(File item : cemeteryList) {
				HoverZoomImageView image = createCardImage(new Image(item.getAbsolutePath()), DECK_IMAGE, ZONE_NAMES[6], CARD_WIDTH, CARD_HEIGHT);
				subLayout.getChildren().add(image);
			}
			Scene subScene = new Scene(subLayout);
			cemeteryWindow.setScene(subScene);
			cemeteryWindow.show();
		});

		//デッキ編成クリック時の操作
		edit.setOnMouseClicked(event -> {

			double maxWidth = Screen.getPrimary().getVisualBounds().getWidth();
			double maxHeight = Screen.getPrimary().getVisualBounds().getHeight();
			primaryStage.setTitle("デッキ編成");
			primaryStage.setWidth(maxWidth);
			primaryStage.setHeight(maxHeight);

			//検索項目画面
			HBox searchSection = new HBox();
			searchSection.setMinHeight((maxHeight / 4));
			searchSection.setAlignment(Pos.BOTTOM_RIGHT);
			searchSection.setStyle("-fx-background-color: #FFFFFA");

			//シミュレーションに移動ボタン
			HBox moveSection = new HBox();
			moveSection.setAlignment(Pos.BOTTOM_RIGHT);
			Button stylishButton = new Button("シミュレーションに移動");
			stylishButton.getStyleClass().add("stylish-button");
			moveSection.getChildren().add(stylishButton);
			searchSection.getChildren().add(moveSection);

			//検索結果のカードリスト
			FlowPane  cardView = new FlowPane ();
			cardView.setMinWidth((maxWidth / 5)  * 3);
			cardView.setMinHeight((maxHeight / 4) * 3);
			cardView.setStyle("-fx-background-color: #F0F8FF");
			cardView.setHgap(10);
			cardView.setVgap(10);

			//デッキリスト
			FlowPane deckList = new FlowPane();
			deckList.setMinWidth((maxWidth / 5)  * 2);
			deckList.setMinHeight((maxHeight / 4) * 3);
			deckList.setHgap(5);
			deckList.setVgap(5);
			deckList.setStyle("-fx-background-color: #C7E6E2");

			List<HoverZoomImageView> imageList = new ArrayList<HoverZoomImageView>();

			int cnt = 0;
			try {
				con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sys", "root", "omega4242");
				String sql = "select * from dualmasterscardlist where color like '%光%'";
				stmt = con.prepareStatement(sql);
				rs = stmt.executeQuery();
				while (rs.next()) {
					String id = rs.getString("id");
					String name = rs.getString("name");
					String fileName = CARD_LIST + "\\" + id + ".jpg";
					ImageView image = new ImageView(fileName);
					image.setFitWidth(CARD_WIDTH);
					image.setFitHeight(CARD_HEIGHT);
					cardView.getChildren().add(image);

					//クリックイベント
					image.setOnMouseClicked(event2 -> {
						ImageView clonedImage = new ImageView(image.getImage());
						//左クリック⇒デッキに追加
						if (event2.getButton() == MouseButton.PRIMARY) {
							clonedImage.setFitWidth(80);
							clonedImage.setFitHeight(112);

							//デッキに追加されたカードにイベントを付与
							clonedImage.setOnMouseClicked(event3 -> {
								//左クリック⇒デッキから削除
								if (event3.getButton() == MouseButton.PRIMARY) {
									deckList.getChildren().remove(clonedImage);
									p1.deleteDeckId(id);
								}
								//右クリック⇒拡大表示
								else if (event3.getButton() == MouseButton.SECONDARY) {
									ImageView clonedImage2 = new ImageView(image.getImage());
									clonedImage2.setFitWidth(350);
									clonedImage2.setFitHeight(525);
									VBox zoomedCardLayout = new VBox(clonedImage2);
									Scene zoomedCardScene = new Scene(zoomedCardLayout, 350, 525);
									Stage zoomedCardStage = new Stage();
									clonedImage2.setOnMouseClicked(closeEvent -> {
										zoomedCardStage.close();
									});
									zoomedCardStage.setScene(zoomedCardScene);
									zoomedCardStage.show();
								}
							});

							deckList.getChildren().add(clonedImage);
							p1.addDeckId(id);
						}
						// 右クリック⇒拡大表示
						else if (event2.getButton() == MouseButton.SECONDARY) {
							clonedImage.setFitWidth(350);
							clonedImage.setFitHeight(525);
							VBox zoomedCardLayout = new VBox(clonedImage);
							Scene zoomedCardScene = new Scene(zoomedCardLayout, 350, 525);
							Stage zoomedCardStage = new Stage();
							clonedImage.setOnMouseClicked(closeEvent -> {
								zoomedCardStage.close();
							});
							zoomedCardStage.setScene(zoomedCardScene);
							zoomedCardStage.show();
						}
					});
				}
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					if (con != null) {
						con.close();
					}
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}
			}

			ScrollPane scrollPane = new ScrollPane(cardView);
			scrollPane.setMinWidth((maxWidth / 5)  * 3);
			scrollPane.setMinHeight((maxHeight / 4) * 3);
			scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);



			HBox bottomLayout = new HBox();
			bottomLayout.getChildren().addAll(scrollPane,deckList);


			//画面全体の構成
			VBox main= new VBox(searchSection, bottomLayout);
			Scene sc = new Scene(main);

			sc.getStylesheets().add(getClass().getResource(CSS_FILE).toExternalForm());
			primaryStage.setScene(sc);
			primaryStage.show();
		});

		primaryStage.show();
	}

	// クリックが1秒以上続いたかどうかを判定
	private boolean isLongClick() {
		long leftClickEndTime = System.currentTimeMillis();
		long clickDuration = leftClickEndTime - clickStartTime;
		return clickDuration >= 500;
	}

	private HBox getRandomCard(String zoneName, Pos alignment, Player player, int numCards, String zone) {
		HBox cardZone = new HBox(0);
		cardZone.setAlignment(alignment);
		List<File> cardFiles = player.getDeck();
		for (int i = 0; i < Math.min(numCards, cardFiles.size()); i++) {
			File cardFile = cardFiles.get(i);
			Image cardImage = new Image(cardFile.toURI().toString());
			HoverZoomImageView cardView = createCardImage(cardImage, cardFile.getParent().toString() + "\\" + cardFile.getName(), zone, CARD_WIDTH, CARD_HEIGHT);
			cardZone.getChildren().add(cardView);
		}
		player.draw(numCards);
		return cardZone;
	}

	private HBox getShieldZoneCard(String zoneName, Pos alignment, Player player, int numCards, String zone) {
		HBox cardZone = new HBox(0);
		cardZone.setAlignment(alignment);
		List<File> cardFiles = player.getDeck();
		for (int i = 0; i < Math.min(numCards, cardFiles.size()); i++) {
			//画像はデッキを使う
			File deckFile = new File(SHILD_IMAGE);
			Image cardImage = new Image(deckFile.toURI().toString());
			//内部ではファイルパスを持たせる
			File cardFile = cardFiles.get(i);
			HoverZoomImageView cardView = createCardImage(cardImage, cardFile.getParent().toString() + "\\" + cardFile.getName(), zone, CARD_WIDTH, CARD_HEIGHT);
			cardZone.getChildren().add(cardView);
		}
		player.draw(numCards);
		return cardZone;
	}


	/**
	 * カードの初期化
	 * @param Image image 表示する画像
	 * @param String imageUrl 表示はしないが内部で持っているカードのファイルパス
	 * @param String zone 表示しているゾーン
	 * @param String width 表示する画像の横幅
	 * @param String height 表示する画像の縦幅
	 * @param String height 表示する画像の縦幅
	 */
	private static HoverZoomImageView createCardImage(Image image, String imageUrl, String zone, double width, double height) {
		final HoverZoomImageView[] cardImageView = {null};
		cardImageView[0] = new HoverZoomImageView(image, imageUrl, zone);
		cardImageView[0].setFitWidth(width);
		cardImageView[0].setFitHeight(height);

		// ドラッグ可能にする
		makeDraggable(cardImageView[0], TransferMode.MOVE);
		return cardImageView[0];
	}

	private VBox createZoneWithLabel(String zoneName, HBox cardZone) {
		Label zoneLabel = new Label(zoneName);
		VBox zoneWithLabel = new VBox(0);
		zoneWithLabel.getChildren().addAll(zoneLabel, cardZone);
		zoneWithLabel.setAlignment(Pos.CENTER);
		return zoneWithLabel;
	}

	/**
	 * デッキからX枚引き特定のゾーンに配置する。
	 * @param HBox targetZone ドローカードの領域
	 * @param Player p1 Playerインスタンス
	 * @param int drawNum 引く枚数
	 * @param String zone ゾーン名
	 */
	private void deckDraw(HBox targetZone, Player p1, int drawNum, String zone) {
		List<File> cardFile = p1.draw(drawNum);
		for (File file : cardFile) {
			//シールドゾーン
			if(zone.equals(ZONE_NAMES[1])) {
				File deckFile = new File(SHILD_IMAGE);
				Image cardImage = new Image(deckFile.toURI().toString());
				HoverZoomImageView cardView = createCardImage(cardImage, file.getParent().toString() + "\\" + file.getName(), zone, CARD_WIDTH, CARD_HEIGHT);
				targetZone.getChildren().add(cardView);
				cardDropNarrow(targetZone, zone);
			}
			//シールドゾーン以外
			else {
				Image cardImage = new Image(file.toURI().toString());
				HoverZoomImageView cardView = createCardImage(cardImage, file.getPath().toString(), zone, CARD_WIDTH, CARD_HEIGHT);
				targetZone.getChildren().add(cardView);
				cardDropNarrow(targetZone, zone);
			}
			//マナゾーンには反転して置く
			if(zone.equals(ZONE_NAMES[3])) {
				targetZone.setRotate(180);
			}
		}
		//デッキが0枚になったらメッセージ
		if (p1.getDeckNum() == 0) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("確認");
			alert.setHeaderText(null);
			alert.setContentText("デッキが0枚です");
			alert.showAndWait();
		}
	}

	private static void makeDraggable(HoverZoomImageView imageView, TransferMode mode) {
		if (!imageView.getCurrentZone().equals(ZONE_NAMES[5])) {
			imageView.setOnDragDetected(event -> {
				Dragboard db = imageView.startDragAndDrop(mode);
				ClipboardContent content = new ClipboardContent();
				content.putImage(imageView.getImage());
				content.putString(imageView.getImageUrl());
				db.setContent(content);
				event.consume();
			});
		}
	}

	/**
	 * 音声を再生
	 * @param String soundFileName 音声ファイルパス(WAVのみ)
	 * @param String imageUrl 表示はしないが内部で持っているカードのファイルパス
	 */
	private void playSound(String soundFileName) {
		try {
			File audioFile = new File(AUDIO_DIRECTORY + soundFileName);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 各ゾーンのドラッグドロップイベント処理
	 * @param zoneName ゾーン名
	 * @param HBox targetZone ドロップされたゾーン
	 * @param HBox targetZone1 他のゾーン
	 * @param HBox targetZone2 他のゾーン
	 * @param HBox targetZone3 他のゾーン
	 * @param HBox targetZone4 他のゾーン
	 */
	private void setupDragAndDrop(String zoneName, HBox targetZone, HBox otherZone1, HBox otherZone2, HBox otherZone3, HBox otherZone4) {
		targetZone.setOnDragOver(event -> {
			if (event.getGestureSource() != targetZone && event.getDragboard().hasImage()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
		targetZone.setOnDragDropped(event -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasImage()) {
				HoverZoomImageView droppedImageView = (HoverZoomImageView) event.getGestureSource();
				moveCardToZone(droppedImageView, targetZone, otherZone1, otherZone2, otherZone3, otherZone4, zoneName);

				//シールドゾーンにドロップされた場合は、裏側画像に差し替えとシールド情報をONにする
				if(zoneName.equals(ZONE_NAMES[1])) {
					droppedImageView.setShildOn(true);
					Image deckImage = new Image(new File(SHILD_IMAGE).toURI().toString());
					droppedImageView.setImage(deckImage);
				}else {
					droppedImageView.setShildOn(false);
					Image deckImage = new Image(droppedImageView.getImageUrl());
					droppedImageView.setImage(deckImage);
				}
				//ドロップ先のカード領域幅を調整
				cardDropNarrow(targetZone, zoneName);
				//ドラックしたカード領域幅を調整
				cardDraqNarrow(droppedImageView, targetZone, otherZone1, otherZone2, otherZone3, otherZone4);

				droppedImageView.setCurrentZone(zoneName);
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});
	}


	/**
	 * 各ゾーンへドロップされたときのカード削除、追加処理
	 * @param droppedImageView 対象の画像 
	 * @param HBox targetZone ドロップされたゾーン
	 * @param HBox targetZone1 他のゾーン
	 * @param HBox targetZone2 他のゾーン
	 * @param HBox targetZone3 他のゾーン
	 * @param HBox targetZone4 他のゾーン
	 */
	private void moveCardToZone(HoverZoomImageView droppedImageView, HBox targetZone, HBox otherZone1, HBox otherZone2, HBox otherZone3, HBox otherZone4, String zoneName) {
		//移動したのが違うゾーンだった時
		if(!zoneName.equals(droppedImageView.getCurrentZone())) {
			if(zoneName.equals(ZONE_NAMES[3])) {
				targetZone.getChildren().add(0,droppedImageView);
			}else {
				targetZone.getChildren().add(droppedImageView);
			}
			otherZone1.getChildren().remove(droppedImageView);
			otherZone2.getChildren().remove(droppedImageView);
			otherZone3.getChildren().remove(droppedImageView);
			otherZone4.getChildren().remove(droppedImageView);

			//異なるゾーンに移動したらタップ解除 + カード回転
			if(zoneName.equals(ZONE_NAMES[3])) {
				System.out.println(droppedImageView.getRotate());
				droppedImageView.setRotate(180);
			}else {
				droppedImageView.setRotate(0);
			}
			droppedImageView.setTapState(false);
		}
	}

	/**
	 * ドロップされた領域が11枚以上の場合は、全体が1000pxになるようにカードを納める。
	 * 10枚以下の場合も元の画像に戻す
	 * 右端のカードは全て表示する。
	 * @param targetZone ドロップされたゾーン
	 * @param zoneName ゾーン名
	 */
	private void cardDropNarrow(Pane  targetZone, String zoneName){
		int cordNum = targetZone.getChildren().size();
		int count = 0;

		//11枚以上の場合
		if (cordNum > CARD_MAX) {
			double cardWidh = 900.0 / (cordNum - 1);
			for (Node node : targetZone.getChildren()) {
				count++;
				//マナゾーン以外は切り取り地点が左端
				if(!zoneName.equals(ZONE_NAMES[3])) {
					HoverZoomImageView hoverZoomImageView = (HoverZoomImageView) node;
					if(cordNum != count) {
						hoverZoomImageView.setViewport(new Rectangle2D(0, 0, hoverZoomImageView.getWidth() - (100 - cardWidh), hoverZoomImageView.getFitHeight()));
						hoverZoomImageView.setFitWidth(cardWidh);
					}else {
						hoverZoomImageView.setViewport(null);
						hoverZoomImageView.setFitWidth(CARD_WIDTH);
						hoverZoomImageView.setFitHeight(CARD_HEIGHT);
					}
				}
				//マナゾーンは反転しているので切り取り地点が右端
				else {
					HoverZoomImageView hoverZoomImageView = (HoverZoomImageView) node;
					if(1 != count) {
						hoverZoomImageView.setViewport(new Rectangle2D(100 - cardWidh, 0, hoverZoomImageView.getWidth() - (100 - cardWidh), hoverZoomImageView.getFitHeight()));
						hoverZoomImageView.setFitWidth(cardWidh);
					}else {
						hoverZoomImageView.setViewport(null);
						hoverZoomImageView.setFitWidth(CARD_WIDTH);
						hoverZoomImageView.setFitHeight(CARD_HEIGHT);
					}
				}
			}
		}
		//10枚以上の場合
		else {
			for (Node node : targetZone.getChildren()) {
				HoverZoomImageView hoverZoomImageView = (HoverZoomImageView) node;
				hoverZoomImageView.setViewport(null);
				hoverZoomImageView.setFitWidth(CARD_WIDTH);
				hoverZoomImageView.setFitHeight(CARD_HEIGHT);
			}
		}
	}

	/**
	 * ドラッグした時のカード幅調整
	 * @param HoverZoomImageView droppedImageView ドラッグされたImage
	 * @param HBox zone1 ゾーン領域
	 * @param HBox zone2 ゾーン領域
	 * @param HBox zone3 ゾーン領域
	 * @param HBox zone4 ゾーン領域
	 * @param HBox zone5 ゾーン領域
	 */
	private void cardDraqNarrow(HoverZoomImageView droppedImageView,  HBox zone1, HBox zone2, HBox zone3, HBox zone4, HBox zone5){
		String dragZoneName = droppedImageView.getCurrentZone();
		if(dragZoneName.equals(zone1.getId())) {
			cardDropNarrow(zone1, dragZoneName);
		}else if (dragZoneName.equals(zone2.getId())){
			cardDropNarrow(zone2, dragZoneName);
		}else if (dragZoneName.equals(zone3.getId())){
			cardDropNarrow(zone3, dragZoneName);
		}else if (dragZoneName.equals(zone4.getId())){
			cardDropNarrow(zone4, dragZoneName);
		}else if (dragZoneName.equals(zone5.getId())){
			cardDropNarrow(zone5, dragZoneName);
		}
	}

	
	private static class detailImage extends ImageView {
		private String ida;
		private String name;
		private Boolean cost;
		private Boolean mana;

	}
	
}
