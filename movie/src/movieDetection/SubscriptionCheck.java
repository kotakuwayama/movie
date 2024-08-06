package movieDetection;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerListParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;

public class SubscriptionCheck {

	public static String stripeKey = "";
	public static String username = "";
	public static String password = "";
	public static String productId = "";

	public static Boolean subscriptionCheck(String email) throws Exception {

		//★★★★★★桑山用★★★★★★
		stripeKey = "sk_test_51Oqvn1I8TZwnhH04Wm8IHI85NgQM8TZPnyWYEcEyWTEMvi9EZjY49Ik8L7TSV2rsmhRyXu87VcPNbmwwPTD0vjGe00y9GROWtE";
		Stripe.apiKey = stripeKey;
		username = "southkouta@gmail.com"; // 送信元のGmailアドレス
		password = "jqcv efri zjwx lahz"; // 送信元のGmailパスワード
		productId = "price_1PkmHSI8TZwnhH048sdpj1Hz";

		//★★★★★★やっぺ用★★★★★★
		//		stripeKey = "sk_test_51Oqvn1I8TZwnhH04Wm8IHI85NgQM8TZPnyWYEcEyWTEMvi9EZjY49Ik8L7TSV2rsmhRyXu87VcPNbmwwPTD0vjGe00y9GROWtE";
		//		Stripe.apiKey = stripeKey;
		//		username = "rakupachi123@gmail.com"; // 送信元のGmailアドレス
		//		password = "drlx auwc cpbu mtko"; // 送信元のGmailパスワード
		//		productId = "price_1PkmIpLHAkDOtX8SvDIGClas";

		String customerId = "";
		String currentSerialNumber = "";
		String registerSerialNumber = "";

		//シリアル番号を取得
		SystemInfo si = new SystemInfo();
		ComputerSystem cs = si.getHardware().getComputerSystem();
		currentSerialNumber = cs.getSerialNumber();

		//顧客リストを全件取得
		CustomerListParams params = CustomerListParams.builder()
				.setLimit((long) 1000) // 必要に応じて制限を設定
				.build();

		//プロパティのメールアドレスと一致するまで回す
		CustomerCollection customers = Customer.list(params);
		List<Customer> customerList = customers.getData();
		for (Customer customer : customerList) {
			Map<String, String> metaList = customer.getMetadata();
			registerSerialNumber = metaList.get("serialNumber");

			//メールアドレスとシリアル番号がstripeに登録してあるデータと一致したら顧客IDを取得
			if (email.equals(customer.getEmail()) && currentSerialNumber.equals(registerSerialNumber)) {
				customerId = customer.getId();
				break;
			}
			//メールアドレスは一致するが、シリアル番号が一致しない場合メールを飛ばす
			else if (email.equals(customer.getEmail()) && !currentSerialNumber.equals(registerSerialNumber)) {
				System.out.println("異常検知：シリアル番号不一致");

				// SMTPサーバの設定
				Properties props = new Properties();
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.host", "smtp.gmail.com");
				props.put("mail.smtp.port", "587");

				javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});

				try {
					// メールメッセージの作成
					Message message = new MimeMessage(session);
					message.setFrom(new InternetAddress(username));
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
					message.setSubject("パチンコツール シリアル番号異常検知");
					message.setText("メールアドレスが一致していますが、シリアル番号が異なるアクセスがありました。"
							+ "\n  メールアドレス⇒" + email + "\n  アクセスPCのシリアル番号⇒" + currentSerialNumber + "\n  stripeに登録されているこのユーザのシリアル番号⇒" + registerSerialNumber);

					// メールの送信
					Transport.send(message);
					//					showAlert(AlertType.ERROR, "Information", "シリアル番号が一致していません。");
					System.out.println("Email sent successfully!");

				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Email=" + email);
		System.out.println("customerId=" + customerId);

		//サブスク支払い判定
		boolean isSubscribed = checkSubscriptionStatus(customerId);
		//		showAlert(AlertType.INFORMATION, "Information", "未登録のため支払いをしてください");

		return isSubscribed;

	}

	//支払いリンクを作成
	public static Boolean payment(String email) throws Exception {

		System.out.println("支払い画面を表示");

		// Checkoutセッションのパラメータを設定
		SessionCreateParams checkOutParam = SessionCreateParams.builder()
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
				.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
				.setSuccessUrl("https://your-domain.com/success?session_id={CHECKOUT_SESSION_ID}")
				.setCancelUrl("https://your-domain.com/cancel")
				.addLineItem(
						SessionCreateParams.LineItem.builder()
								.setPrice(productId)
								.setQuantity(1L)
								.build())
				.setCustomerEmail(email)
				.build();

		// Checkoutセッションの作成
		Session session = Session.create(checkOutParam);
		String sessionId = session.getId();
		String sessionUrl = session.getUrl();

		// ブラウザでセッションURLを開く
		openBrowser(sessionUrl);

		int cnt = 0;
		Boolean payJudge = false;
		while (true) {
			try {
				// Checkoutセッションのステータスを取得する
				session = Session.retrieve(sessionId);

				//一定回数チェックしても終わらない場合は強制終了
				if (cnt > 10) {
					break;
				}

				// ステータスを確認し、支払いが成功しているかどうかを確認
				if ("complete".equals(session.getStatus())) {
					System.out.println("支払い成功");
					payJudge = true;
					break;
				} else {
					cnt++;
					System.out.println("支払いが完了していません。status: " + session.getStatus());
					payJudge = false;
				}

				// 一定時間待ってから再度確認する
				Thread.sleep(20000); // 20秒待つ
			} catch (StripeException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Checkoutセッションを取得
		session = Session.retrieve(sessionId);

		// セッションから顧客IDを取得
		String customerId = session.getCustomer();

		// 顧客IDを使用して顧客情報を取得
		Customer customer = Customer.retrieve(customerId);

		// 顧客情報を表示
		System.out.println("Customer ID: " + customer.getId());
		System.out.println("Customer Email: " + customer.getEmail());
		System.out.println("Customer Name: " + customer.getName());

		// 顧客更新パラメータを設定
		SystemInfo si = new SystemInfo();
		ComputerSystem cs = si.getHardware().getComputerSystem();
		// シリアル番号を取得
		String currentSerialNumber = cs.getSerialNumber();
		CustomerUpdateParams updateParams = CustomerUpdateParams.builder()
				.putMetadata("serialNumber", currentSerialNumber)
				.build();

		// 顧客情報を更新
		customer = customer.update(updateParams);
		return payJudge;

	}

	// メッセージボックスを表示するメソッド
	private static void showAlert(AlertType alertType, String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(alertType);
			alert.setTitle(title);
			alert.setHeaderText(null); // ヘッダーを非表示にする場合
			alert.setContentText(message);
			alert.showAndWait();
		});
	}

	//サブスクの支払いチェック
	private static boolean checkSubscriptionStatus(String customerId) throws StripeException {
		SubscriptionListParams params = SubscriptionListParams.builder()
				.setCustomer(customerId)
				.build();

		if (customerId.equals("")) {
			return false;
		}

		SubscriptionCollection subscriptions = Subscription.list(params);
		List<Subscription> subscriptionList = subscriptions.getData();
		for (Subscription subscription : subscriptionList) {
			if ("active".equals(subscription.getStatus())) {
				//				System.out.println("サブスクリプションID: " + subscription.getId());
				System.out.println("サブスクリプションの開始日: " + epochToDateTime(subscription.getCurrentPeriodStart()));
				System.out.println("サブスクリプションの終了日: " + epochToDateTime(subscription.getCurrentPeriodEnd()));

				return true; // アクティブなサブスクリプションがある場合
			}
		}
		return false; // アクティブなサブスクリプションがない場合
	}

	// ブラウザで指定したURLを開くメソッド
	private static void openBrowser(String url) {
		try {
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				// Windowsの場合
				Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url });
			} else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
				// macOSの場合
				Runtime.getRuntime().exec("open " + url);
			} else if (System.getProperty("os.name").toLowerCase().contains("nix") ||
					System.getProperty("os.name").toLowerCase().contains("nux")) {
				// Unix/Linuxの場合
				Runtime.getRuntime().exec("xdg-open " + url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String epochToDateTime(Long epochTime) {
		if (epochTime == null)
			return "不明な日時";
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime), ZoneId.systemDefault());
		return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

}
