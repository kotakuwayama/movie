package movieDetection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.SubscriptionItem;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.SubscriptionListParams;

public class SubscriptionCheck {

	//ローカル用
	private static String sqlHost = "jdbc:mysql://localhost:3306/sys";
	private static String sqlUser = "root";
	private static String sqlPassword = "omega4242";

	public static void main(String[] args) {

		// Stripe APIキーをセットアップ
		Stripe.apiKey = "sk_test_51Oqvn1I8TZwnhH04Wm8IHI85NgQM8TZPnyWYEcEyWTEMvi9EZjY49Ik8L7TSV2rsmhRyXu87VcPNbmwwPTD0vjGe00y9GROWtE";

		//プロパティ読み込み
		String currentPath = Paths.get("").toAbsolutePath().toString();
		String path = currentPath + "\\system.properties";
		String email = "";
		try {
			InputStream istream = new FileInputStream(path);
			Properties properties = new Properties();
			properties.load(istream);
			email = properties.getProperty("mailAddress");

			String customerId = "";
			//顧客リストを取得
			CustomerListParams params = CustomerListParams.builder()
					.setLimit((long) 1000) // 必要に応じて制限を設定
					.build();

			CustomerCollection customers = Customer.list(params);
			List<Customer> customerList = customers.getData();

			//顧客リストからメールアドレスでユーザを特定
			for (Customer customer : customerList) {
				System.out.println("Email:" + customer.getEmail() + "  Id:" + customer.getId());
				if (email.equals(customer.getEmail())) {
					customerId = customer.getId();
					break;
				}
			}

			//顧客IDの登録
			boolean isSubscribed = checkSubscriptionStatus(customerId);
			if (isSubscribed) {

			}
		} catch (IOException | StripeException e) {
			System.out.println(e.getMessage());
		}
	}

	private static boolean checkSubscriptionStatus(String customerId) throws StripeException {
		SubscriptionListParams params = SubscriptionListParams.builder()
				.setCustomer(customerId)
				.build();

		SubscriptionCollection subscriptions = Subscription.list(params);
		List<Subscription> subscriptionList = subscriptions.getData();

		for (Subscription subscription : subscriptionList) {
			if ("active".equals(subscription.getStatus())) {
				System.out.println("サブスクリプションID: " + subscription.getId());
				System.out.println("サブスクリプションの開始日: " + epochToDateTime(subscription.getCurrentPeriodStart()));
				System.out.println("サブスクリプションの終了日: " + epochToDateTime(subscription.getCurrentPeriodEnd()));

				for (SubscriptionItem item : subscription.getItems().getData()) {
					System.out.println("プランID: " + item.getPlan().getId());
					System.out.println("プラン名: " + item.getPlan().getNickname());
					System.out.println("プランの金額: " + item.getPlan().getAmount());
					System.out.println("通貨: " + item.getPlan().getCurrency());
				}

				return true; // アクティブなサブスクリプションがある場合
			}
		}
		return false; // アクティブなサブスクリプションがない場合
	}

	private static String epochToDateTime(Long epochTime) {
		if (epochTime == null)
			return "不明な日時";
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime), ZoneId.systemDefault());
		return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public static String registerUser(String email) {

		//顧客IDの存在チェック
		String customerId = getCustomerId(email);
		if (customerId != null) {
			System.out.println("すでに登録されているユーザです。顧客ID: " + customerId);
			return customerId;
		}

		try {
			CustomerCreateParams params = CustomerCreateParams.builder()
					.setEmail(email)
					.build();

			Customer customer = Customer.create(params);
			customerId = customer.getId();

			//顧客IDが存在しないので登録
			saveCustomerId(email, customerId);
			return customerId;

		} catch (StripeException e) {
			e.printStackTrace();
			return null;
		}
	}

	//既に存在する顧客IDかチェック
	public static String getCustomerId(String email) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String sql = "SELECT stripe_customer_id FROM subscription WHERE email = ?";
		String customerId = null;

		try {
			con = DriverManager.getConnection(sqlHost, sqlUser, sqlPassword);
			stmt = con.prepareStatement(sql);
			stmt.setString(1, email);
			rs = stmt.executeQuery();

			if (rs.next()) {
				customerId = rs.getString("stripe_customer_id");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return customerId;
	}

	//顧客IDの新規登録
	private static void saveCustomerId(String email, String customerId) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String sql = "INSERT INTO subscription (email, stripe_customer_id) VALUES (?, ?)";

		try {
			con = DriverManager.getConnection(sqlHost, sqlUser, sqlPassword);
			stmt = con.prepareStatement(sql);

			stmt.setString(1, email);
			stmt.setString(2, customerId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
