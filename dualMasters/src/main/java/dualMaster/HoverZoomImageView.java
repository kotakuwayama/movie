package dualMaster;

import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ドラッグドロップ用のImageクラス
 * @param Image image
 * @param String imageUrl
 * @param String zone
 */
class HoverZoomImageView extends ImageView {
	private String imageUrl;
	private String currentZone;
	private Boolean shildOn;
	private Boolean tapState;
	private static final String[] ZONE_NAMES = {"バトルゾーン", "シールドゾーン", "手札", "マナゾーン", "どこでもないゾーン","デッキ","墓地","GRゾーン"};
	private static final String DECK_IMAGE = "C:\\pleiades\\2023-06\\workspace\\dualMasters\\image\\デッキ画像.jpg";
	
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl() {
		this.imageUrl = imageUrl;
	}
	public String getCurrentZone() {
		return currentZone;
	}
	public void setCurrentZone(String zone) {
		this.currentZone = zone;
	}
	public Boolean getShildOn() {
		return shildOn;
	}
	public void setShildOn(Boolean shildOn) {
		this.shildOn = shildOn;
	}
	public double getWidth() {
		Image image = this.getImage();
		return image.getWidth();
	}
	public double getHeight() {
		Image image = this.getImage();
		return image.getHeight();
	}
	public Boolean getTapState() {
		return tapState;
	}
	public void setTapState(Boolean tapState) {
		this.tapState = tapState;
	}

	/**
	 * HoverZoomImageViewのコンストラクタ
	 * @param Image image 表示する画像
	 * @param String imageUrl 表示はしないが内部で持っているカードのファイルパス
	 * @param String zone 表示しているゾーン
	 */
	public HoverZoomImageView(Image image, String imageUrl, String zone) {
		super(image);
		this.imageUrl = imageUrl;
		this.currentZone = zone;
		this.tapState = false;

		if(zone.equals(ZONE_NAMES[1])) {
			this.shildOn = true;
		}else {
			this.shildOn = false;
		}

		//カードをクリックしたときは シールド状態なら裏→表の繰り返し  そうでないなら拡大表示
		setOnMouseClicked(event -> {

			//左クリック⇒拡大 or シールド開く
			if (event.getButton() == MouseButton.PRIMARY) {
				File file = new File(imageUrl);
				Image newImage = new Image(file.toURI().toString());

				//シールドが裏側の場合は、表のカードに差し替える
				if(currentZone.equals(ZONE_NAMES[1]) && shildOn) {
					setImage(newImage);
					shildOn = false;
				}
				//シールドが表側の場合は、裏のカードに差し替える
				else if(currentZone.equals(ZONE_NAMES[1]) && !shildOn) {
					File deckFile = new File(DECK_IMAGE);
					Image deckImage = new Image(deckFile.toURI().toString());
					setImage(deckImage);
					shildOn = true;
				}
				//拡大表示
				else {
					Image Image = new Image(imageUrl);
					HoverZoomImageView hoverZoomImageView = new HoverZoomImageView(Image, imageUrl, zone);
					hoverZoomImageView.setFitWidth(350);
					hoverZoomImageView.setFitHeight(525);

					//墓地ゾーン以外のカードは拡大表示をつける
					if(!currentZone.equals(ZONE_NAMES[6])) {
						VBox zoomedCardLayout = new VBox(hoverZoomImageView);
						zoomedCardLayout.setAlignment(Pos.CENTER);
						Scene zoomedCardScene = new Scene(zoomedCardLayout, 350, 525);
						Stage zoomedCardStage = new Stage();
						// ウィンドウを閉じる処理を追加
						hoverZoomImageView.setOnMouseClicked(closeEvent -> {
							zoomedCardStage.close();
						});
						zoomedCardStage.setScene(zoomedCardScene);
						zoomedCardStage.show();
					}
				}
			}
			//右クリック⇒タップ
			else if (event.getButton() == MouseButton.SECONDARY) {
				if(!this.currentZone.equals(ZONE_NAMES[3])) {
					if(this.tapState == false) {
						this.tapState = true;
						this.setRotate(270);
					}else {
						this.tapState = false;
						this.setRotate(0);
					}
				}
				//マナゾーンは逆方向に回転
				else {
					if(this.tapState == false) {
						this.tapState = true;
						this.setRotate(270);
					}else {
						this.tapState = false;
						this.setRotate(180);
					}
				}
			}
		});
	}
}
