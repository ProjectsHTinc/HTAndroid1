package com.hst.osa_tatva.ccavenue.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hst.osa_tatva.R;
import com.hst.osa_tatva.helpers.ProgressDialogHelper;
import com.hst.osa_tatva.interfaces.DialogClickListener;
import com.hst.osa_tatva.servicehelpers.ServiceHelper;
import com.hst.osa_tatva.serviceinterfaces.IServiceListener;
import com.hst.osa_tatva.utils.OSAConstants;
import com.hst.osa_tatva.utils.PreferenceStorage;

import org.json.JSONException;
import org.json.JSONObject;


public class StatusActivity extends AppCompatActivity implements IServiceListener, DialogClickListener {

	LinearLayout advLayout, payLayout, walletLayout;
	ImageView paymentIcon, bookingIcon, walletIcon;
	TextView paymentStatus, paymentComment, bookingStatus, bookingComment, walletStatus, walletComment;
	Button booking, rate, wallet;
	String page = "";
	String status = "";

	private ServiceHelper serviceHelper;
	private ProgressDialogHelper progressDialogHelper;

	private int a = 1;
	private Handler handler = new Handler();
//	public void startTimer() {
//		handler.postDelayed(new Runnable() {
//			public void run() {
//				checkProviderAssign();
//				handler.postDelayed(this, 60000);
//			}
//		}, 60000);
//	}

	private void suucc() {
		JSONObject jsonObject = new JSONObject();
		String orderId = "";
		orderId = PreferenceStorage.getOrderId(this);

		try {
			jsonObject.put(OSAConstants.PARAMS_ORDER_ID, orderId);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = OSAConstants.BUILD_URL + OSAConstants.SUCCESSFUL_PAYMENT;
		serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_status);

		initVals();

		Intent mainIntent = getIntent();

		TextView tv4 = (TextView) findViewById(R.id.textView1);
		tv4.setText(mainIntent.getStringExtra("transStatus"));
		status = mainIntent.getStringExtra("transStatus");

		page = (String) mainIntent.getStringExtra("page");

		setPageVal();

		if (page.equalsIgnoreCase("advance_pay") && !(status.equalsIgnoreCase("Transaction Declined!")||status.equalsIgnoreCase("Transaction Cancelled!"))){
//			sendAdvanceStatus();
//			final int oneMin = 3 * 60 * 1000; // 1 minute in milli seconds
//
//			/** CountDownTimer starts with 1 minutes and every onTick is 1 second */
//			new CountDownTimer(oneMin, 1000) {
//				public void onTick(long millisUntilFinished) {
//
//					//forward progress
//					long finishedSeconds = oneMin - millisUntilFinished;
//					int total = (int) (((float)finishedSeconds / (float)oneMin) * 100.0);
//
////                //backward progress
////                int total = (int) (((float) millisUntilFinished / (float) oneMin) * 100.0);
////                progressBar.setProgress(total);
//
//				}
//
//				public void onFinish() {
//					// DO something when 1 minute is up
//					handler.removeCallbacksAndMessages(null);
//
//				}
//			}.start();
//			startTimer();
		} else {

		}

	}

	private void initVals() {

		serviceHelper = new ServiceHelper(this);
		serviceHelper.setServiceListener(this);
		progressDialogHelper = new ProgressDialogHelper(this);

		advLayout = findViewById(R.id.advance_layout);
		bookingIcon = findViewById(R.id.status_img);
		bookingStatus = findViewById(R.id.status_text);
		bookingComment = findViewById(R.id.status_comment_text);
		booking = findViewById(R.id.home_booking);

		walletLayout = findViewById(R.id.wallet_layout);
		walletIcon = findViewById(R.id.wallet_status_img);
		walletStatus = findViewById(R.id.wallet_status_text);
		walletComment = findViewById(R.id.wallet_status_comment_text);
		wallet = findViewById(R.id.wallet_home_booking);

		payLayout = findViewById(R.id.final_payment_layout);
		paymentIcon = findViewById(R.id.payment_status_icon);
		paymentStatus = findViewById(R.id.payment_status_text);
		paymentComment = findViewById(R.id.payment_status_comment_text);
		rate = findViewById(R.id.rate_service);
	}

	private void setPageVal() {

		if(page.equalsIgnoreCase("advance_pay")) {
			payLayout.setVisibility(View.GONE);

			findViewById(R.id.toolbar).setVisibility(View.VISIBLE);

			advLayout.setVisibility(View.VISIBLE);
			if(status.equalsIgnoreCase("Transaction Declined!")||status.equalsIgnoreCase("Transaction Cancelled!")) {
//				bookingIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_servicebook_failed));
				bookingStatus.setText(R.string.booking_failed);
				bookingComment.setText(R.string.booking_failed_comment);
				booking.setText(R.string.try_again);
				booking.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						Intent newIntent = new Intent(getApplicationContext(), BookingSummaryAcivity.class);
//						startActivity(newIntent);
//						finish();
					}
				});

			} else {
//				bookingIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_servicebook_success));
				bookingStatus.setText(R.string.booking_success);
				bookingComment.setText(R.string.booking_success_comment);
				booking.setText(R.string.alert_button_ok);
				booking.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						Intent newIntent = new Intent(getApplicationContext(), RequestedServicesActivity.class);
//						newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//						startActivity(newIntent);
//						finish();
					}
				});
			}
		} else if(page.equalsIgnoreCase("wallet")) {
			payLayout.setVisibility(View.GONE);
			advLayout.setVisibility(View.GONE);

			findViewById(R.id.toolbar).setVisibility(View.VISIBLE);

			walletLayout.setVisibility(View.VISIBLE);
			if(status.equalsIgnoreCase("Transaction Declined!")||status.equalsIgnoreCase("Transaction Cancelled!")) {
//				walletIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_servicebook_failed));
				walletStatus.setText(R.string.booking_failed);
				walletComment.setText(R.string.wallet_failed_comment);
				wallet.setText(R.string.try_again);
				wallet.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						Intent newIntent = new Intent(getApplicationContext(), WalletAddMoneyActivity.class);
//						startActivity(newIntent);
//						finish();
					}
				});

			} else {
//				walletIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_servicebook_success));
				walletStatus.setText(R.string.booking_success);
				walletComment.setText(R.string.wallet_success_comment);
				wallet.setText(R.string.go_home);
				wallet.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						Intent newIntent = new Intent(getApplicationContext(), WalletActivity.class);
//						newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//						startActivity(newIntent);
//						finish();
					}
				});
			}
		} else if (page.equalsIgnoreCase("service_pay")){
			advLayout.setVisibility(View.GONE);
			findViewById(R.id.toolbar).setVisibility(View.GONE);
			payLayout.setVisibility(View.VISIBLE);
			if(status.equalsIgnoreCase("Transaction Declined!")||status.equalsIgnoreCase("Transaction Cancelled!")) {

//				payLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.payment_failed_bg));
//				paymentIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_payment_failed));
//				paymentStatus.setText(R.string.payment_failed);
//				paymentStatus.setTextColor(ContextCompat.getColor(this, R.color.payment_failed_font));
//				paymentComment.setText(R.string.payment_failed_comment);
//				paymentComment.setTextColor(ContextCompat.getColor(this, R.color.payment_failed_font));
//				rate.setText(R.string.try_again);
//				rate.setBackground(ContextCompat.getDrawable(this, R.drawable.button_try_again));
//				rate.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						Intent intent = new Intent(getApplicationContext(), ServiceHistoryActivity.class);
//						startActivity(intent);
//						finish();
//					}
//				});

			} else {
				suucc();
//				payLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.payment_success_bg));
//				paymentIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_payment_success));
//				paymentStatus.setText(R.string.payment_success);
//				paymentComment.setText(R.string.payment_success_comment);
//				rate.setText(R.string.rating_text);
//				rate.setBackground(ContextCompat.getDrawable(this, R.drawable.button_rate_service));
//				rate.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						Intent intent = new Intent(getApplicationContext(), RateServiceActivity.class);
//						startActivity(intent);
//						finish();
//					}
//				});

			}
		}
	}

	public void showToast(String msg) {
		Toast.makeText(this, "Toast: " + msg, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onAlertPositiveClicked(int tag) {

	}

	@Override
	public void onAlertNegativeClicked(int tag) {

	}

	@Override
	public void onResponse(JSONObject response) {
//		try {
//			if (response.getString("msg").equalsIgnoreCase("Advance paid Successfully")) {
//				Intent intent = new Intent(this, AdvancePaymentActivity.class);
//				startActivity(intent);
//				finish();
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void onError(String error) {

	}
}